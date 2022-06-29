package com.mcmiddleearth.mcmescripts.quest.tags;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class PrimitiveTag<T> extends AbstractTag<T> {

    public PrimitiveTag(String name, T value) {
        super(name, value);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.name(getName()).value(getValue().toString());
    }

}
