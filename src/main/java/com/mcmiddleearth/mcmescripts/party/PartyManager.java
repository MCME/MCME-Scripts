package com.mcmiddleearth.mcmescripts.party;

import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.quest.QuestData;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class PartyManager {

    private final static Set<Party> onlineParties = new HashSet<>();

    private static final File partiesFolder = new File(MCMEScripts.getInstance().getDataFolder(),"parties");

    public static Set<Party> getOnlineParties() {
        return onlineParties;
    }

    public static Set<QuestData> getQuestData(Party party) {

    }

    //todo: remove unused parties (long period check)
}
