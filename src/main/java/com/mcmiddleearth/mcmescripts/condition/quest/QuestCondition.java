package com.mcmiddleearth.mcmescripts.condition.quest;

import com.mcmiddleearth.mcmescripts.condition.Condition;
import com.mcmiddleearth.mcmescripts.quest.Quest;
import com.mcmiddleearth.mcmescripts.quest.Stage;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public abstract class QuestCondition extends Condition {

    @Override
    public final boolean test(TriggerContext context) {
        if(context.getScript() instanceof Stage) {
            return test(((Stage)context.getScript()).getQuest(),context);
        }
        return false;
    }

    protected abstract boolean test(Quest quest, TriggerContext context);

}
