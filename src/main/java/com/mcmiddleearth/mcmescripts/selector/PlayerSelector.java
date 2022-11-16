package com.mcmiddleearth.mcmescripts.selector;

import org.bukkit.entity.Player;

import java.util.List;

public class PlayerSelector extends EntitySelector<Player> {

    public PlayerSelector(String selector) throws IndexOutOfBoundsException {
        super(selector);
        //DebugManager.info(Modules.Selector.create(this.getClass()),
        //        "Selector: "+selector);
    }

    @Override
    public List<Player> provideTargets(EntitySelectorContext<Player> selectorContext) {
        return providePlayerTargets(selectorContext);
    }
}
