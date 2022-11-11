package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcmiddleearth.mcmescripts.TimedTriggerManager;
import com.mcmiddleearth.mcmescripts.action.Action;
import com.mcmiddleearth.mcmescripts.condition.Condition;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.selector.McmeEntitySelector;
import com.mcmiddleearth.mcmescripts.trigger.DecisionTreeTrigger;
import com.mcmiddleearth.mcmescripts.trigger.ExternalTrigger;
import com.mcmiddleearth.mcmescripts.trigger.Trigger;
import com.mcmiddleearth.mcmescripts.trigger.player.PlayerJoinTrigger;
import com.mcmiddleearth.mcmescripts.trigger.player.PlayerQuitTrigger;
import com.mcmiddleearth.mcmescripts.trigger.player.PlayerTalkTrigger;
import com.mcmiddleearth.mcmescripts.trigger.player.VirtualPlayerAttackTrigger;
import com.mcmiddleearth.mcmescripts.trigger.timed.*;
import com.mcmiddleearth.mcmescripts.trigger.virtual.AnimationChangeTrigger;
import com.mcmiddleearth.mcmescripts.trigger.virtual.GoalFinishedTrigger;
import com.mcmiddleearth.mcmescripts.trigger.virtual.VirtualEntityStopTalkTrigger;
import com.mcmiddleearth.mcmescripts.trigger.virtual.VirtualEntityTalkTrigger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TriggerCompiler {

    public static final String  KEY_TRIGGER         = "event",
                                KEY_TRIGGER_ARRAY   = "events",

                                KEY_TYPE            = "type",
                                KEY_TIME            = "time",
                                KEY_PERIOD          = "period",
                                KEY_THEN            = "then",
                                KEY_ELSE            = "else",
                                KEY_CALL_ONCE       = "call_once",
                                KEY_LOCATION        = "location",
                                KEY_PLAYER          = "player",
                                KEY_ENTITY          = "entity",
                                KEY_CENTER          = "center",
                                KEY_MET_ALL_CONDITIONS  = "met_all_conditions",
                                KEY_NAME                = "name",
                                KEY_CURRENT_ANIMATION   = "current_animation",
                                KEY_NEXT_ANIMATION      = "next_animation",
                                KEY_PROCESS             = "process",
                                KEY_USE_ALL_ENTITIES    = "use_all_entities",

                                VALUE_REAL_TIMED_TRIGGER            = "real_timed",
                                VALUE_REAL_PERIODIC_TRIGGER         = "real_periodic",
                                VALUE_SERVER_TIMED_TRIGGER          = "server_timed",
                                VALUE_SERVER_PERIODIC_TRIGGER       = "server_periodic",
                                VALUE_PLAYER_TALK_TRIGGER           = "player_talk",
                                VALUE_PLAYER_JOIN_TRIGGER           = "player_join",
                                VALUE_PLAYER_QUIT_TRIGGER           = "player_quit",
                                VALUE_PLAYER_VIRTUAL_ATTACK_TRIGGER = "player_virtual_attack",
                                VALUE_VIRTUAL_TALK_TRIGGER              = "virtual_talk",
                                VALUE_VIRTUAL_STOP_TALK_TRIGGER         = "virtual_stop_talk",
                                VALUE_GOAL_FINISHED_TRIGGER             = "goal_finished",
                                VALUE_ANIMATION_CHANGE_TRIGGER          = "animation_change",
                                VALUE_SELECTION_TRIGGER                 = "selection",
                                VALUE_EXTERNAL_TRIGGER                  = "external";

    public static Set<Trigger> compile(JsonObject jsonData) {
        JsonElement triggerData = jsonData.get(KEY_TRIGGER);
        Set<Trigger> triggers = new HashSet<>(compileTriggers(triggerData));
        triggerData = jsonData.get(KEY_TRIGGER_ARRAY);
        triggers.addAll(compileTriggers(triggerData));
        return triggers;
    }

    private static Set<Trigger> compileTriggers(JsonElement triggerData) {
        Set<Trigger> triggers = new HashSet<>();
        if(triggerData == null) return triggers;
        if(triggerData.isJsonArray()) {
            for(int i = 0; i< triggerData.getAsJsonArray().size(); i++) {
                compileTrigger(triggerData.getAsJsonArray().get(i).getAsJsonObject()).ifPresent(triggers::add);
            }
        } else {
            compileTrigger(triggerData.getAsJsonObject()).ifPresent(triggers::add);
        }
        return triggers;
    }

    private static Optional<Trigger> compileTrigger(JsonObject jsonObject) {
        JsonElement type = jsonObject.get(KEY_TYPE);
        if(type==null) {
            DebugManager.warn(Modules.Trigger.create(TriggerCompiler.class),"Can't compile trigger. Missing trigger type.");
            return Optional.empty();
        }

        DecisionTreeTrigger trigger = null;
        switch(type.getAsString()) {
            case VALUE_REAL_TIMED_TRIGGER:
                JsonElement time = jsonObject.get(KEY_TIME);
                if (time != null && time.isJsonPrimitive()) {
                    LocalDateTime localDateTime = LocalDateTime.parse(time.getAsString());
                    ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                    long millis = zdt.toInstant().toEpochMilli();
                    trigger = new OnceRealTimeTrigger(null, millis);
                    trigger.setCallOnce(true);
                } else {
                    DebugManager.warn(Modules.Location.create(LocationCompiler.class), "Can't compile " + VALUE_REAL_TIMED_TRIGGER + " trigger. Missing time.");
                    return Optional.empty();
                }
                break;
            case VALUE_REAL_PERIODIC_TRIGGER:
                time = jsonObject.get(KEY_PERIOD);
                if (time != null && time.isJsonPrimitive()) {
                    trigger = new PeriodicRealTimeTrigger(null, time.getAsInt());
                } else {
                    DebugManager.warn(Modules.Location.create(LocationCompiler.class), "Can't compile " + VALUE_REAL_PERIODIC_TRIGGER + " trigger. Missing time.");
                    return Optional.empty();
                }
                break;
            case VALUE_SERVER_TIMED_TRIGGER:
                time = jsonObject.get(KEY_TIME);
                if (time != null && time.isJsonPrimitive()) {
                    trigger = new OnceServerTimeTrigger(null, time.getAsInt());
                } else {
                    DebugManager.warn(Modules.Location.create(LocationCompiler.class), "Can't compile " + VALUE_SERVER_TIMED_TRIGGER + " trigger. Missing time.");
                    return Optional.empty();
                }
                break;
            case VALUE_SERVER_PERIODIC_TRIGGER:
                time = jsonObject.get(KEY_PERIOD);
                if (time != null && time.isJsonPrimitive()) {
                    trigger = new PeriodicServerTimeTrigger(null, time.getAsInt());
                } else {
                    trigger = new PeriodicServerTimeTrigger(null, TimedTriggerManager.MIN_TRIGGER_CHECK_PERIOD);
                }
                break;
            case VALUE_PLAYER_TALK_TRIGGER:
                trigger = new PlayerTalkTrigger(null);
                break;
            case VALUE_PLAYER_JOIN_TRIGGER:
                trigger = new PlayerJoinTrigger(null);
                break;
            case VALUE_PLAYER_QUIT_TRIGGER:
                trigger = new PlayerQuitTrigger(null);
                break;
            case VALUE_PLAYER_VIRTUAL_ATTACK_TRIGGER:
                boolean useAllEntities = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_USE_ALL_ENTITIES),false);
                trigger = new VirtualPlayerAttackTrigger(null, useAllEntities);
                break;
            case VALUE_GOAL_FINISHED_TRIGGER:
                useAllEntities = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_USE_ALL_ENTITIES),false);
                trigger = new GoalFinishedTrigger(null, useAllEntities);
                break;
            case VALUE_VIRTUAL_TALK_TRIGGER:
                useAllEntities = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_USE_ALL_ENTITIES),false);
                trigger = new VirtualEntityTalkTrigger(null, useAllEntities);
                break;
            case VALUE_VIRTUAL_STOP_TALK_TRIGGER:
                useAllEntities = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_USE_ALL_ENTITIES),false);
                trigger = new VirtualEntityStopTalkTrigger(null, useAllEntities);
                break;
            case VALUE_ANIMATION_CHANGE_TRIGGER:
                JsonElement currentJson = jsonObject.get(KEY_CURRENT_ANIMATION);
                String current = null;
                if (currentJson instanceof JsonPrimitive) {
                    current = currentJson.getAsString();
                }
                JsonElement nextJson = jsonObject.get(KEY_NEXT_ANIMATION);
                String next = null;
                if (nextJson instanceof JsonPrimitive) {
                    next = nextJson.getAsString();
                }
                useAllEntities = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_USE_ALL_ENTITIES),false);
                trigger = new AnimationChangeTrigger(null, current, next, useAllEntities);
                break;
            case VALUE_SELECTION_TRIGGER:
                JsonElement timeJson = jsonObject.get(KEY_PERIOD);
                int period = TimedTriggerManager.MIN_TRIGGER_CHECK_PERIOD;
                if (timeJson != null && timeJson.isJsonPrimitive()) {
                    try {
                        period = timeJson.getAsInt();
                    } catch(NumberFormatException ignore) {}
                }
                McmeEntitySelector mcmeEntitySelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                JsonElement processJson = jsonObject.get(KEY_PROCESS);
                SelectionTrigger.Process process;
                if (processJson instanceof JsonPrimitive && processJson.getAsString().equalsIgnoreCase("leave")) {
                    process = SelectionTrigger.Process.LEAVE;
                } else if (processJson instanceof JsonPrimitive && processJson.getAsString().equalsIgnoreCase("enter")) {
                    process = SelectionTrigger.Process.ENTER;
                } else{
                    process = SelectionTrigger.Process.ENTER;
                    DebugManager.warn(Modules.Location.create(LocationCompiler.class), "Can't parse " + KEY_PROCESS + ". Using "+process.name());
                }
                trigger = new SelectionTrigger(null, period, mcmeEntitySelector, process);
                break;
            case VALUE_EXTERNAL_TRIGGER:
                String name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name == null) {
                    DebugManager.warn(Modules.Location.create(LocationCompiler.class), "Can't compile " + VALUE_EXTERNAL_TRIGGER + " trigger. Missing name.");
                    return Optional.empty();
                }
                trigger = new ExternalTrigger(null,name);
                break;
        }
        if(trigger == null) {
            DebugManager.warn(Modules.Location.create(LocationCompiler.class),"Can't compile trigger. Invalid trigger type.");
            return Optional.empty();
        }
        DecisionTreeTrigger.DecisionNode decisionNode = compileDecisionNode(jsonObject);
        trigger.setDecisionNode(decisionNode);
        trigger.setLocation(LocationCompiler.compile(jsonObject.get(KEY_LOCATION)).orElse(null));
        trigger.setPlayer(SelectorCompiler.compilePlayerSelector(jsonObject,KEY_PLAYER));
        trigger.setEntity(SelectorCompiler.compileVirtualEntitySelector(jsonObject,KEY_PLAYER));
        DecisionTreeTrigger finalTrigger = trigger;
        LocationCompiler.compile(jsonObject.get(KEY_CENTER)).ifPresent(finalTrigger::setLocation);

        JsonElement nameJson = jsonObject.get(KEY_NAME);
        if(nameJson != null && nameJson.isJsonPrimitive()) {
            trigger.setName(nameJson.getAsString());
        }

        boolean callOnce = false;
        JsonElement callOnceJson = jsonObject.get(KEY_CALL_ONCE);
        if(callOnceJson!=null) {
            callOnce = callOnceJson.getAsBoolean();
        }
        trigger.setCallOnce(callOnce);

        return Optional.of(trigger);
    }

    private static DecisionTreeTrigger.DecisionNode compileDecisionNode(JsonObject jsonObject) {
        Collection<Condition> conditions = ConditionCompiler.compile(jsonObject);
        boolean metAllConditions = getMetAllConditions(jsonObject);
        Collection<Action> actions = ActionCompiler.compile(jsonObject);
        DecisionTreeTrigger.DecisionNode node = new DecisionTreeTrigger.DecisionNode(actions);
        node.setMetAllConditions(metAllConditions);
        node.addConditions(conditions);

        JsonElement thenData = jsonObject.get(KEY_THEN);
        if(thenData!=null) {
            node.setConditionSuccessTrigger(compileDecisionNode(thenData.getAsJsonObject()));
        }
        JsonElement elseData = jsonObject.get(KEY_ELSE);
        if(thenData!=null) {
            node.setConditionFailTrigger(compileDecisionNode(elseData.getAsJsonObject()));
        }
        return node;
    }

    public static boolean getMetAllConditions(JsonObject jsonData) {
        JsonElement data = jsonData.get(KEY_MET_ALL_CONDITIONS);
        if(data!=null) {
            return data.getAsBoolean();
        }
        return false;
    }


}
