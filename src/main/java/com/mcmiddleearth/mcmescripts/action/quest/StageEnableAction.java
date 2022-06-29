package com.mcmiddleearth.mcmescripts.action.quest;

import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class StageEnableAction extends Action {

    private final Quest quest;
    private final String stageName;

    public StageEnableAction(Quest quest, String stageName) {
        this.quest = quest;
        this.stageName = stageName;
        getDescriptor().indent().addLine("Quest.Stage: "+quest.getName()+"."+stageName).outdent();
    }

    @Override
    protected void handler(TriggerContext context) {
        quest.enableStage(stageName);
    }

}
