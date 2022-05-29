package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.compiler.ScriptCompiler;
import com.mcmiddleearth.mcmescripts.party.Party;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuestLoader{

    private final String questName;

    private final File dataFile;

    private final Set<Stage> stages = new HashSet<>();

    public QuestLoader(File file) throws IOException {
        dataFile = file;
        JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
        assert jsonData != null;
        questName = ScriptCompiler.getName(jsonData).orElse(System.currentTimeMillis()+"_"+Math.random());
        stages = StageCompiler.compile(jsonData);
    }

    public void checkQuestLoading(Map<Party, QuestData> questData) {
        //check for each party if it triggers this quest, then create quest object and add to QuestManager
    }
}
