package com.mcmiddleearth.mcmescripts.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {

    private boolean requireAllOnline;

    private final List<UUID> players = new ArrayList<>();

    public boolean isOnline() {
        return false;
    }
}
