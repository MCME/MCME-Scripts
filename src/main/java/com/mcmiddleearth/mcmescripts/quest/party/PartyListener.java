package com.mcmiddleearth.mcmescripts.quest.party;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        //create party objects and load quests
        PartyManager.playerJoin(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void playerJoin(PlayerQuitEvent event) {
        //remove party objects and unload quests
        PartyManager.playerLeave(event.getPlayer().getUniqueId());
    }
}
