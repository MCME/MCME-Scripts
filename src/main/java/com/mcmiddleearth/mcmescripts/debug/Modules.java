package com.mcmiddleearth.mcmescripts.debug;

@SuppressWarnings("rawtypes")
public enum Modules {

    ACTION_EXECUTE      ("action.execute"),
    ACTION_CREATE       ("action.create"),
    CONDITION_TEST      ("condition.test"),
    CONDITION_CREATE    ("condition.create"),
    TRIGGER_CALL        ("trigger.call"),
    TRIGGER_CREATE      ("trigger.create"),
    TRIGGER_REGISTER    ("trigger.register"),
    TRIGGER_UNREGISTER  ("trigger.unregister"),
    SCRIPT_READ         ("script.read"),
    SCRIPT_LOAD         ("script.load"),
    SCRIPT_UNLOAD       ("script.unload"),
    QUEST_READ          ("quest.read"),
    QUEST_CREATE        ("quest.create"),
    QUEST_LOAD          ("quest.load"),
    QUEST_UNLOAD        ("quest.unload"),
    SELECTOR_SELECT     ("selector.select"),
    SELECTOR_CREATE     ("selector.create"),
    LOCATION_CREATE     ("location.create"),
    ITEM_CREATE         ("item.create"),
    COMMAND_EXECUTE     ("command.execute");

    private final String module;

    Modules(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }

    public static class Command {
        public static String execute(Class clazz) {
            return COMMAND_EXECUTE.module+"."+clazz.getSimpleName();
        }
    }

    public static class Action {
        public static String execute(Class clazz) {
            return ACTION_EXECUTE.module+"."+clazz.getSimpleName();
        }

        public static String create(Class clazz) {
            return ACTION_CREATE.module+"."+clazz.getSimpleName();
        }
    }

    public static class Condition {
        public static String test(Class clazz) {
            return CONDITION_TEST.module+"."+clazz.getSimpleName();
        }

        public static String create(Class clazz) {
            return CONDITION_CREATE.module+"."+clazz.getSimpleName();
        }
    }

    public static class Trigger {
        public static String call(Class clazz) {
            return TRIGGER_CALL.module+"."+clazz.getSimpleName();
        }

        public static String create(Class clazz) {
            return TRIGGER_CREATE.module+"."+clazz.getSimpleName();
        }

        public static String register(Class clazz) {
            return TRIGGER_REGISTER.module+"."+clazz.getSimpleName();
        }

        public static String unregister(Class clazz) {
            return TRIGGER_UNREGISTER.module+"."+clazz.getSimpleName();
        }
    }

    public static class Script {
        public static String create(Class clazz) {
            return SCRIPT_READ.module+"."+clazz.getSimpleName();
        }

        public static String load(Class clazz) {
            return SCRIPT_LOAD.module+"."+clazz.getSimpleName();
        }

        public static String unload(Class clazz) {
            return SCRIPT_UNLOAD.module+"."+clazz.getSimpleName();
        }
    }

    public static class Quest {
        public static String create(Class clazz) {
            return QUEST_CREATE.module+"."+clazz.getSimpleName();
        }

        public static String load(Class clazz) {
            return QUEST_LOAD.module+"."+clazz.getSimpleName();
        }

        public static String unload(Class clazz) {
            return QUEST_UNLOAD.module+"."+clazz.getSimpleName();
        }
    }

    public static class QuestLoader {
        public static String create(Class clazz) { return QUEST_READ.module+"."+clazz.getSimpleName(); }
    }

    public static class Selector {
        public static String select(Class clazz) {
            return SELECTOR_SELECT.module+"." + clazz.getSimpleName();
        }

        public static String create(Class clazz) {
            return SELECTOR_CREATE.module+"." + clazz.getSimpleName();
        }

    }

    public static class Location {
        public static String create(Class clazz) { return LOCATION_CREATE.module+"."+clazz.getSimpleName(); }
    }

    public static class Item {
        public static String create(Class clazz) { return ITEM_CREATE.module+"."+clazz.getSimpleName(); }
    }




}
