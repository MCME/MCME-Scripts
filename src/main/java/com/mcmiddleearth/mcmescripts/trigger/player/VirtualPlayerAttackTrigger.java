package com.mcmiddleearth.mcmescripts.trigger.player;

import com.mcmiddleearth.entities.events.events.player.VirtualPlayerAttackEvent;
import com.mcmiddleearth.entities.events.handler.EntityEventHandler;
import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.trigger.EntitiesEventTrigger;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class VirtualPlayerAttackTrigger extends EntitiesEventTrigger {

    public VirtualPlayerAttackTrigger(Action action, boolean useAllEntities) {
        super(action, useAllEntities);
        //DebugManager.info(Modules.Trigger.create(this.getClass()),
        //        "Action: " + (action!=null?action.getClass().getSimpleName():null));
    }

    @SuppressWarnings("unused")
    @EntityEventHandler
    public void playerJoin(VirtualPlayerAttackEvent event) {
        if(isScriptEntity(event.getVirtualEntity())) {
            TriggerContext context = new TriggerContext(this)
                    .withPlayer(event.getPlayer().getBukkitPlayer())
                    .withEntity(event.getVirtualEntity());
            call(context);
            //DebugManager.verbose(Modules.Trigger.call(this.getClass()),
            //        "Player: " + event.getPlayer().getName() + " Entity: " + context.getEntity().getName());
        }
    }

}
