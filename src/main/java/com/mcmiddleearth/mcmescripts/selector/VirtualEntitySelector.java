package com.mcmiddleearth.mcmescripts.selector;

import com.mcmiddleearth.entities.entities.VirtualEntity;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;

import java.util.List;

public class VirtualEntitySelector extends EntitySelector<VirtualEntity> {

    public VirtualEntitySelector(String selector) throws IndexOutOfBoundsException {
        super(selector);
        DebugManager.info(Modules.Selector.create(this.getClass()),
                "Selector: "+selector);
    }

    @Override
    public List<VirtualEntity> provideTargets(EntitySelectorContext<VirtualEntity> selectorContext) {
        return provideVirtualEntityTargets(selectorContext);
    }
}
