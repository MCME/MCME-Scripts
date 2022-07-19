package com.mcmiddleearth.mcmescripts.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mcmiddleearth.command.McmeCommandSender;
import com.mcmiddleearth.entities.EntitiesPlugin;
import com.mcmiddleearth.mcmescripts.ConfigKeys;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.script.ScriptManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DriveUtil {
    private static final String APPLICATION_NAME = "MCME Scripts";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);//DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        try (InputStream in = new FileInputStream(new java.io.File(MCMEScripts.getInstance().getDataFolder(),
                CREDENTIALS_FILE_PATH))) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            java.io.File tokenDir = new java.io.File(MCMEScripts.getInstance().getDataFolder(),
                    TOKENS_DIRECTORY_PATH);
            if (!tokenDir.exists()) {
                tokenDir.mkdir();
            }
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(tokenDir))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
    }

    private static Credential getServiceCredentials() throws IOException {
        return GoogleCredential
                .fromStream(new FileInputStream(new java.io.File(MCMEScripts.getInstance().getDataFolder(),
                        "key.json")))
                .createScoped(SCOPES);
                //.createDelegated("user@example.com");
    }

    private static Credential getTokenCredential() throws IOException {
        return new GoogleCredential().setAccessToken(MCMEScripts.getConfigString(ConfigKeys.DRIVE_ACCESS_TOKEN,""));

    }

    public static GoogleCredential getServiceCredentialImpersonate(
            HttpTransport transport,
            JsonFactory jsonFactory,
            String serviceAccountId,
            Collection<String> serviceAccountScopes,
            java.io.File p12File,
            String serviceAccountUser) throws GeneralSecurityException, IOException {
        return new GoogleCredential.Builder().setTransport(transport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountId)
                .setServiceAccountScopes(serviceAccountScopes)
                .setServiceAccountPrivateKeyFromP12File(p12File)
                .setServiceAccountUser(serviceAccountUser)
                .build();
    }

    public static void refreshToken() {
        URL url;
        try {
            url = new URL("https://oauth2.googleapis.com/token");
            URLConnection con = url.openConnection();
            HttpsURLConnection http = (HttpsURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            //http.setRequestProperty("client_id", MCMEScripts.getConfigString(ConfigKeys.DRIVE_CLIENT_ID,""));
            //http.setRequestProperty("client_secret", MCMEScripts.getConfigString(ConfigKeys.DRIVE_CLIENT_SECRET,""));
            //http.setRequestProperty("grant_type","refresh_token");
            //http.setRequestProperty("request_token", MCMEScripts.getConfigString(ConfigKeys.DRIVE_REFRESH_TOKEN,""));
            http.setDoOutput(true);
            String fields = "client_id="+MCMEScripts.getConfigString(ConfigKeys.DRIVE_CLIENT_ID,"")
                    +"&client_secret="+MCMEScripts.getConfigString(ConfigKeys.DRIVE_CLIENT_SECRET,"")
                    +"&grant_type=refresh_token"
                    +"&refresh_token="+MCMEScripts.getConfigString(ConfigKeys.DRIVE_REFRESH_TOKEN,"");
            //System.out.println(fields);
            byte[] out = fields.getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
                os.flush();
            }
            //JsonParser parser = new JsonParser();
            JsonElement response = JsonParser.parseReader(new JsonReader(new InputStreamReader(http.getInputStream())));
            JsonElement tokenJson = response.getAsJsonObject().get("access_token");
            if(tokenJson instanceof JsonPrimitive) {
                String token = tokenJson.getAsString();
                MCMEScripts.setConfigString(ConfigKeys.DRIVE_ACCESS_TOKEN,token);
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void readFiles() {
        // Build a new authorized API client service.
        new BukkitRunnable() {
            public void run() {
                final NetHttpTransport HTTP_TRANSPORT;

                try {
                    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
//                                        getCredentials(HTTP_TRANSPORT)
                            getTokenCredential()
//                                     getServiceCredentials()
/*                                    getServiceCredentialImpersonate(HTTP_TRANSPORT, JSON_FACTORY,
                                            "118347752578020467440",SCOPES,
                                            new java.io.File(MCMEScripts.getInstance().getDataFolder(),"key.p12"),
                                            "mcmiddleearth.com@gmail.com"
                                            )*/
                    )
                            .setApplicationName(APPLICATION_NAME)
                            .build();

                    // Print the names and IDs for up to 10 files.
                    FileList result = service.files().list()
                            .setPageSize(100)
                            .setFields("nextPageToken, files(id, name)")
                            .execute();
                    List<File> files = result.getFiles();
                    if (files == null || files.isEmpty()) {
                        System.out.println("No files found.");
                    } else {
                        System.out.println("Files:");
                        for (File file : files) {
                            System.out.printf("%s (%s)\n", file.getName(), file.getId());
                        }
                    }
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(MCMEScripts.getInstance());

    }

    public static void exportFile(McmeCommandSender sender, String parent, String filename) throws IOException, GeneralSecurityException {
        String parentId = getParentId(parent);
        java.io.File localFile = getLocalFile(parent, filename);
        if(parentId == null || localFile == null) {
            sender.sendMessage(new ComponentBuilder().append("Export Failed.").color(ChatColor.RED).create());
            DebugManager.info(Modules.Command.execute(DriveUtil.class),"Export failed: parent or file not found.");
            return;
        }
        // Build a new authorized API client service.
        new BukkitRunnable() {
            public void run() {
                try {
                    export();
                    sender.sendMessage(new ComponentBuilder().append("Export done.").color(ChatColor.GREEN).create());
                    DebugManager.info(Modules.Command.execute(DriveUtil.class),"Exported: "+parent+"/"+filename);
                } catch (GeneralSecurityException | IOException e) {
                    refreshToken();
                    try {
                        export();
                        sender.sendMessage(new ComponentBuilder().append("Export done.").color(ChatColor.GREEN).create());
                        DebugManager.info(Modules.Command.execute(DriveUtil.class),"Exported: "+parent+"/"+filename);
                    } catch (GeneralSecurityException | IOException ex) {
                        sender.sendMessage(new ComponentBuilder().append("Export Failed.").color(ChatColor.RED).create());
                        DebugManager.info(Modules.Command.execute(DriveUtil.class),"Export failed: access denied.");
                        ex.printStackTrace();
                    }
                }
            }

            private void export() throws GeneralSecurityException, IOException {
                final NetHttpTransport HTTP_TRANSPORT;
                    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
//                                        getCredentials(HTTP_TRANSPORT)
                            getTokenCredential()
//                                     getServiceCredentials()
/*                                    getServiceCredentialImpersonate(HTTP_TRANSPORT, JSON_FACTORY,
                                            "118347752578020467440",SCOPES,
                                            new java.io.File(MCMEScripts.getInstance().getDataFolder(),"key.p12"),
                                            "mcmiddleearth.com@gmail.com"
                                            )*/
                    )
                            .setApplicationName(APPLICATION_NAME)
                            .build();

                    File fileMetadata = new File();
                    if(filename.equals("debug.txt")) fileMetadata.setName(filename);
                    else fileMetadata.setName(filename+".json");
                    fileMetadata.setParents(Collections.singletonList(parentId)); //animations
                    //java.io.File filePath = new java.io.File(MCMEScripts.getInstance().getDataFolder(),filename+".json");
                    if(!localFile.exists()) {
                        localFile.createNewFile();
                    }
                    FileContent mediaContent = new FileContent("text/plain", localFile);
                    File file = service.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();
                    System.out.println("File ID: " + file.getId());

                /*try {
                    DriveUtil.readFiles();
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }*/
            }
        }.runTaskAsynchronously(MCMEScripts.getInstance());

    }
    public static void importFile(McmeCommandSender sender, String parent, String filename) throws IOException, GeneralSecurityException {
        String parentId = getParentId(parent);
        java.io.File localFile = getLocalFile(parent, filename);
        if(parentId == null || localFile == null) {
            sender.sendMessage(new ComponentBuilder().append("Import Failed.").color(ChatColor.RED).create());
            DebugManager.info(Modules.Command.execute(DriveUtil.class),"Import failed: parent or file not found.");
            return;
        }
        // Build a new authorized API client service.
        new BukkitRunnable() {
            public void run() {
                try {
                    importer();
                    sender.sendMessage(new ComponentBuilder().append("Import done.").color(ChatColor.GREEN).create());
                    DebugManager.info(Modules.Command.execute(DriveUtil.class),"Imported: "+parent+"/"+filename);
                } catch (GeneralSecurityException | IOException e) {
                    refreshToken();
                    try {
                        importer();
                        sender.sendMessage(new ComponentBuilder().append("Import done.").color(ChatColor.GREEN).create());
                        DebugManager.info(Modules.Command.execute(DriveUtil.class),"Imported: "+parent+"/"+filename);
                    } catch (GeneralSecurityException | IOException ex) {
                        sender.sendMessage(new ComponentBuilder().append("Import Failed.").color(ChatColor.RED).create());
                        DebugManager.info(Modules.Command.execute(DriveUtil.class),"Import failed: access denied.");
                        ex.printStackTrace();
                    }
                }
            }

            private void importer() throws GeneralSecurityException, IOException {
                final NetHttpTransport HTTP_TRANSPORT;
                    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                            getTokenCredential()
                    )
                            .setApplicationName(APPLICATION_NAME)
                            .build();

                    List<String> files = getFileIds(service, parentId, filename);

                    for(String fileId: files) {
                        try (OutputStream outputStream = new FileOutputStream(localFile)) {
                            service.files().get(fileId)
                                    .executeMediaAndDownloadTo(outputStream);
                        }
                    }

            }
        }.runTaskAsynchronously(MCMEScripts.getInstance());

    }

    private static List<String> getFileIds(Drive service, String parent, String filename) throws IOException {
        FileList result = service.files().list()
                .setQ("'"+parent+"'"+" in parents and name = '"+filename+".json'")
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
//System.out.println(files.size());
        return files.stream().map(File::getId).collect(Collectors.toList());
    }

    private static String getParentId(String parent) {
        switch (parent) {
            case "animations": return MCMEScripts.getConfigString(ConfigKeys.DRIVE_FOLDER_ANIMATIONS, "");
            case "entities": return MCMEScripts.getConfigString(ConfigKeys.DRIVE_FOLDER_ENTITIES, "");
            case "scripts": return MCMEScripts.getConfigString(ConfigKeys.DRIVE_FOLDER_SCRIPTS, "");
            default: return null;
        }
    }

    private static java.io.File getLocalFile(String parent, String filename) {
        switch(parent) {
            case "animations":
                return new java.io.File(EntitiesPlugin.getAnimationFolder(),filename + ".json");
            case "entities":
                return new java.io.File(EntitiesPlugin.getEntitiesFolder(),filename + ".json");
            case "scripts":
                if(filename.equals("debug.txt")) return new java.io.File(MCMEScripts.getInstance().getDataFolder(), filename);
                return new java.io.File(ScriptManager.getScriptFolder(),filename + ".json");
        }
        return null;
    }
}