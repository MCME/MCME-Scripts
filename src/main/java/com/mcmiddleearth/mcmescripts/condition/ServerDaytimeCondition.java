package com.mcmiddleearth.mcmescripts.condition;

import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ServerDaytimeCondition extends Condition{

    private final long startTick, endTick;

    private World world;

    private boolean negate;

    public ServerDaytimeCondition(World world, long startTick, long endTick, boolean negate) {
        this.startTick = startTick;
        this.endTick = endTick;
        this.world = world;
        this.negate = negate;
    }

    @Override
    public boolean test(TriggerContext context) {
        long time = world.getTime();
        context.getDescriptor().add(super.getDescriptor()).indent()
               .addLine("Now: "+time);
        boolean result;
        if(negate) {
            result = !(startTick < time && time < endTick);
        } else {
            result = startTick < time && time < endTick;
        }
        context.getDescriptor().addLine("Test result: "+result).outdent();
        return result;
    }

    public Descriptor getDescriptor() {
        return super.getDescriptor().indent()
                .addLine("World: "+world)
                .addLine("Negate: "+negate)
                .addLine("Start: "+startTick)
                .addLine("End: "+endTick)
                .outdent();
    }

}
