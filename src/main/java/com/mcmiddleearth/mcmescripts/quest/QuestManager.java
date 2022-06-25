package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonSyntaxException;
import com.mcmiddleearth.mcmescripts.ConfigKeys;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.party.Party;
import com.mcmiddleearth.mcmescripts.party.PartyManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class QuestManager {

    //Periodically check questLoaders
    private BukkitTask checker;

    private static long lastQuestLifetimeCheck = 0L;
    private static long questLifetimeCheckPeriod;

    private static final Map<String, QuestLoader> questLoaders = new HashMap<>();

    //quests of online parties
    private static final Map<Party, Set<Quest>> quests = new HashMap<>();

    private static final File questFolder = new File(MCMEScripts.getInstance().getDataFolder(),"quests");

    public static void readQuests() {
        questLifetimeCheckPeriod = MCMEScripts.getConfigLong(ConfigKeys.QUEST_LIFETIME, 1000*3600*24);
        if(!questFolder.exists()) {
            if(questFolder.mkdir()) {
                Logger.getLogger(MCMEScripts.class.getSimpleName()).info("Quests folder created.");
            }
        }
        for(File file : questFolder.listFiles(((dir, name) -> name.endsWith(".json")))) {
            try {
                QuestLoader questLoader = new QuestLoader(file);
                //Todo: reject double quest names
                questLoaders.put(questLoader.getQuestName(), questLoader);
            } catch (NullPointerException | IOException | IllegalStateException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addQuest(String questName, Party party) {
        addQuest(questName, party, new QuestData(questName, System.currentTimeMillis()));
    }

    public static void addQuest(String questName, Party party, QuestData questData) {
        try {
            Quest quest = new Quest(new File(questFolder,questName+".json"),party, questData);
            quests.computeIfAbsent(party, k -> new HashSet<>()).add(quest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadQuests(Party party) {
        PartyManager.getQuestData(party).forEach(questData -> {
            addQuest(questData.getQuestName(),party,questData);
        });
    }

    public void unloadQuests(Party party) {
        if(quests.containsKey(party)) {
            quests.get(party).forEach(Quest::unload);
            quests.remove(party);
        }
    }

    public void startChecker() {
        stopChecker();
        checker = new BukkitRunnable() {
            @Override
            public void run() {
                //Creation of new quests
                questLoaders.forEach((name,loader) -> {
                    loader.checkQuestCreation();
                });

                //Loading and unloading of stages
                quests.forEach((party,quests) -> {
                    quests.forEach(Quest::checkStages);
                });

                //Delete outdated quests (long period check!!!)
                if(System.currentTimeMillis() > lastQuestLifetimeCheck + questLifetimeCheckPeriod) {

                }


            }
        }.runTaskTimer(MCMEScripts.getInstance(),MCMEScripts.getConfigInt(ConfigKeys.START_UP_DELAY,95),
                MCMEScripts.getConfigInt(ConfigKeys.SCRIPT_CHECKER_PERIOD,100));
    }

    public void stopChecker() {
        if(checker!=null && !checker.isCancelled()) {
            checker.cancel();
        }
    }

    public static Map<Party, Set<Quest>> getAllQuests() {
        return quests;
    }

    public static Set<Quest> getQuests(Party party) {
        return quests.get(party);
    }

    public static boolean hasActiveQuest(Party party, String questName) {
        return quests.containsKey(party)
            && quests.get(party).stream().anyMatch(quest -> quest.getName().equals(questName));
    }

}
