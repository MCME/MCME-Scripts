package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.quest.StageAccess;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class StageCompiler {

    public static final String  KEY_STAGES          = "stages",
                                KEY_ACCESS          = "access_stage";

    public static JsonObject getStageData(String name, JsonObject questData) {
        JsonObject stages = questData.get(KEY_STAGES).getAsJsonObject();
        JsonElement stageData = stages.get(name);
        if(stageData.isJsonObject()) {
            return stageData.getAsJsonObject();
        } else {
            return null;
        }
    }

    public static Set<StageAccess> readAccessStages(File dataFile, JsonObject questData) {
        JsonObject stages = questData.get(KEY_STAGES).getAsJsonObject();
        Set<StageAccess> accessStages = new HashSet<>();
        stages.entrySet().forEach(entry -> {
            String name = entry.getKey();
            JsonObject stageData = entry.getValue().getAsJsonObject();
            if(stageData.get(KEY_ACCESS).getAsBoolean()) {
                accessStages.add(new StageAccess(name, dataFile, stageData));
            }
        });
        return accessStages;
    }
}
