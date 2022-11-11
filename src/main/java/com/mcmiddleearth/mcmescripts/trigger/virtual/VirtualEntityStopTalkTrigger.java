package com.mcmiddleearth.mcmescripts.trigger.virtual;

import com.mcmiddleearth.entities.events.events.virtual.VirtualEntityStopTalkEvent;
import com.mcmiddleearth.entities.events.handler.EntityEventHandler;
import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.trigger.EntitiesEventTrigger;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class
VirtualEntityStopTalkTrigger extends EntitiesEventTrigger {

    public VirtualEntityStopTalkTrigger(Action action, boolean useAllEntities) {
        super(action, useAllEntities);
        //DebugManager.info(Modules.Trigger.create(this.getClass()),
        //         "Action: " + (action!=null?action.getClass().getSimpleName():null));
    }

    @SuppressWarnings("unused")
    @EntityEventHandler
    public void onEntityStopTalk(VirtualEntityStopTalkEvent event) {
        if(isScriptEntity(event.getVirtualEntity())) {
            TriggerContext context = new TriggerContext(this)
                    .withEntity(event.getVirtualEntity());
            call(context);
            //DebugManager.verbose(Modules.Trigger.call(this.getClass()),
            //        "Entity: "+context.getEntity());
        }
    }
}
