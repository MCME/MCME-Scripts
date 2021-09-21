package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmiddleearth.mcmescripts.selector.*;

import java.util.Optional;

public class SelectorCompiler {

    private static final String KEY_SELECTOR           = "select";

    public static PlayerSelector compilePlayerSelector(JsonObject jsonObject) {
        String selectorData = getSelectorData(jsonObject, KEY_SELECTOR);
        return new PlayerSelector(selectorData);
    }

    public static VirtualEntitySelector compileVirtualEntitySelector(JsonObject jsonObject) {
        String selectorData = getSelectorData(jsonObject, KEY_SELECTOR);
        return new VirtualEntitySelector(selectorData);
    }

    private static String getSelectorData(JsonObject jsonObject, String key) {
        JsonElement selectorJson = jsonObject.get(key);
        if(selectorJson == null) return "@s";
        return selectorJson.getAsString();
    }

    public static Optional<McmeEntitySelector> compileMcmeEntitySelector(JsonObject jsonObject) {
        return compileMcmeEntitySelector(jsonObject,KEY_SELECTOR);
    }

    public static Optional<McmeEntitySelector> compileMcmeEntitySelector(JsonObject jsonObject, String key) {
        String selectorData = getSelectorData(jsonObject, key);
        return Optional.of(new McmeEntitySelector(selectorData));
        /*String selectorData = getSelectorData(jsonObject);
        switch(selectorData.charAt(1)) {
            case 'p':
            case 'a':
            case 'r':
                return Optional.of(compilePlayerSelector(jsonObject));
            case 'e':
            case 's':
            case 'v':
                return Optional.of(compileVirtualEntitySelector(jsonObject));
        }
        return Optional.empty();*/
    }
}
