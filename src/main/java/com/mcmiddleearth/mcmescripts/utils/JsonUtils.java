package com.mcmiddleearth.mcmescripts.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mcmiddleearth.entities.ai.movement.EntityBoundingBox;
import com.mcmiddleearth.entities.api.VirtualEntityFactory;
import com.mcmiddleearth.entities.api.VirtualEntityGoalFactory;
import com.mcmiddleearth.entities.entities.attributes.VirtualEntityAttributeInstance;
import com.mcmiddleearth.entities.entities.composite.bones.SpeechBalloonLayout;
import com.mcmiddleearth.entities.json.*;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonUtils {

    private static final Gson gson;

    static {
        gson = getGsonBuilder().create();
    }

    public static JsonObject loadJsonData(File dataFile) throws IOException {
        try (FileReader reader = new FileReader(dataFile)) {
            JsonElement element =  new JsonParser().parse(new JsonReader(reader));
            if(element instanceof JsonObject) {
                return element.getAsJsonObject();
            }
        }
        return null;
    }

    private static GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setLenient();
    }

    public static Gson getGson() {
        return gson;
    }
}
