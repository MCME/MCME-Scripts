package com.mcmiddleearth.mcmescripts.quest;

import com.mcmiddleearth.mcmescripts.quest.party.Party;
import com.mcmiddleearth.mcmescripts.quest.tags.AbstractTag;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Quest {

    private final String name;

    private final QuestData data;
    private final Party party;

    //Map of all loaded stages. mapping stage.name -> stage
    private Map<String,Stage> stages;

    private final File dataFile;

    public Quest(File file, Party party, QuestData data) throws IOException {
        this.name = data.getQuestName();
        dataFile = file;
        this.party = party;
        this.data = data;
        data.setLastPlayTime(System.currentTimeMillis());
        save();
    }

    public void enableStage(String name) {
        if(!stages.containsKey(name)) {
            try {
                stages.put(name, new Stage(this,name));
                data.addStage(name);
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disableStage(String name) {
        if(stages.containsKey(name)) {
            Stage stage = stages.get(name);
            stage.unload();
            stages.remove(name);
            data.removeStage(name);
            save();
            //todo: put in disabled state until stage unloads
        }
    }

    public void checkStages() {
        //check if stages needs loading or unloading
        stages.values().stream().filter(stage -> stage.isTriggered() && !stage.isActive()).forEach(Stage::load);
        stages.values().stream().filter(stage -> !stage.isTriggered() && stage.isActive()).forEach(Stage::unload);
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
