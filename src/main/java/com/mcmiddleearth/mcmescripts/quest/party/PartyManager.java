package com.mcmiddleearth.mcmescripts.quest.party;

import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.quest.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The party manager handles all operations on parties and their members.
 * Party objects and also PartyPlayer objects are unique. The party manager ensures there are no two objects
 * representing the same party or player.
 *
 * Handled events:
 *
 * Player joins server:
 * - Where required: Create PartyPlayer object for joining player
 * - If player has no party yet:
 *   - Create solo party
 * - If player already has one or more parties:
 *   - If required: Load party objects of all parties the joining player is member of.
 * - Fill set of parties in PartyPlayer object of joining player.
 *
 * Player leaves server:
 * -
 */
public class PartyManager {

    /**
     * Set of all parties with at least one member being online.
     */
    private final static Set<Party> parties = new HashSet<>();

    /**
     * Set of all loaded party players
     */
    private final static Set<PartyPlayer> players = new HashSet<>();

    public static Set<Party> getParties() {
        return parties;
    }

    public static Party getParty(String name) {
        return parties.stream().filter(party->party.getName().equals(name)).findAny().orElse(null);
    }

    public static Party getParty(UUID uniqueId) {
        return parties.stream().filter(party->party.getUniqueId().equals(uniqueId)).findAny().orElse(null);
    }

    public static void playerJoin(UUID playerId) {
        PartyPlayer player = getOrCreatePartyPlayer(playerId);
        player.updateName();
        Set<UUID> partyIds = player.loadPartyIds();
        if(partyIds.isEmpty()) {
            //Create a new party for solo questing
            Party party = new Party(playerId, Bukkit.getOfflinePlayer(playerId).getName() + "'s solo", player);
            parties.add(party);
            addPlayerToParty(player, party);
            player.setActiveParty(party);
            party.updateOnlinePlayers();
            //QuestManager.loadQuests(party); there can't be any quests as the party is new
            //player.addParty(party);
        } else {
            //Check if all parties the joining player is part of do exists. Create missing party objects.
            partyIds.forEach(partyId -> {
                Party party = parties.stream().filter(search -> search.getUniqueId().equals(partyId)).findAny().orElse(null);
                if(party != null) {
                    //party already loaded
                    player.addParty(party);
                    party.updateOnlinePlayers();
                } else {
                    //party not yet loaded
                    party = loadParty(partyId);
                    //Check if party has not been disbanded while the player has been offline
                    if (party != null) {
                        parties.add(party);
                        player.addParty(party);
                        party.updateOnlinePlayers();
                    } else {
                        //todo: remove ids of disbanded parties from player data file.
                    }
                }
            });
            //read active party from player data file.
            player.setActiveParty(getParty(player.loadActivePartyId()));
        }
        //save player data to add new party membership or remove membership of disbanded party
        player.save();
    }

    public static void playerLeave(UUID playerId) {
        PartyPlayer player = getOrCreatePartyPlayer(playerId);
        player.getParties().stream()
                .filter(party -> party.getPartyPlayers().stream()
                        .noneMatch(partyPlayer -> !partyPlayer.getUniqueId().equals(playerId) && partyPlayer.isOnline()))
                .forEach(party -> {
            parties.remove(party);
            QuestManager.unloadQuests(party);
        });
        players.remove(player);
        player.getParties().clear();
        new BukkitRunnable(){
            @Override
            public void run() {
                player.getParties().forEach(Party::updateOnlinePlayers);
            }
        }.runTaskLater(MCMEScripts.getInstance(), 2);
    }

    private static Party loadParty(UUID partyId) {
        //load party data from party file and add all players and return null if file doesn't exist
        Party.PartyData partyData = Party.loadPartyData(partyId);
        if(partyData!=null) {
            Party party = new Party(partyId, partyData.name, getOrCreatePartyPlayer(partyData.founder));
            partyData.members.forEach(memberId -> {
                PartyPlayer player = getOrCreatePartyPlayer(memberId);
                party.addPlayer(player);
            });
            QuestManager.loadQuests(party, partyData);
        }
        return null;
    }

    public static void createParty(String name, PartyPlayer founder) {
        Party party = new Party(UUID.randomUUID(), name, founder);
        addPlayerToParty(founder, party);
        founder.setActiveParty(party);
        parties.add(party);
        party.updateOnlinePlayers();
        party.save();
    }

    public static void disbandParty(UUID partyId) {
        Party party = parties.stream().filter(search -> search.getUniqueId().equals(partyId)).findAny().orElse(null);
        if(party != null) {
            players.forEach(player -> {
                removePlayerFromParty(player, party);
                player.save();
            });
            parties.remove(party);
            party.deleteFile();
        }
    }

    public static boolean joinParty(PartyPlayer player, Party party) {
        if(!player.isMember(party)) {
            addPlayerToParty(player, party);
            player.setActiveParty(party);
            party.updateOnlinePlayers();
            player.save();
            party.save();
            return true;
        }
        return false;
    }

    public static boolean leaveParty(PartyPlayer player, Party party) {
        if(player.isMember(party)) {
            removePlayerFromParty(player, party);
            player.save();
            if(party.getPartyPlayers().isEmpty()) {
                disbandParty(party.getUniqueId());
            } else {
                party.updateOnlinePlayers();
                party.save();
            }
            return true;
        }
        return false;
    }

    private static void addPlayerToParty(PartyPlayer player, Party party) {
        party.addPlayer(player);
        player.addParty(party);
    }

    private static void removePlayerFromParty(PartyPlayer player, Party party) {
        party.removePlayer(player);
        player.removeParty(party);
    }

    public static PartyPlayer getOrCreatePartyPlayer(UUID playerId) {
        PartyPlayer partyPlayer = players.stream().filter(player->player.getUniqueId().equals(playerId)).findAny().orElse(null);
        if (partyPlayer == null) {
            partyPlayer = new PartyPlayer(playerId, Bukkit.getOfflinePlayer(playerId).getName());
            players.add(partyPlayer);
        }
        return partyPlayer;
    }

    //todo: remove unused parties (long period check)
}
