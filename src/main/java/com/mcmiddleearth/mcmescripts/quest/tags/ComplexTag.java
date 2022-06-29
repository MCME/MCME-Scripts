package com.mcmiddleearth.mcmescripts.quest.tags;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ComplexTag<T> extends AbstractTag<T> {
    public ComplexTag(String name, T value) {
        super(name, value);
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        throw new UnsupportedEncodingException("Not yet implemented!");
    }
}
