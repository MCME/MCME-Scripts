package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonSyntaxException;
import com.mcmiddleearth.mcmescripts.ConfigKeys;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.quest.party.Party;
import com.mcmiddleearth.mcmescripts.quest.party.PartyManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages creation, deletion, loading and unloading of quests.
 */
public class QuestManager {

    /**
     * Periodically check:tLoaders
     * - creation of quest instances
     * - loading and unloading of quest stages
     */
    private static BukkitTask checker;

    /**
     * Timestamp of last lifetime check.
     */
    private static long lastQuestLifetimeCheck = 0L;

    /**
     * Period of quest lifetime checks.
     */
    private static long questLifetimeCheckPeriod;

    /**
     * Map of all quest loaders, one for each quest.
     * mapping: Quest name -> Quest loader
     */
    private static final Map<String, QuestLoader> questLoaders = new HashMap<>();

    /**
     * Map of all quest instance of all loaded parties
     * mapping: quest -> Set of all quests this party is doing.
     */
    private static final Map<Party, Set<Quest>> quests = new HashMap<>();

    /**
     * Folder for storing quest information (entities and events of all stages)
     */
    private static final File questFolder = new File(MCMEScripts.getInstance().getDataFolder(),"quests");

    public static void readQuests() {
        removeQuests();
        questLifetimeCheckPeriod = MCMEScripts.getConfigLong(ConfigKeys.QUEST_LIFETIME, 1000*3600*24);
        if(!questFolder.exists()) {
            if(questFolder.mkdir()) {
                Logger.getLogger(MCMEScripts.class.getSimpleName()).info("Quest folder created.");
            }
        }
        for(File file : Objects.requireNonNull(questFolder.listFiles(((dir, name) -> name.endsWith(".json"))))) {
            try {
Logger.getGlobal().info("create quest loader: "+file.getName());
                QuestLoader questLoader = new QuestLoader(file);
                //Todo: reject double quest names
                questLoaders.put(questLoader.getQuestName(), questLoader);
            } catch (NullPointerException | IOException | IllegalStateException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        PartyManager.getParties().forEach(party -> {
            Party.PartyData partyData = Party.loadPartyData(party.getUniqueId());
            if(partyData!=null) {
                loadQuests(party, partyData);
            }
        });
    }

    public static void addQuest(String questName, String accessStage, Party party) {
        DebugManager.info(Modules.Quest.create(QuestManager.class),
                          new Descriptor("Quest: "+questName).indent()
                                .addLine("Access stage: "+accessStage)
                                .addLine("Party: "+party.getName()).print(""));
        addQuest(party, new QuestData(questName, accessStage, System.currentTimeMillis()));
    }

    private static void addQuest(Party party, QuestData questData) {
        try {
            Quest quest = new Quest(new File(questFolder,questData.getQuestName()+".json"),party, questData);
            quests.computeIfAbsent(party, k -> new HashSet<>()).add(quest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadQuests(Party party, Party.PartyData partyData) {
        partyData.questData.forEach(questData -> {
            addQuest(party,questData);
        });
    }

    public static void removeQuests() {
        Set<Party> parties = new HashSet<>(quests.keySet());
        parties.forEach(QuestManager::unloadQuests);
        quests.clear();
        questLoaders.clear();
    }

    public static void unloadQuests(Party party) {
        if(quests.containsKey(party)) {
            quests.get(party).forEach(Quest::unload);
            quests.remove(party);
        }
    }

    public static void startChecker() {
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
                    //todo: check if party is active
                    quests.forEach(Quest::checkStages);
                });

                //todo: Delete outdated quests (long period check!!!)
                if(System.currentTimeMillis() > lastQuestLifetimeCheck + questLifetimeCheckPeriod) {

                    lastQuestLifetimeCheck = System.currentTimeMillis();
                }

Logger.getGlobal().info("parties: "+PartyManager.getParties().size());
Logger.getGlobal().info("players: "+PartyManager.getPlayers().size());
PartyManager.getParties().forEach(party -> {
    Logger.getGlobal().info(party.getName());
    Logger.getGlobal().info("Members: "+party.getPartyPlayers().size());
    party.getPartyPlayers().forEach(player -> Logger.getGlobal().info(player.getName()+" online: "+player.isOnline()+" parties: "+player.getParties().size()));
});


            }
        }.runTaskTimer(MCMEScripts.getInstance(), 1,
                       MCMEScripts.getConfigInt(ConfigKeys.SCRIPT_CHECKER_PERIOD,100));
    }

    public static void stopChecker() {
        if(checker!=null && !checker.isCancelled()) {
            checker.cancel();
        }
    }

    public static Map<Party, Set<Quest>> getAllQuests() {
        return quests;
    }

    public static Set<Quest> getQuests(Party party) {
        return quests.getOrDefault(party, Collections.emptySet());
    }

    public static boolean hasActiveQuest(Party party, String questName) {
        return quests.containsKey(party)
            && quests.get(party).stream().anyMatch(quest -> quest.getName().equals(questName));
    }

}
