package com.mcmiddleearth.mcmescripts.quest.tags;

public class StringTag extends Tag {

    private String value;

    public StringTag(String name, String value) {
        super(name);
        this.value = value;
    }

    public String getString() {
        return value;
    }

    public void setString(String value) {
        this.value = value;
    }
}
