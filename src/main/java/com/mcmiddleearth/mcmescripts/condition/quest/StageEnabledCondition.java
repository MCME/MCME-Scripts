package com.mcmiddleearth.mcmescripts.condition.quest;

import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class StageEnabledCondition extends QuestCondition{

    private final String stage;

    public StageEnabledCondition(String stage) {
        this.stage = stage;
    }

    @Override
    protected boolean test(Quest quest, TriggerContext context) {
        return quest.isStageEnabled(stage);
    }
}
