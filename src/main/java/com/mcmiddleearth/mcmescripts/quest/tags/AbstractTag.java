package com.mcmiddleearth.mcmescripts.quest.tags;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public abstract class AbstractTag<T> {

    private T value;

    private final String name;

    public AbstractTag(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() { return value; }

    public void setValue(T value) {
        this.value = value;
    }

    public static AbstractTag<?> loadTag(String name, JsonElement tagData) {
        if(tagData.isJsonPrimitive()) {
            if(tagData.getAsJsonPrimitive().isNumber()) {
                try { return new IntegerTag(name, tagData.getAsInt());
                } catch (NumberFormatException ignore) {}
                try { return new LongTag(name, tagData.getAsLong());
                } catch (NumberFormatException ignore) {}
                try { return new DoubleTag(name, tagData.getAsDouble());
                } catch (NumberFormatException ignore) {}
            }
            return new StringTag(name, tagData.getAsString());
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public abstract void writeJson(JsonWriter writer) throws IOException;
}
