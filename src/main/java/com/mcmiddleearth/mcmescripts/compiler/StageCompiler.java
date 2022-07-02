package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.quest.StageAccess;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class StageCompiler {

    public static final String  KEY_STAGES          = "stages",
                                KEY_ACCESS          = "access_stage";

    public static JsonObject getStageData(String name, JsonObject questData) {
        JsonObject stages = questData.get(KEY_STAGES).getAsJsonObject();
Logger.getGlobal().info("Compiling stage: "+name);
questData.entrySet().forEach(entry -> Logger.getGlobal().info("Name: "+entry.getKey()+" Value"+entry.getValue()));
        JsonElement stageData = stages.get(name);
        if(stageData!=null && stageData.isJsonObject()) {
            return stageData.getAsJsonObject();
        } else {
            return null;
        }
    }

    public static Set<StageAccess> readAccessStages(JsonObject questData) {
        JsonObject stages = questData.get(KEY_STAGES).getAsJsonObject();
        Set<StageAccess> accessStages = new HashSet<>();
        stages.entrySet().forEach(entry -> {
            String name = entry.getKey();
            JsonObject stageData = entry.getValue().getAsJsonObject();
            JsonElement accessJson = stageData.get(KEY_ACCESS);
            if(accessJson!=null && accessJson.isJsonPrimitive()
               && accessJson.getAsJsonPrimitive().isBoolean() && accessJson.getAsBoolean()) {
                accessStages.add(new StageAccess(name, stageData));
            }
        });
        return accessStages;
    }
}
