package com.mcmiddleearth.mcmescripts.quest.party;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a minecraft player as member of parties.
 * Hold
 */
public class PartyPlayer {

    /**
     * Unique id of represented minecraft player
     */
    private final UUID uuid;

    /**
     * Name of represented minecraft player. Might be outdated if player is not online.
     */
    private String name;

    /**
    * Set of all Paries this player is member of .Filled only for online players!
    */
    private final Set<Party> parties = new HashSet<>();

    /**
     * Party that is chosen by this player to be his active party. Might be null when this player is offline.
     */
    private Party activeParty = null;

    /**
     * Folder to store player data.
     */
    private final static File playerFolder = new File(MCMEScripts.getInstance().getDataFolder(),"players");

    /**
     * File to store data of this player object.
     */
    private final File dataFile;

    /**
     * Keys in player data json file.
     */
    private static final String KEY_ACTIVE = "active",
                                KEY_PARTIES = "parties";

    static {
        if(!playerFolder.exists()) {
            if(playerFolder.mkdir()) {
                Logger.getLogger(MCMEScripts.class.getSimpleName()).info("Player folder created.");
            }
        }
    }

    PartyPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        dataFile = new File(playerFolder, uuid.toString()+".json");
    }

    public void addParty(Party party) {
        parties.add(party);
        if(activeParty == null) {
            activeParty = party;
        }
    }

    public void removeParty(Party party) {
        parties.remove(party);
        if(activeParty.equals(party)) {
            activeParty = parties.stream().findAny().orElse(null);
        }
    }

    public Set<Party> getParties() {
        return parties;
    }

    public Player getOnlinePlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getOnlinePlayer() != null;
    }

    public boolean isMember(Party party) {
        return parties.contains(party);
    }

    public Party getActiveParty() {
        return activeParty;
    }

    public void setActiveParty(Party activeParty) {
        this.activeParty = activeParty;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void updateName() {
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
    }

    public Set<UUID> loadPartyIds() {
        //load party ids from player file
        if(dataFile.exists()) {
            try {
                Set<UUID> partyIds = new HashSet<>();
                JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
                assert jsonData != null;
                jsonData.get(KEY_PARTIES).getAsJsonArray().forEach(party -> {
                    partyIds.add(UUID.fromString(party.getAsString()));
                });
                return partyIds;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptySet();
    }

    public UUID loadActivePartyId() {
        if(dataFile.exists()) {
            try {
                JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
                assert jsonData != null;
                return UUID.fromString(jsonData.get(KEY_ACTIVE).getAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void save() {
        //save player data file
//Logger.getGlobal().info("Save player data file!");
//DebugManager.printStackTrace();
        try(JsonWriter writer = JsonUtils.getGson().newJsonWriter(new FileWriter(dataFile))) {
            writer.beginObject()
                .name(KEY_ACTIVE).value(activeParty!=null?activeParty.getUniqueId().toString():"null")
                .name(KEY_PARTIES).beginArray();
                    for(Party party: parties) { writer.value(party.getUniqueId().toString());}
                writer.endArray();
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyPlayer that = (PartyPlayer) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
