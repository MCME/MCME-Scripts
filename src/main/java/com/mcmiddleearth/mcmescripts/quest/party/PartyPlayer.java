package com.mcmiddleearth.mcmescripts.quest.party;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PartyPlayer {

    private final UUID uuid;

    private String name;

    //filled only for online players
    private final Set<Party> parties = new HashSet<>();

    //filled only for online players
    private Party activeParty = null;

    private final static File playerFolder = new File(MCMEScripts.getInstance().getDataFolder(),"players");

    private final File dataFile;

    private static final String KEY_ACTIVE = "active",
                                KEY_PARTIES = "parties";

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
