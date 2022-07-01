package com.mcmiddleearth.mcmescripts.action.quest;

import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class TagDeleteAction extends QuestAction {

    private final String tagName;

    public TagDeleteAction(String tagName) {
        this.tagName = tagName;
    }

    @Override
    protected void handler(Quest quest, TriggerContext context) {
        quest.deleteTag(tagName);
    }
}
