package com.mcmiddleearth.mcmescripts.party;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        //create party objects and load quests
    }

    @EventHandler
    public void playerJoin(PlayerQuitEvent event) {
        //remove party objects and unload quests
    }
}
