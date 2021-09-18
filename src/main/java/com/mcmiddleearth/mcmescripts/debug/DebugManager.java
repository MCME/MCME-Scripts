package com.mcmiddleearth.mcmescripts.debug;

import com.mcmiddleearth.mcmescripts.MCMEScripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class DebugManager {

    private static final Set<String> debugModules = new HashSet<>();

    private static final File logFile = new File(MCMEScripts.getInstance().getDataFolder(),"debug.txt");

    private static PrintWriter writer;

    public static void open() {
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

    public static void close() {
        writer.close();
    }

    public static void toggleDebug(String module) {
        debug(module,!debugModules.contains(module));
    }

    public static void debug(String module, String enable) {
        debug(module, enable.equalsIgnoreCase("true"));
        log("Active Debug Modules: ");
        debugModules.forEach(DebugManager::log);
    }

    public static void debug(String module, boolean enable) {
        if(module.equalsIgnoreCase("all")) {
            Arrays.stream(Modules.values()).forEach(mod -> debug(mod.getModule(), true));
        } else if(module.equalsIgnoreCase("none")) {
                Arrays.stream(Modules.values()).forEach(mod -> debug(mod.getModule(),false));
        } else {
            if (enable) {
                debugModules.add(module);
            } else {
                debugModules.remove(module);
            }
        }
    }

    public static void list(String module) {
        String[] split = module.split("\\.");
        log("List of registered "+split[0]+"s:");
        if(split[0].equalsIgnoreCase("script")) {
            MCMEScripts.getScriptManager().getScripts().entrySet().stream()
                       .filter(entry -> (split.length < 2 || entry.getKey().toLowerCase().startsWith(split[1].toLowerCase())))
                       .forEach(entry -> log(entry.getKey()+(entry.getValue().isActive()?"(active)":"")+": "
                                                               +entry.getValue().getTriggers().size()+" Triggers, "
                                                               +entry.getValue().getEntities().size()+" Entities."));
        } else if(split[0].equalsIgnoreCase("trigger")) {
            MCMEScripts.getScriptManager().getScripts().entrySet().stream()
                       .filter(entry -> (split.length<2 || entry.getKey().startsWith(split[1])))
                       .forEach(entry -> entry.getValue().getTriggers().stream()
                            .filter(trigger -> split.length < 3
                                    || trigger.getClass().getSimpleName().toLowerCase().startsWith(split[2].toLowerCase()))
                            .forEach(trigger -> log(trigger.getClass().getSimpleName()+" "+trigger.toString())));
        }
    }

    public static void log(String module, String message) {
        String initialModule = ""+module;
        module = module + ".";
        int lastDot = module.lastIndexOf('.');
        do {
            module = module.substring(0,lastDot);
            //log(module);
            if(debugModules.contains(module)) {
                //log("Found: "+module);
                break;
            }
            lastDot = module.lastIndexOf('.');
        } while(lastDot > 0);
        //log("Final module: "+module);
        if(debugModules.contains(module) || debugModules.contains(module.split("\\.")[0])) {
            //log("found final module.");
            log(initialModule + " -> " + message);
        }
    }

    public static void log(String message) {
        Logger.getLogger(MCMEScripts.class.getSimpleName()).info(message);
        writer.println(message);
    }

}
