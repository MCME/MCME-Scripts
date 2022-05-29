package com.mcmiddleearth.mcmescripts.quest;

import com.mcmiddleearth.mcmescripts.party.Party;
import com.mcmiddleearth.mcmescripts.script.Script;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Quest extends Script {

    private String name;

    private QuestData data;
    private Party party;

    //active triggers and entities mapping stage.name -> stage
    private Map<String,Stage> stages;

    public Quest(File file) throws IOException {
        super(file);
    }
}
