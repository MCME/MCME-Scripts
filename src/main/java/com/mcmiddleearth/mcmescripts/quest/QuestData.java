package com.mcmiddleearth.mcmescripts.quest;

import com.mcmiddleearth.mcmescripts.quest.tags.Tag;

import java.util.*;

public class QuestData {

    private final String questName;

    //system timestamp when the quest began, needed for lifetime checks.
    private final long startTime;

    //ordered by activation time
    private final List<String> stages = new ArrayList<>();

    //mapping tag.name -> tag
    private final Map<String,Tag> tags = new HashMap<>();

    public QuestData(String questName, long startTime) {
        this.questName = questName;
        this.startTime = startTime;
    }

    public String getQuestName() {
        return questName;
    }

    public long getStartTime() {
        return startTime;
    }
}
