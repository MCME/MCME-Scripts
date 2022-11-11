package com.mcmiddleearth.mcmescripts.trigger.virtual;

import com.google.common.base.Joiner;
import com.mcmiddleearth.entities.events.events.virtual.VirtualEntityTalkEvent;
import com.mcmiddleearth.entities.events.handler.EntityEventHandler;
import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.trigger.EntitiesEventTrigger;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class VirtualEntityTalkTrigger extends EntitiesEventTrigger {

    public VirtualEntityTalkTrigger(Action action, boolean useAllEntities) {
        super(action, useAllEntities);
        //DebugManager.info(Modules.Trigger.create(this.getClass()),
        //        "Action: " + (action!=null?action.getClass().getSimpleName():null));
    }

    @SuppressWarnings("unused")
    @EntityEventHandler
    public void onEntityTalk(VirtualEntityTalkEvent event) {
        if(isScriptEntity(event.getVirtualEntity())) {
            TriggerContext context = new TriggerContext(this)
                    .withEntity(event.getVirtualEntity())
                    .withMessage(Joiner.on("\n").join(event.getLayout().getLines()));
            call(context);
            //DebugManager.verbose(Modules.Trigger.call(this.getClass()),
            //        "Entity: "+context.getEntity());
        }
    }
}
