package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonSyntaxException;
import com.mcmiddleearth.mcmescripts.ConfigKeys;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.party.Party;
import com.mcmiddleearth.mcmescripts.script.Script;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QuestManager {

    //Periodically check questLoaders
    private BukkitTask checker;

    private final Map<String, QuestLoader> questLoaders = new HashMap<>();

    //active quests
    private final Map<Party, Set<Quest>> quests = new HashMap<>();

    //inactive quests of online parties
    private final Map<Party, QuestData> questData = new HashMap<>();

    public void startChecker() {
        stopChecker();
        checker = new BukkitRunnable() {
            @Override
            public void run() {
                questLoaders.forEach((name,loader) -> {
                    loader.checkQuestLoading(questData);
                });
            }
        }.runTaskTimer(MCMEScripts.getInstance(),MCMEScripts.getConfigInt(ConfigKeys.START_UP_DELAY,95),
                MCMEScripts.getConfigInt(ConfigKeys.SCRIPT_CHECKER_PERIOD,100));
    }

    public void stopChecker() {
        if(checker!=null && !checker.isCancelled()) {
            checker.cancel();
        }
    }

    public void addQuest(Party party, Quest quest) {
        //add to active quests
    }

}
