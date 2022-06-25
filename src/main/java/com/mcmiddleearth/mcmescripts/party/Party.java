package com.mcmiddleearth.mcmescripts.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Party {

    private boolean requireAllOnline;

    private UUID uuid;

    private String name;

    private final List<UUID> players = new ArrayList<>();

    public boolean isOnline() {
        return false;
    }

    public Collection<Player> getOnlinePlayers() {
        return players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }
}
