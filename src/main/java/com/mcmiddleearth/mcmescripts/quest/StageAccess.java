package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.compiler.ConditionCompiler;
import com.mcmiddleearth.mcmescripts.compiler.TriggerCompiler;
import com.mcmiddleearth.mcmescripts.condition.Condition;
import com.mcmiddleearth.mcmescripts.quest.party.Party;
import com.mcmiddleearth.mcmescripts.trigger.Trigger;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class StageAccess {

    private final String name;

    private final Set<Condition> conditions;
    private boolean metAllConditions = true;

    public StageAccess(String name, File dataFile) throws IOException {
        this(name, dataFile, Stage.getJsonData(name, dataFile));
    }

    public StageAccess(String name, File dataFile, JsonObject jsonData) {
        this.name = name;
        conditions = ConditionCompiler.compile(jsonData);
        if(!conditions.isEmpty()) metAllConditions = TriggerCompiler.getMetAllConditions(jsonData);
    }

    public boolean isTriggered(Party party) {
        if(conditions.isEmpty()) return true;
        TriggerContext context = new TriggerContext(new Trigger() {
            @Override
            public void call(TriggerContext context) {}
        }).withParty(party);
        for(Condition condition: conditions) {
            boolean testResult = condition.test(context);
            if(metAllConditions && !testResult) {
                return false;
            } else if(!metAllConditions && testResult) {
                return true;
            }
        }
        return metAllConditions;
    }
}
