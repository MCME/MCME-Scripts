package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.compiler.ScriptCompiler;
import com.mcmiddleearth.mcmescripts.compiler.StageCompiler;
import com.mcmiddleearth.mcmescripts.quest.party.PartyManager;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Manages creation of new instances of a quest.
 * A new instance is created when a party triggers one of the quests access stages.
 */
public class QuestLoader{

    /**
     * Quest name
     */
    private final String questName;

    /**
     * Data file of this quest containing information about entities and events of all stages.
     */
    private final File dataFile;

    /**
     * Set of all access stages that can trigger quest creation.
     */
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
