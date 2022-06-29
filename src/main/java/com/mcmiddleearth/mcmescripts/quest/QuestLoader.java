package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.compiler.ScriptCompiler;
import com.mcmiddleearth.mcmescripts.compiler.StageCompiler;
import com.mcmiddleearth.mcmescripts.quest.party.PartyManager;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class QuestLoader{

    private final String questName;

    private final File dataFile;

    private Set<StageAccess> accessStages;

    public QuestLoader(File file) throws IOException {
        dataFile = file;
        JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
        assert jsonData != null;
        questName = ScriptCompiler.getName(jsonData).orElse(System.currentTimeMillis()+"_"+Math.random());
        accessStages = StageCompiler.readAccessStages(dataFile, jsonData);
    }

    public void checkQuestCreation() {
        //check for each party if it triggers this quest, then create quest object and add to QuestManager
        PartyManager.getParties().stream()
                .filter(party->!QuestManager.hasActiveQuest(party,questName))
                                                .forEach(party -> {
            for(StageAccess stage: accessStages) {
                if (stage.isTriggered(party)) {
                    QuestManager.addQuest(questName, party);
                    break;
                }
            }
        });
    }

    public String getQuestName() {
        return questName;
    }
}
