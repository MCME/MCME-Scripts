package com.mcmiddleearth.mcmescripts.trigger;

import com.mcmiddleearth.entities.EntitiesPlugin;
import com.mcmiddleearth.entities.entities.McmeEntity;
import com.mcmiddleearth.entities.events.events.McmeEntityEvent;
import com.mcmiddleearth.entities.events.listener.McmeEventListener;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.script.Script;

public abstract class EntitiesEventTrigger extends EventTrigger implements McmeEventListener {

    private final boolean useAllEntities;

    public EntitiesEventTrigger(Action action, boolean useAllEntities) {
        super(action);
        this.useAllEntities = useAllEntities;
    }

    @Override
    public void register(Script script) {
        super.register(script);
        EntitiesPlugin.getEntityServer().registerEvents(MCMEScripts.getInstance(),this);
    }

    @Override
    public void unregister() {
        super.unregister();
        EntitiesPlugin.getEntityServer().unregisterEvents(MCMEScripts.getInstance(),this);
    }

    protected boolean isScriptEntity(McmeEntity entity) {
        if(useAllEntities) return true;
        if(entity == null) return false;
        for(McmeEntity search: getScript().getEntities()) {
            if(search.equals(entity)) return true;
        }
        return false;
    }

    @Override
    public Descriptor getDescriptor() {
        return super.getDescriptor().addLine("Use all entities: "+useAllEntities);
    }
}
