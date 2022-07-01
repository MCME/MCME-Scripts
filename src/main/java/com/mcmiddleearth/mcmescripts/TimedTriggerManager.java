package com.mcmiddleearth.mcmescripts;

import com.mcmiddleearth.mcmescripts.trigger.timed.TimedTrigger;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class
TimedTriggerManager {

    public static final int MIN_TRIGGER_CHECK_PERIOD = MCMEScripts.getConfigInt(ConfigKeys.TRIGGER_CHECKER_PERIOD,10);
    private static BukkitTask timedTriggerTask;

    private final Set<TimedTrigger> triggers = new HashSet<>();

    public void startChecker() {
        stopChecker();
        timedTriggerTask = new BukkitRunnable() {
            @Override
            public void run() {
                new HashSet<>(triggers).forEach(TimedTrigger::call);
            }
        }.runTaskTimer(MCMEScripts.getInstance(), 5,
                                                  MIN_TRIGGER_CHECK_PERIOD);
    }

    public void stopChecker() {
        if(timedTriggerTask!=null && !timedTriggerTask.isCancelled()) {
            timedTriggerTask.cancel();
        }
    }

    public void register(TimedTrigger trigger) {
        triggers.add(trigger);
    }

    public void unregister(TimedTrigger trigger) {
        triggers.remove(trigger);
    }
}
