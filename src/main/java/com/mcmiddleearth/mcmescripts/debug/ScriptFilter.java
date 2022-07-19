package com.mcmiddleearth.mcmescripts.debug;

public class ScriptFilter {

    private final Type type;
    private final String selectedScript;

    public static ScriptFilter AllScriptFilter() {
        return new ScriptFilter(Type.ALL,null);
    }

    public static ScriptFilter NoScriptFilter() {
        return new ScriptFilter(Type.NO,null);
    }

    public static ScriptFilter OneScriptFilter(String scriptName) {
        return new ScriptFilter(Type.ONE,scriptName);
    }

    private ScriptFilter(Type type, String selectedScript) {
        this.type = type;
        this.selectedScript = selectedScript;
    }

    public boolean filter(String scriptName) {
        switch (type) {
            case NO:
                return false;
            case ALL:
                return true;
            case ONE:
                return selectedScript.equalsIgnoreCase(scriptName);
        };
        return false;
    }

    public enum Type {
        NO, ALL, ONE
    }
}
