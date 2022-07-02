package com.mcmiddleearth.mcmescripts.quest;

import com.mcmiddleearth.mcmescripts.quest.party.Party;
import com.mcmiddleearth.mcmescripts.quest.tags.AbstractTag;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a quest of a party of players. If there are two parties doing the same quest at the same time
 * there will be two quest objects. One for each party.
 *
 * A quest consists of one or several stages. Each stage is a script. If a stage is enabled it can be triggered and loaded
 * like any script depending on given conditions.
 */
public class Quest {

    /**
     * Party that is doing this quest.
     */
    private final Party party;

    /**
     * Name of this quest.
     */
    private final String name;

    /**
     * Stores information about enabled stages and stored quest tags.
     */
    private final QuestData data;

    /**
     * Map of all enabled stages. mapping stage.name -> stage
     */
    private final Map<String,Stage> stages = new HashMap<>();

    /**
     * Data file of this quest that contains information about entities and events of all stages
     * IMPORTANT: Not to be confused with quest data which are stored in party data files.
     */
    private final File dataFile;

    public Quest(File file, Party party, QuestData data) throws IOException {
        this.name = data.getQuestName();
        dataFile = file;
        this.party = party;
        this.data = data;
        data.setLastPlayTime(System.currentTimeMillis());
        save();
        data.getEnabledStages().forEach(stageName -> {
            Stage stage = readStage(stageName);
            if(stage!=null) {
                stages.put(stageName, stage);
            }
        });
    }

    private Stage readStage(String stageName) {
        try {
            return new Stage(this,stageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void enableStage(String name) {
        if(!stages.containsKey(name)) {
            Stage stage = readStage(name);
            if(stage!=null) {
                data.addStage(name);
                save();
                stages.put(name, stage);
            }
        }
    }

    public void disableStage(String name) {
        if(stages.containsKey(name)) {
            Stage stage = stages.get(name);
            if(stage.isActive())
                stage.unload();
            stages.remove(name);
            data.removeStage(name);
            save();
            //todo: put in disabled state until stage unloads
        }
    }

    public boolean isStageEnabled(String name) {
        return data.isStageEnabled(name);
    }

    public void checkStages() {
        //check if stages needs loading or unloading
//Logger.getGlobal().info("Check stages of: "+name + " number of stages: "+stages.size());
        stages.values().forEach(stage -> {
            if(stage.isTriggered() && !stage.isActive()) {
                stage.load();
            } else if(!stage.isTriggered() && stage.isActive()) {
                stage.unload();
            }
        });
        //todo: check for entities and triggers which are used in more than one stage
    }

    public void unload() {
        stages.values().forEach(Stage::unload);
        stages.clear();
        data.setLastPlayTime(System.currentTimeMillis());
        save();
    }

    public void setTag(AbstractTag<?> tag) {
        data.setTag(tag);
        save();
    }

    public void deleteTag(String name) {
        data.deleteTag(name);
        save();
    }

    public boolean hasTag(String name) {
        return data.hasTag(name);
    }

    public Party getParty() {
        return party;
    }

    public File getDataFile() {
        return dataFile;
    }

    public String getName() {
        return name;
    }

    public void save() {
        party.save();
    }

    public QuestData getQuestData() {
        return data;
    }


}
