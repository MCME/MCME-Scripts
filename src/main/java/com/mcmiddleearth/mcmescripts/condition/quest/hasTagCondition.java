package com.mcmiddleearth.mcmescripts.condition.quest;

import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class hasTagCondition extends QuestCondition {

    private final String tagName;

    public hasTagCondition(String tagName) {
        this.tagName = tagName;
    }

    @Override
    protected boolean test(Quest quest, TriggerContext context) {
        return quest.hasTag(tagName);
    }
}
