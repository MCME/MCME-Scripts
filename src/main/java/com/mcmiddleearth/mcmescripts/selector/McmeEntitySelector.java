package com.mcmiddleearth.mcmescripts.selector;

import com.mcmiddleearth.entities.EntitiesPlugin;
import com.mcmiddleearth.entities.entities.McmeEntity;
import com.mcmiddleearth.entities.entities.RealPlayer;
import com.mcmiddleearth.entities.entities.VirtualEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class McmeEntitySelector extends EntitySelector<McmeEntity> {

    public McmeEntitySelector(String selector) throws IndexOutOfBoundsException {
        super(selector);
        //DebugManager.info(Modules.Selector.create(this.getClass()),
        //        "Selector: "+selector);
        //Throwable throwable = new Throwable();
        //throwable.printStackTrace();
    }

    @Override
    public List<McmeEntity> provideTargets(EntitySelectorContext<McmeEntity> selectorContext) {
        if (selectorType == SelectorType.TRIGGER_ENTITY) {
            // Special case to prevent duplicates between entity and player trigger targets
            List<McmeEntity> targets = new ArrayList<>(2);

            McmeEntity entity = selectorContext.getTriggerContext().getEntity();
            Player player = selectorContext.getTriggerContext().getPlayer();

            if (entity != null) targets.add(entity);
            if (player != null) {
                McmeEntity realPlayer = EntitiesPlugin.getEntityServer().getPlayerProvider().getOrCreateMcmePlayer(player);

                if (!realPlayer.equals(entity)) {
                    targets.add(realPlayer);
                }
            }

            return targets;
        }

        List<VirtualEntity> virtualEntities = provideVirtualEntityTargets(selectorContext);
        List<RealPlayer> players = provideMcmePlayerTargets(selectorContext);

        List<McmeEntity> targets = new ArrayList<>(virtualEntities.size() + players.size());
        targets.addAll(virtualEntities);
        targets.addAll(players);

        return targets;
    }
}
