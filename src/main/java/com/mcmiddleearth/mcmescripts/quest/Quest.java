package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.compiler.EntityCompiler;
import com.mcmiddleearth.mcmescripts.compiler.TriggerCompiler;
import com.mcmiddleearth.mcmescripts.party.Party;
import com.mcmiddleearth.mcmescripts.script.Script;
import com.mcmiddleearth.mcmescripts.trigger.Trigger;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Quest extends Script {

    private final QuestData data;
    private final Party party;

    //Map of all loaded stages. mapping stage.name -> stage
    private Map<String,Stage> stages;

    public Quest(File file, Party party, QuestData data) throws IOException {
        super(file);
        this.party = party;
        this.data = data;
    }

    public void checkStages() {
        //check if stages needs loading or unloading
        stages.values().stream().filter(stage -> stage.isTriggered() && !stage.isActive()).forEach(stage -> {
            JsonObject jsonData = null;
            try {
                jsonData = JsonUtils.loadJsonData(getDataFile());
                assert jsonData!=null;
                Set<Trigger> triggers = EntityCompiler.compile(jsonData);
                triggers.forEach(trigger -> trigger.register(this));
                triggers = TriggerCompiler.compile(jsonData);
                triggers.forEach(trigger -> trigger.register(this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        stages.values().stream().filter(stage -> !stage.isTriggered() && stage.isActive()).forEach(stage -> {

        });
    }

    @Override
    public void unload() {
        //todo: unload all stages
        stages.values().forEach(Stage::unload);
    }

    public Party getParty() {
        return party;
    }
}
