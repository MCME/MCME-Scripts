package com.mcmiddleearth.mcmescripts.quest;

import com.mcmiddleearth.mcmescripts.quest.tags.Tag;

import java.util.*;

public class QuestData {

    //system timestamp when the quest began, needed for lifetime checks.
    private long startTime;

    //ordered by activation time
    private final List<Stage> stages = new ArrayList<>();

    //mapping tag.name -> tag
    private final Map<String,Tag> tags = new HashMap<>();
}
