package com.mcmiddleearth.mcmescripts.quest;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mcmiddleearth.mcmescripts.quest.tags.AbstractTag;
import com.mcmiddleearth.mcmescripts.quest.tags.StringTag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents progress of a party in a quest.
 */
public class QuestData {

    /**
     * Quest name.
     */
    private final String questName;

    /**
     * Timestamp of quest creation, needed for lifetime checks.
     */
    private final long startTime;

    /**
     * Timestamp of last activity, needed for lifetime checks.
     */
    private long lastPlayTime;

    /**
     * List of all enabled stages, ordered by activation time
     */
    private final List<String> stages = new ArrayList<>();

    /**
     * Map of all stored tags related to this quest.
     * mapping: tag.name -> tag
     */
    private final Map<String, AbstractTag<?>> tags = new HashMap<>();

    /**
     * Keys in quest data storage json.
     */
    private static final String KEY_TAGS = "tags",
                                KEY_STAGES = "stages",
                                KEY_START_TIME = "start_time",
                                KEY_LAST_PLAYED = "last_played";

    public QuestData(String questName, String accessStage, long startTime) {
        this.questName = questName;
        this.startTime = startTime;
        if(accessStage!=null) stages.add(accessStage);
    }

    public static QuestData loadQuestData(String questName, JsonObject jsonObject) {
        long startTime = jsonObject.get(KEY_START_TIME).getAsLong();
        QuestData result = new QuestData(questName, null, startTime);
        long lastPlayed = jsonObject.get(KEY_LAST_PLAYED).getAsLong();
        result.setLastPlayTime(lastPlayed);
        jsonObject.get(KEY_STAGES).getAsJsonArray().forEach(stage-> result.addStage(stage.getAsString()));
        jsonObject.get(KEY_TAGS).getAsJsonObject().entrySet().forEach(tag -> {
            result.setTag(AbstractTag.loadTag(tag.getKey(), tag.getValue()));
        });
        return result;
    }

    public void writeJson(JsonWriter writer) {
        try {
            writer.name(questName);
            writer.beginObject();
                writer.name(KEY_START_TIME).value(startTime);
                writer.name(KEY_LAST_PLAYED).value(lastPlayTime);
                writer.name(KEY_STAGES);
                writer.beginArray();
                    for(String stage: stages) writer.value(stage);
                writer.endArray();
                writer.name(KEY_TAGS);
                writer.beginObject();
                    for(Map.Entry<String, AbstractTag<?>> entry: tags.entrySet()) {
                        writer.name(entry.getKey());
                        entry.getValue().writeJson(writer);
                    }
                writer.endObject();
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getQuestName() {
        return questName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public void addStage(String name) {
        stages.add(name);
    }

    public void removeStage(String name) {
        stages.remove(name);
    }

    public List<String> getEnabledStages() {
        return stages;
    }

    public boolean isStageEnabled(String name) {
        return stages.contains(name);
    }

    public void setTag(String name, String value) {
        tags.put(name, new StringTag(name, value));
    }

    public void setTag(AbstractTag<?> tag) {
        tags.put(tag.getName(), tag);
    }

    public void deleteTag(String name) {
        tags.remove(name);
    }

    public boolean hasTag(String name) {
        return tags.containsKey(name);
    }

    private void save() {

    }
}
