package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcmiddleearth.entities.ai.goal.GoalType;
import com.mcmiddleearth.mcmescripts.condition.*;
import com.mcmiddleearth.mcmescripts.condition.proximity.LocationProximityCondition;
import com.mcmiddleearth.mcmescripts.condition.proximity.PlayerProximityCondition;
import com.mcmiddleearth.mcmescripts.condition.proximity.VirtualEntityProximityCondition;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.selector.PlayerSelector;
import com.mcmiddleearth.mcmescripts.selector.Selector;
import com.mcmiddleearth.mcmescripts.selector.VirtualEntitySelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.swing.text.html.Option;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ConditionCompiler {

    private static final String KEY_CONDITION           = "condition",
                                KEY_CONDITION_ARRAY     = "conditions",

                                KEY_CONDITION_TYPE      = "type",
                                KEY_CONSTRAIN           = "criterion",
                                KEY_CENTER              = "center",
                                KEY_MATCH_ALL_SELECTED  = "match_all",
                                KEY_GOAL_TYPE           = "goal_type",
                                KEY_EXCLUDE             = "negate",
                                KEY_CURRENT_ANIMATION   = "current_animation",
                                KEY_MANUAL_ANIMATION    = "manual_animation",
                                KEY_INSTANT_SWITCHING   = "instant_animation_switching",
                                KEY_MANUAL_OVERRIDE     = "manual_animation_override",
                                KEY_START               = "start",
                                KEY_END                 = "end",
                                KEY_WORLD               = "world",
                                KEY_MESSAGE             = "message",

                                VALUE_TALK                  = "talk",
                                VALUE_NO_TALK               = "no_talk",
                                VALUE_GOAL_TYPE             = "goal_type",
                                VALUE_PROXIMITY_LOCATION    = "location_proximity",
                                VALUE_PROXIMITY_PLAYER      = "player_proximity",
                                VALUE_PROXIMITY_ENTITY      = "entity_proximity",
                                VALUE_ANIMATION             = "animation",
                                VALUE_PLAYER_ONLINE         = "player_online",
                                VALUE_SERVER_DAYTIME        = "server_daytime",
                                VALUE_MESSAGE               = "message";

    public static Set<Condition> compile(JsonObject jsonData) {
        JsonElement conditions = jsonData.get(KEY_CONDITION);
        Set<Condition> result = new HashSet<>(compileConditions(conditions));
        conditions = jsonData.get(KEY_CONDITION_ARRAY);
        result.addAll(compileConditions(conditions));
        return result;
    }

    private static Set<Condition> compileConditions(JsonElement conditionData) {
        Set<Condition> result = new HashSet<>();
        if(conditionData==null) return result;
        if(conditionData.isJsonArray()) {
            for(int i = 0; i< conditionData.getAsJsonArray().size(); i++) {
                compileCondition(conditionData.getAsJsonArray().get(i).getAsJsonObject()).ifPresent(result::add);
            }
        } else {
            compileCondition(conditionData.getAsJsonObject()).ifPresent(result::add);
        }
        return result;
    }

    private static Optional<Condition> compileCondition(JsonObject jsonObject) {
        JsonElement type = jsonObject.get(KEY_CONDITION_TYPE);
        if(type==null)  return Optional.empty();
        @SuppressWarnings("rawtypes")
        Selector selector;
        boolean noTalk = false;
        try {
            switch (type.getAsString()) {
                case VALUE_NO_TALK:
                    noTalk = true;
                case VALUE_TALK:
                    selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                    TalkCondition condition = new TalkCondition((VirtualEntitySelector) selector,noTalk);
                    getMatchAll(jsonObject).ifPresent(condition::setMatchAllSelected);
                    return Optional.of(condition);
                case VALUE_GOAL_TYPE:
                    selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                    JsonElement goalTypeJson = jsonObject.get(KEY_GOAL_TYPE);
                    if(goalTypeJson instanceof JsonPrimitive) {
                        try {
                            GoalType goalType = GoalType.valueOf(goalTypeJson.getAsString().toUpperCase());
                            boolean exclude = jsonObject.has(KEY_EXCLUDE) && jsonObject.get(KEY_EXCLUDE).getAsBoolean();
                            GoalTypeCondition goalTypeCondition = new GoalTypeCondition((VirtualEntitySelector) selector,
                                                                                        goalType, exclude);
                            getMatchAll(jsonObject).ifPresent(goalTypeCondition::setMatchAllSelected);
                            return Optional.of(goalTypeCondition);
                        } catch (IllegalArgumentException ex) {
                            DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_GOAL_TYPE+" condition. Illegal goal type.");
                        }
                    } else {
                        DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_GOAL_TYPE+" condition. Missing goal type.");
                    }
                    return Optional.empty();
                case VALUE_PROXIMITY_LOCATION:
                    selector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                    Location location = LocationCompiler.compile(jsonObject.get(KEY_CENTER)).orElse(null);
                    if(location==null) {
                        DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_PROXIMITY_LOCATION+" condition. Missing center location.");
                        return Optional.empty();
                    }
                    return Optional.of(new LocationProximityCondition(location, selector, compileFunction(jsonObject)));
                case VALUE_PROXIMITY_ENTITY:
                    selector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                    String entityName = getName(jsonObject.get(KEY_CENTER));
                    if(entityName==null) {
                        DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_PROXIMITY_ENTITY+" condition. Missing center entity name.");
                        return Optional.empty();
                    }
                    return Optional.of(new VirtualEntityProximityCondition(entityName, selector, compileFunction(jsonObject)));
                case VALUE_PROXIMITY_PLAYER:
                    selector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                    String playerName = getName(jsonObject.get(KEY_CENTER));
                    if(playerName==null) {
                        DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_PROXIMITY_ENTITY+" condition. Missing center player name.");
                        return Optional.empty();
                    }
                    return Optional.of(new PlayerProximityCondition(playerName, selector, compileFunction(jsonObject)));
                case VALUE_ANIMATION:
                    selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                    String current = getName(jsonObject.get(KEY_CURRENT_ANIMATION));
                    Boolean manualAnimation = getBoolean(jsonObject.get(KEY_MANUAL_ANIMATION));
                    Boolean manualOverride = getBoolean(jsonObject.get(KEY_MANUAL_OVERRIDE));
                    Boolean instantSwitching = getBoolean(jsonObject.get(KEY_INSTANT_SWITCHING));
                    return Optional.of(new AnimationCondition(selector, current, manualAnimation,
                                                              instantSwitching, manualOverride));
                case VALUE_SERVER_DAYTIME:
                    boolean exclude = jsonObject.has(KEY_EXCLUDE) && jsonObject.get(KEY_EXCLUDE).getAsBoolean();
                    JsonElement worldJson = jsonObject.get(KEY_WORLD);
                    if(!((worldJson instanceof JsonPrimitive) && Bukkit.getWorld(worldJson.getAsString()) != null)) {
                        DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_SERVER_DAYTIME+" condition. Missing or invalid world name.");
                        return Optional.empty();
                    }
                    World world = Bukkit.getWorld(worldJson.getAsString());
                    long startTick = -1, endTick = -1;
                    try {
                        startTick = jsonObject.get(KEY_START).getAsInt();
                        endTick = jsonObject.get(KEY_END).getAsInt();
                    } catch(NullPointerException | NumberFormatException ignore) {}
                    if(startTick == -1 || endTick == -1 || startTick >= endTick) {
                        DebugManager.warn(Modules.Condition.create(ConditionCompiler.class),"Can't compile "+VALUE_SERVER_DAYTIME+" condition. Missing or invalid time period.");
                        return Optional.empty();
                    }
                    return Optional.of(new ServerDaytimeCondition(world, startTick, endTick, exclude));
                case VALUE_PLAYER_ONLINE:
                    PlayerSelector playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                    return Optional.of(new OnlinePlayerCondition(playerSelector, compileFunction(jsonObject)));
                case VALUE_MESSAGE:
                    boolean negate = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_EXCLUDE), false);
                    String message = PrimitiveCompiler.compileString(jsonObject.get(KEY_MESSAGE), "*");
                    return Optional.of(new MessageCondition(message, negate));
            }
        } catch(NullPointerException ignore) {}
        return Optional.empty();
    }

    private static String getName(JsonElement element) {
        if(!(element instanceof JsonPrimitive)) return null;
        return element.getAsString();
    }

    private static Boolean getBoolean(JsonElement element) {
        if(!(element instanceof JsonPrimitive)) return null;
        return element.getAsBoolean();
    }

    private static Optional<Boolean> getMatchAll(JsonObject jsonObject) {
        JsonElement selectorJson = jsonObject.get(KEY_MATCH_ALL_SELECTED);
        if(selectorJson == null || selectorJson.isJsonPrimitive()) return Optional.empty();
        return Optional.of(selectorJson.getAsBoolean());
    }

    private static Criterion compileFunction(JsonObject jsonObject) {
        JsonElement constrainData = jsonObject.get(KEY_CONSTRAIN);
        if(constrainData!=null) {
            try {
                String constrain = constrainData.getAsString();
                String comparator;
                int limit;
                if (constrain.charAt(1) == '=' || constrain.charAt(1) == '<') {
                    comparator = constrain.substring(0, 2);
                    limit = Integer.parseInt(constrain.substring(2));
                } else {
                    comparator = constrain.substring(0, 1);
                    limit = Integer.parseInt(constrain.substring(1));
                }
                return new Criterion(comparator,limit);

            } catch(NumberFormatException ignore) {}
        }
        DebugManager.warn(Modules.CONDITION_CREATE.getModule(), "Invalid criterion! Condition will always be true!");
        return new Criterion("true",0);//a -> true;
    }
}
