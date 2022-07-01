package com.mcmiddleearth.mcmescripts.action.quest;

import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class StageDisableAction extends QuestAction {

    private final String stageName;

    public StageDisableAction(String stageName) {
        this.stageName = stageName;
        getDescriptor().indent().addLine("Stage: "+stageName).outdent();
    }

    @Override
    protected void handler(Quest quest, TriggerContext context) {
        quest.disableStage(stageName);
    }

}
