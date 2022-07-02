package com.mcmiddleearth.mcmescripts.quest.party;

import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.quest.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

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
            createSoloParty(player);
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
//Logger.getGlobal().info("Load party: "+partyId+" "+party);
                    //Check if party has not been disbanded while the player has been offline
                    if (party != null) {
                        parties.add(party);
                        player.addParty(party);
                        party.updateOnlinePlayers();
                    }
                }
            });
            if(player.getParties().isEmpty()) {
//Logger.getGlobal().info("Could not load any parties. Creating solo party!");
                createSoloParty(player);
            } else {
                //read active party from player data file.
                player.setActiveParty(getParty(player.loadActivePartyId()));
                player.save();
            }
        }
    }

    public static void playerLeave(UUID playerId) {
        PartyPlayer player = getOrCreatePartyPlayer(playerId);
        //unload parties where leaving player is only member
        unloadUnusedParties(player);
        player.getParties().clear();
        unloadUnusedPlayer(player);
        new BukkitRunnable(){
            @Override
            public void run() {
                player.getParties().forEach(Party::updateOnlinePlayers);
            }
        }.runTaskLater(MCMEScripts.getInstance(), 2);
    }

    public static void createParty(String name, PartyPlayer founder) {
        Party party = new Party(UUID.randomUUID(), name, founder);
        addPlayerToParty(founder, party);
        founder.setActiveParty(party);
        founder.save();
        parties.add(party);
        party.updateOnlinePlayers();
        party.save();
    }

    public static void disbandParty(UUID partyId) {
        Party party = parties.stream().filter(search -> search.getUniqueId().equals(partyId)).findAny().orElse(null);
        if(party != null) {
            QuestManager.unloadQuests(party);
            parties.remove(party);
            players.forEach(player -> {
                removePlayerFromParty(player, party);
                player.save();
                unloadUnusedPlayer(player);
            });
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
            unloadUnusedPlayer(player);
            if(party.getPartyPlayers().isEmpty()) {
                disbandParty(party.getUniqueId());
            } else {
                party.updateOnlinePlayers();
                party.save();
                unloadUnusedParty(player, party);
            }
            return true;
        }
        return false;
    }

    private static void createSoloParty(PartyPlayer player) {
        createParty(Bukkit.getOfflinePlayer(player.getUniqueId()).getName() + "'s solo", player);
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
            return party;
        }
        return null;
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

    public static Set<PartyPlayer> getPlayers() {
        return players;
    }

    private static void unloadUnusedPlayer(PartyPlayer player) {
        if(parties.stream().noneMatch(party -> party.getPartyPlayers().contains(player))) {
            players.remove(player);
        }
    }

    private static void unloadUnusedParties(PartyPlayer leavingPlayer) {
        leavingPlayer.getParties().forEach(party -> {
                unloadUnusedParty(leavingPlayer, party);
            });
    }

    private static void unloadUnusedParty(PartyPlayer leavingPlayer, Party party) {
        UUID playerId = leavingPlayer.getUniqueId();
        if(party.getPartyPlayers().stream()
                .noneMatch(partyPlayer -> !partyPlayer.getUniqueId().equals(playerId) && partyPlayer.isOnline())) {
            QuestManager.unloadQuests(party);
            parties.remove(party);
        }
    }

    //todo: delete unused parties (long period check)
}
