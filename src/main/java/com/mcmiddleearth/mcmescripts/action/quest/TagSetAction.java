package com.mcmiddleearth.mcmescripts.action.quest;

import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.quest.tags.AbstractTag;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class TagSetAction extends QuestAction {

    private final AbstractTag<?> tag;

    public TagSetAction(AbstractTag<?> tag) {
        this.tag = tag;
    }

    @Override
    protected void handler(Quest quest, TriggerContext context) {
        quest.setTag(tag);
    }
}
