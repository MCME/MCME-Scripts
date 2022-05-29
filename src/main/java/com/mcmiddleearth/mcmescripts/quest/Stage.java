package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.mcmiddleearth.entities.entities.McmeEntity;
import com.mcmiddleearth.mcmescripts.compiler.ConditionCompiler;
import com.mcmiddleearth.mcmescripts.compiler.TriggerCompiler;
import com.mcmiddleearth.mcmescripts.condition.Condition;
import com.mcmiddleearth.mcmescripts.party.Party;
import com.mcmiddleearth.mcmescripts.trigger.Trigger;

import java.util.HashSet;
import java.util.Set;

public class Stage {

    private String name;

    private final Set<Condition> conditions;
    private boolean metAllConditions = true;

    private final Set<Trigger> triggers = new HashSet<>();
    private final Set<McmeEntity> entities = new HashSet<>();

    public Stage(String name, JsonObject jsonData) {
        this.name = name;
        conditions = ConditionCompiler.compile(jsonData);
        if(!conditions.isEmpty()) metAllConditions = TriggerCompiler.getMetAllConditions(jsonData);
    }

    public boolean isTriggered(Party party) {

    }
}
