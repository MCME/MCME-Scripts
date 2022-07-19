package com.mcmiddleearth.mcmescripts.debug;

import com.mcmiddleearth.mcmescripts.MCMEScripts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

public class DebugManager {

    public static final String INDENT = "  ";

    private static final Map<String,Level> debugModules = new HashMap<>();

    private static final File logFile = new File(MCMEScripts.getInstance().getDataFolder(),"debug.txt");

    private static PrintWriter writer;

    private static final Map<UUID, ScriptFilter> playerDebugScripts = new HashMap<>();
    private static ScriptFilter consoleDebugScript = ScriptFilter.NoScriptFilter();
    private static ScriptFilter fileDebugScript = ScriptFilter.AllScriptFilter();

    public static void open() {
        Arrays.stream(Modules.values()).filter(module -> !debugModules.containsKey(module.getModule()))
                .forEach(module -> debugModules.put(module.getModule(),Level.VERBOSE));
        Arrays.stream(Modules.values()).filter(module -> !debugModules.containsKey(module.getModule().split("\\.")[0]))
                .forEach(module -> debugModules.put(module.getModule().split("\\.")[0],Level.VERBOSE));
        if(logFile.exists()) {
            if(!logFile.delete()) {
                Logger.getLogger(MCMEScripts.class.getSimpleName()).info("Can't delete old log file!");
            }
        }
        try {
            if(logFile.createNewFile()) {
                Logger.getLogger(MCMEScripts.class.getSimpleName()).info("Creating log file!");
            }
            writer = new PrintWriter(new FileWriter(logFile),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setConsoleDebugScript(String scriptName) {
        if(scriptName == null || scriptName.equalsIgnoreCase("none")) {
            consoleDebugScript = ScriptFilter.NoScriptFilter();
        } else if(scriptName.equals("*") || scriptName.equalsIgnoreCase("all")) {
            consoleDebugScript = ScriptFilter.AllScriptFilter();
        } else {
            consoleDebugScript = ScriptFilter.OneScriptFilter(scriptName);
        }
    }

    public static void setFileDebugScript(String scriptName) {
        if(scriptName == null || scriptName.equalsIgnoreCase("none")) {
            fileDebugScript = ScriptFilter.NoScriptFilter();
        } else if(scriptName.equals("*") || scriptName.equalsIgnoreCase("all")) {
            fileDebugScript = ScriptFilter.AllScriptFilter();
        } else {
            fileDebugScript = ScriptFilter.OneScriptFilter(scriptName);
        }
    }

    public static void setPlayerDebugScript(Player player, String scriptName) {
        if(scriptName == null || scriptName.equalsIgnoreCase("none")) {
            playerDebugScripts.remove(player.getUniqueId());
        } else if(scriptName.equals("*") || scriptName.equalsIgnoreCase("all")) {
            playerDebugScripts.put(player.getUniqueId(), ScriptFilter.AllScriptFilter());
        } else {
            playerDebugScripts.put(player.getUniqueId(), ScriptFilter.OneScriptFilter(scriptName));
        }
    }

    public static void close() {
        writer.close();
    }

    public static Level cycleDebugLevel(String module) {
        Level level;
        if(module.equals("all")) {
            setDebugLevel("all", Level.VERBOSE);
            level = Level.VERBOSE;
        } else if(module.equals("none")) {
            setDebugLevel("all", Level.CRITICAL);
            level = Level.CRITICAL;
        } else {
            level = Level.next(debugModules.get(module));
            setDebugLevel(module, level);
        }
        log("Debug Modules: ",null,Level.INFO);
        debugModules.forEach((key, value) -> log(key+" - "+value.name(),null,Level.INFO));
        return level;
    }

    public static Level setDebugLevel(String module, String debugLevel) {
        try {
            return setDebugLevel(module, Level.valueOf(debugLevel.toUpperCase()));
        } catch(IllegalArgumentException ignore) {}
        log("Debug Modules: ",null,Level.INFO);
        debugModules.forEach((key, value) -> log(key+" - "+value.name(),null,Level.INFO));
        return null;
    }

    public static Level setDebugLevel(String module, Level debugLevel) {
        if(module.equalsIgnoreCase("all")) {
            new HashSet<>(debugModules.keySet()).forEach(mod -> setDebugLevel(mod, debugLevel));
        } else {
            debugModules.put(module, debugLevel);
        }
        return debugLevel;
    }

    public static void list(String module) {
        String[] split = module.split("\\.");
        log("List of registered "+split[0]+"s:",null, Level.INFO);
        if(split[0].equalsIgnoreCase("script")) {
            MCMEScripts.getScriptManager().getScripts().entrySet().stream()
                       .filter(entry -> (split.length < 2 || entry.getKey().toLowerCase().startsWith(split[1].toLowerCase())))
                       .forEach(entry -> log(entry.getKey()+(entry.getValue().isActive()?"(active)":"")+": "
                                                               +entry.getValue().getTriggers().size()+" Triggers, "
                                                               +entry.getValue().getEntities().size()+" Entities.",null,Level.INFO));
        } else if(split[0].equalsIgnoreCase("trigger")) {
            MCMEScripts.getScriptManager().getScripts().entrySet().stream()
                       .filter(entry -> (split.length<2 || entry.getKey().startsWith(split[1])))
                       .forEach(entry -> entry.getValue().getTriggers().stream()
                            .filter(trigger -> split.length < 3
                                    || trigger.getClass().getSimpleName().toLowerCase().startsWith(split[2].toLowerCase()))
                            .forEach(trigger -> log(trigger.getClass().getSimpleName()+" "+trigger,null, Level.INFO)));
        }
    }

    public static void verbose(String module, String message) {
        log(module, message, Level.VERBOSE, null);
    }

    public static void info(String module, String message) {
        log(module, message, Level.INFO, null);
    }

    public static void warn(String module, String message) {
        log(module, message, Level.WARNING, null);
    }

    public static void severe(String module, String message) {
        log(module, message, Level.SEVERE, null);
    }

    public static void critical(String module, String message) {
        log(module, message, Level.CRITICAL,null);
    }

    public static void verbose(String module, String message, String script) {
        log(module, message, Level.VERBOSE, script);
    }

    public static void info(String module, String message, String script) {
        log(module, message, Level.INFO, script);
    }

    public static void warn(String module, String message, String script) {
        log(module, message, Level.WARNING, script);
    }

    public static void severe(String module, String message, String script) {
        log(module, message, Level.SEVERE, script);
    }

    public static void critical(String module, String message, String script) {
        log(module, message, Level.CRITICAL,script);
    }

    public static void log(String module, String message, Level debugLevel, String scriptName) {
        String initialModule = ""+module;
        module = module + ".";
        int lastDot = module.lastIndexOf('.');
        do {
            module = module.substring(0,lastDot);
            if(isActive(module, debugLevel)) {
                break;
            }
            lastDot = module.lastIndexOf('.');
        } while(lastDot > 0);
        if(isActive(module,debugLevel) || isActive(module.split("\\.")[0],debugLevel)) {
            log("["+LocalTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_LOCAL_TIME)+" "+initialModule+"] -> " + message, scriptName, debugLevel);
        }
    }

    private static boolean isActive(String module, Level debugLevel) {
        return debugModules.get(module)!=null && debugModules.get(module).getDebugLevel()<=debugLevel.getDebugLevel();
    }

    public static void log(String message, String scriptName, Level level) {
        ChatColor color = ChatColor.GRAY;
        switch (level) {
            case INFO:
                color = ChatColor.WHITE; break;
            case WARNING:
                color = ChatColor.YELLOW; break;
            case SEVERE:
                color = ChatColor.RED; break;
            case CRITICAL:
                color = ChatColor.DARK_RED;
        }
        if(fileDebugScript.filter(scriptName)) {
            writer.println(message);
        }
        if(consoleDebugScript.filter(scriptName)) {
            Logger.getLogger(MCMEScripts.class.getSimpleName()).log(level.getLoggerLevel(), message);
        }
        ChatColor finalColor = color;
        Bukkit.getOnlinePlayers().stream().filter(player -> {
            ScriptFilter playerScriptFilter = playerDebugScripts.get(player.getUniqueId());
            return playerScriptFilter != null && playerScriptFilter.filter(scriptName);
            }).forEach(player -> player.sendMessage(finalColor +message));
    }

    public static void printStackTrace() {
        try {
            throw new Exception("printStackTrace!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
