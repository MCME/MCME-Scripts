package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.compiler.StageCompiler;
import com.mcmiddleearth.mcmescripts.script.Script;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;

import java.io.File;
import java.io.IOException;

/**
 * Stages are scripts that can be disabled.
 * todo: Entities of a disabled stage are not instantly removed but stop reacting on players and despawn
 * todo: when no party player is in their spawn range
 * todo: after all entities despawned a disabled stage is removed.
 */
public class Stage extends Script {

    /**
     * name of quest this stage belongs to.
     */
    private final Quest quest;

    /**
     * Flag to indicate this stage is disabled.
     */
    private boolean disabled;

    public Stage(Quest quest, String name) throws IOException {
        this(quest, name, getJsonData(name, quest.getDataFile()));
    }

    public Stage(Quest quest, String name, JsonObject jsonData) {
        super(quest.getDataFile(),jsonData);
        this.name = name;
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public JsonObject getJsonData() throws IOException {
        return getJsonData(name, getDataFile());
    }

    static JsonObject getJsonData(String name, File dataFile) throws IOException {
        JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
        assert jsonData != null;
        jsonData = StageCompiler.getStageData(name, jsonData);
        assert  jsonData != null;
        return jsonData;
    }
}
