package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcmiddleearth.entities.api.VirtualEntityFactory;
import com.mcmiddleearth.entities.api.VirtualEntityGoalFactory;
import com.mcmiddleearth.entities.effect.Explosion;
import com.mcmiddleearth.entities.entities.composite.bones.SpeechBalloonLayout;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.action.*;
import com.mcmiddleearth.mcmescripts.action.quest.StageDisableAction;
import com.mcmiddleearth.mcmescripts.action.quest.StageEnableAction;
import com.mcmiddleearth.mcmescripts.action.quest.TagDeleteAction;
import com.mcmiddleearth.mcmescripts.action.quest.TagSetAction;
import com.mcmiddleearth.mcmescripts.component.EnchantmentChoice;
import com.mcmiddleearth.mcmescripts.component.ItemFilter;
import com.mcmiddleearth.mcmescripts.component.WrappedEnchantment;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.looting.ItemChoice;
import com.mcmiddleearth.mcmescripts.quest.tags.StringTag;
import com.mcmiddleearth.mcmescripts.selector.McmeEntitySelector;
import com.mcmiddleearth.mcmescripts.selector.PlayerSelector;
import com.mcmiddleearth.mcmescripts.selector.VirtualEntitySelector;
import com.mcmiddleearth.mcmescripts.trigger.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;


public class ActionCompiler {

    public static final String  KEY_ACTION          = "action",
                                KEY_ACTION_ARRAY    = "actions",

                                KEY_ACTION_TYPE     = "type",
                                KEY_DELAY           = "delay",
                                KEY_TARGET          = "location",
                                KEY_GOAL_TARGET     = "goal_target",
                                KEY_TRIGGER_NAME    = "name",
                                KEY_TELEPORT_SPREAD = "spread",
                                KEY_POTION_EFFECT   = "potion_effect",
                                KEY_TIME            = "time",
                                KEY_STATE           = "state",
                                KEY_ANIMATION       = "animation",
                                KEY_OVERRIDE        = "override",
                                KEY_ITEM            = "item",
                                KEY_ITEMS           = "items",
                                KEY_ITEM_FILTER     = "item_filter",
                                KEY_ITEM_FILTERS    = "item_filters",
                                KEY_ENCHANTMENT     = "enchantment",
                                KEY_ENCHANTMENTS    = "enchantments",
                                KEY_SLOT            = "slot",
                                KEY_SLOT_ID         = "slot_id",
                                KEY_DURATION        = "duration",
                                KEY_COMMAND         = "command",
                                KEY_PROBABILITY     = "probability",
                                KEY_GROUP           = "group",
                                KEY_RADIUS          = "radius",
                                KEY_QUANTITY        = "quantity",
                                KEY_CHOICES         = "choices",
                                KEY_WEIGHT          = "weight",
                                KEY_CENTER          = "center",
                                KEY_MUSIC_FILE      = "sound_file",
                                KEY_MUSIC_ID        = "sound_id",
                                KEY_LIFESPAN        = "lifespan",
                                KEY_DROP_HEIGHT     = "drop_height",
                                KEY_TITLE           = "title",
                                KEY_SUBTITLE        = "subtitle",
                                KEY_FADE_IN         = "fade_in",
                                KEY_STAY            = "stay",
                                KEY_FADE_OUT        = "fade_out",
                                KEY_ON_GROUND       = "on_ground",
                                KEY_NAME            = "name",
                                KEY_VISIBLE         = "visible",
                                KEY_STYLE           = "style",
                                KEY_COLOR           = "color",
                                KEY_FOG             = "fog",
                                KEY_DARKEN          = "darken",
                                KEY_MUSIC           = "music",
                                KEY_PROGRESS        = "progress",
                                KEY_LOCATION        = "location",
                                KEY_CHECKPOINTS     = "checkpoints",
                                KEY_SERVER_SIDE     = "server_side",
                                KEY_ENEMIES         = "enemies",
                                KEY_X_EDGE          = "x_edge",
                                KEY_SPREAD          = "spread",

                                KEY_TAG_VALUE        = "value",

                                VALUE_REGISTER_TRIGGER      = "register_event",
                                VALUE_UNREGISTER_TRIGGER    = "unregister_event",

                                VALUE_SET_GOAL              = "set_goal",
                                VALUE_SET_ENEMIES           = "set_enemies",
                                VALUE_SPAWN                 = "spawn",
                                VALUE_SPAWN_RELATIVE        = "spawn_relative",
                                VALUE_DESPAWN               = "despawn",
                                VALUE_STOP_TALK             = "stop_talk",
                                VALUE_TALK                  = "talk",
                                VALUE_TELEPORT              = "teleport",
                                VALUE_ADD_POTION_EFFECT     = "add_potion_effect",
                                VALUE_REMOVE_POTION_EFFECT  = "remove_potion_effect",
                                VALUE_SET_SERVER_TIME       = "set_server_time",
                                VALUE_ENTITY_STATE          = "entity_state",
                                VALUE_ANIMATION             = "animation",
                                VALUE_GIVE_ITEM             = "give_item",
                                VALUE_REMOVE_ITEM           = "remove_item",
                                VALUE_EYE_EFFECT            = "eye_effect",
                                VALUE_EXECUTE_COMMAND       = "execute_command",
                                VALUE_FIREWORK              = "firework",
                                VALUE_EXPLOSION             = "explosion",
                                VALUE_RANDOM_SPAWN          = "random_spawn",
                                VALUE_MUSIC_START           = "start_sound",
                                VALUE_MUSIC_STOP            = "stop_sound",
                                VALUE_GIVE_CHEST            = "give_chest",
                                VALUE_RAIN_ITEM             = "rain_item",
                                VALUE_TITLE                 = "title",
                                VALUE_ACTION_BAR            = "action_bar",
                                VALUE_ADD_ENCHANTMENT       = "add_enchantment",
                                VALUE_REMOVE_ENCHANTMENT    = "remove_enchantment",
                                VALUE_BOSS_BAR_ADD          = "boss_bar_add",
                                VALUE_BOSS_BAR_REMOVE       = "boss_bar_remove",
                                VALUE_BOSS_BAR_EDIT         = "boss_bar_edit",

                                VALUE_STAGE_ENABLE          = "enable_stage",
                                VALUE_STAGE_DISABLE         = "disable_stage",
                                VALUE_TAG_SET               = "set_tag",
                                VALUE_TAG_DELETE            = "delete_tag";


    public static Collection<Action> compile(JsonObject jsonData) {
        JsonElement actionData = jsonData.get(KEY_ACTION);
        Set<Action> result = new HashSet<>(compileActions(actionData));
        actionData = jsonData.get(KEY_ACTION_ARRAY);
        result.addAll(compileActions(actionData));
        return result;
    }

    private static Set<Action> compileActions(JsonElement actionData) {
        Set<Action> result = new HashSet<>();
        if(actionData == null) return result;
        if(actionData.isJsonArray()) {
            for(int i = 0; i< actionData.getAsJsonArray().size(); i++) {
                compileAction(actionData.getAsJsonArray().get(i).getAsJsonObject()).ifPresent(result::add);
            }
        } else {
            compileAction(actionData.getAsJsonObject()).ifPresent(result::add);
        }
        return result;
    }

    private static Optional<Action> compileAction(JsonObject jsonObject) {
        Action action;
        JsonElement type = jsonObject.get(KEY_ACTION_TYPE);
        if (type == null) {
            DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile action. Missing: "+KEY_ACTION_TYPE);
            return Optional.empty();
        }
        switch(type.getAsString()) {
            case VALUE_REGISTER_TRIGGER:
                Set<Trigger> triggers = TriggerCompiler.compile(jsonObject);
                if(triggers.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_REGISTER_TRIGGER+" action. Missing event.");
                    return Optional.empty();
                }
                action = new TriggerRegisterAction(triggers);
                break;
            case VALUE_UNREGISTER_TRIGGER:
                Set<String> triggerNames = compileTriggerNames(jsonObject);
                if(triggerNames.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_UNREGISTER_TRIGGER+" action. Missing events.");
                    return Optional.empty();
                }
                action = new TriggerUnregisterAction(triggerNames);
                break;
            case VALUE_SET_GOAL:
                Optional<VirtualEntityGoalFactory> goalFactoryOpt = VirtualEntityGoalFactoryCompiler.compile(jsonObject);
                if(!goalFactoryOpt.isPresent()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_SET_GOAL+" action. Missing goal.");
                    return Optional.empty();
                }
                VirtualEntitySelector selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                McmeEntitySelector goalTargetSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject, KEY_GOAL_TARGET);
                action = new SetGoalAction(goalFactoryOpt.get(), selector, goalTargetSelector);
                break;
            case VALUE_SET_ENEMIES:
                selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                McmeEntitySelector enemySelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject, KEY_ENEMIES);
                action = new SetEnemyAction(selector, enemySelector);
                break;
            case VALUE_SPAWN:
                List<VirtualEntityFactory> factories = VirtualEntityFactoryCompiler.compile(jsonObject);
                if(factories.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_SPAWN+" action. Missing entity factory.");
                    return Optional.empty();
                }
                int lifespan = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_LIFESPAN),-1);
                boolean serverSide = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_SERVER_SIDE),false);
                //TODO: optional - set Goal
                action = new SpawnAction(factories, lifespan, serverSide);
                break;
            case VALUE_SPAWN_RELATIVE:
                factories = VirtualEntityFactoryCompiler.compile(jsonObject);
                if(factories.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_SPAWN_RELATIVE+" action. Missing entity factory.");
                    return Optional.empty();
                }
                lifespan = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_LIFESPAN),-1);
                Location location = LocationCompiler.compile(jsonObject.get(KEY_LOCATION)).orElse(null);
                JsonElement checkpointJson = jsonObject.get(KEY_CHECKPOINTS);
                boolean onGround = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_ON_GROUND),true);
                VirtualEntityGoalFactory goalFactory = VirtualEntityGoalFactoryCompiler.compile(jsonObject).orElse(null);
                Location[] checkpoints = null;
                if(goalFactory!=null) {
                    List<Location> waypoints = new ArrayList<>();
                    if (checkpointJson instanceof JsonArray) {
                        for (JsonElement element : checkpointJson.getAsJsonArray()) {
                            LocationCompiler.compile(element).ifPresent(waypoints::add);
                        }
                        if (waypoints.isEmpty()) {
                            DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Warning while parsing "+VALUE_SPAWN_RELATIVE
                                                                           +" action. Found empty checkpoint array.");
                        } else {
                            checkpoints = waypoints.toArray(new Location[0]);
                        }
                    }
                }
                goalTargetSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject, KEY_GOAL_TARGET);
                McmeEntitySelector mcmeEntitySelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                int quantity = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_QUANTITY),1);
                serverSide = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_SERVER_SIDE),false);

                int spreading = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_SPREAD),1);
                int xEdge = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_X_EDGE),(int) Math.sqrt(quantity));
                action = new SpawnRelativeAction(mcmeEntitySelector, factories, lifespan, onGround,
                                                 goalTargetSelector, goalFactory, location, checkpoints, serverSide,
                                                 quantity,xEdge, spreading);
                break;
            case VALUE_DESPAWN:
                selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                action = new DespawnAction(selector);
                break;
            case VALUE_STOP_TALK:
                selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                action = new StopTalkAction(selector);
                break;
            case VALUE_TALK:
                SpeechBalloonLayout layout = SpeechBalloonLayoutCompiler.compile(jsonObject);
                selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                action = new TalkAction(layout,selector);
                break;
            case VALUE_TELEPORT:
                Location target = LocationCompiler.compile(jsonObject.get(KEY_TARGET)).orElse(null);
                if(target == null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_TELEPORT+" action. Missing target location.");
                    return Optional.empty();
                }
                PlayerSelector playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                double spread = PrimitiveCompiler.compileDouble(jsonObject.get(KEY_TELEPORT_SPREAD),0);
                action = new TeleportAction(target,spread,playerSelector);
                break;
            case VALUE_ADD_POTION_EFFECT:
                McmeEntitySelector mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                PotionEffect effect = PotionEffectCompiler.compile(jsonObject.get(KEY_POTION_EFFECT));
                Set<PotionEffectAddAction.PotionEffectChoice> potionEffectChoices = new HashSet<>();
                JsonElement effectChoicesJson = jsonObject.get(KEY_CHOICES);
                if(effectChoicesJson instanceof JsonArray) {
                    effectChoicesJson.getAsJsonArray().forEach(element -> {
                        if(element instanceof JsonObject) {
                            int weight = PrimitiveCompiler.compileInteger(element.getAsJsonObject().get(KEY_WEIGHT),10);
                            PotionEffect choiceEffect = PotionEffectCompiler.compile(element.getAsJsonObject().get(KEY_POTION_EFFECT));
                            if(choiceEffect!=null) {
                                potionEffectChoices.add(new PotionEffectAddAction.PotionEffectChoice(choiceEffect, weight));
                            } else {
                                DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't parse potion effect in random choice.");
                            }
                        }
                    });
                }
                if(effect == null && potionEffectChoices.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_ADD_POTION_EFFECT+" action. Missing potion effect.");
                    return Optional.empty();
                }
                action = new PotionEffectAddAction(effect, potionEffectChoices, mcmeSelector);
                break;
            case VALUE_REMOVE_POTION_EFFECT:
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                effect = PotionEffectCompiler.compile(jsonObject.get(KEY_POTION_EFFECT));
                if(effect == null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_REMOVE_POTION_EFFECT+" action. Missing potion effect.");
                    return Optional.empty();
                }
                action = new PotionEffectRemoveAction(effect, mcmeSelector);
                break;
            case VALUE_SET_SERVER_TIME:
                JsonElement timeJson = jsonObject.get(KEY_TIME);
                if(! (timeJson instanceof JsonPrimitive)) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_SET_SERVER_TIME+" action. Missing server time.");
                    return Optional.empty();
                }
                long serverTime = timeJson.getAsLong();
                action = new ServerTimeAction(serverTime);
                break;
            case VALUE_ENTITY_STATE:
                JsonElement stateJson = jsonObject.get(KEY_STATE);
                if(! (stateJson instanceof JsonPrimitive)) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_ENTITY_STATE+" action. Missing entity state.");
                    return Optional.empty();
                }
                String state = stateJson.getAsString();
                selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                action = new EntityStateAction(selector, state);
                break;
            case VALUE_ANIMATION:
                JsonElement animationJson = jsonObject.get(KEY_ANIMATION);
                if(! (animationJson instanceof JsonPrimitive)) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_ANIMATION+" action. Missing animation name.");
                    return Optional.empty();
                }
                String animationName = animationJson.getAsString();
                JsonElement overrideJson = jsonObject.get(KEY_OVERRIDE);
                boolean override = true;
                if(overrideJson instanceof JsonPrimitive) {
                    override = overrideJson.getAsBoolean();
                }
                selector = SelectorCompiler.compileVirtualEntitySelector(jsonObject);
                action = new AnimationAction(selector, animationName, override);
                break;
            case VALUE_GIVE_ITEM:
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                Set<ItemStack> items = ItemCompiler.compile(jsonObject.get(KEY_ITEM));
                items.addAll(ItemCompiler.compile(jsonObject.get(KEY_ITEMS)));
                Set<ItemChoice> itemChoices = LootTableCompiler.compileItemChoices(jsonObject).orElse(new HashSet<>());
                if(items.isEmpty() && itemChoices.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class), "No items found for "+VALUE_GIVE_ITEM+" action.");
                    return Optional.empty();
                }
                EquipmentSlot slot = null;
                JsonElement slotJson = jsonObject.get(KEY_SLOT);
                if(slotJson instanceof JsonPrimitive) {
                    try {
                        slot = EquipmentSlot.valueOf(slotJson.getAsString().toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Illegal equipment slot for "+VALUE_GIVE_ITEM+" action. Using main hand slot.");
                        slot = EquipmentSlot.HAND;
                    }
                }
                int slotId = -1;
                JsonElement slotIdJson = jsonObject.get(KEY_SLOT_ID);
                if(slotIdJson instanceof JsonPrimitive) {
                    try {
                        slotId = slotIdJson.getAsInt();
                    } catch(NumberFormatException ex) {
                        DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't parse slot id for action "+VALUE_GIVE_ITEM+".");
                    }
                }
                int duration = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_DURATION),-1);
                action = new ItemGiveAction(mcmeSelector, items, itemChoices, slot, slotId, duration);
                break;
            case VALUE_ADD_ENCHANTMENT:
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                Set<WrappedEnchantment> enchantments = EnchantmentCompiler.compile(jsonObject.get(KEY_ENCHANTMENT));
                enchantments.addAll(EnchantmentCompiler.compile(jsonObject.get(KEY_ENCHANTMENTS)));
                Set<EnchantmentChoice> enchantmentChoices = LootTableCompiler.compileEnchantmentChoices(jsonObject).orElse(new HashSet<>());
                if(enchantments.isEmpty() && enchantmentChoices.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_ADD_ENCHANTMENT+". Missing enchantment.");
                    return Optional.empty();
                }

                Set<ItemFilter> itemFilters = ItemFilterCompiler.compile(jsonObject.get(KEY_ITEM_FILTER));
                itemFilters.addAll(ItemFilterCompiler.compile(jsonObject.get(KEY_ITEM_FILTERS)));

                quantity = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_QUANTITY),-1);
                duration = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_DURATION),-1);

                action = new EnchantmentAddAction(mcmeSelector, itemFilters, enchantments, enchantmentChoices, quantity, duration);
                break;
            case VALUE_REMOVE_ENCHANTMENT:
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                enchantments = EnchantmentCompiler.compile(jsonObject.get(KEY_ENCHANTMENT));
                enchantments.addAll(EnchantmentCompiler.compile(jsonObject.get(KEY_ENCHANTMENTS)));
                enchantmentChoices = LootTableCompiler.compileEnchantmentChoices(jsonObject).orElse(new HashSet<>());
                if(enchantments.isEmpty() && enchantmentChoices.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_REMOVE_ENCHANTMENT+". Missing enchantment.");
                    return Optional.empty();
                }

                itemFilters = ItemFilterCompiler.compile(jsonObject.get(KEY_ITEM_FILTER));
                itemFilters.addAll(ItemFilterCompiler.compile(jsonObject.get(KEY_ITEM_FILTERS)));

                quantity = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_QUANTITY),-1);
                action = new EnchantmentRemoveAction(mcmeSelector, itemFilters, enchantments, enchantmentChoices, quantity);
                break;
            case VALUE_REMOVE_ITEM:
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                items = ItemCompiler.compile(jsonObject.get(KEY_ITEM));
                items.addAll(ItemCompiler.compile(jsonObject.get(KEY_ITEMS)));
                if(items.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_REMOVE_ITEM+". Missing item.");
                    return Optional.empty();
                }
                action = new ItemRemoveAction(mcmeSelector, items);
                break;
            case VALUE_EYE_EFFECT:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                duration = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_DURATION),200);
                action = new EyeEffectAction(playerSelector, duration);
                break;
            case VALUE_EXECUTE_COMMAND:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                JsonElement commandJson = jsonObject.get(KEY_COMMAND);
                if(!(commandJson instanceof JsonPrimitive)) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_EXECUTE_COMMAND+" action. Missing command line.");
                    return Optional.empty();
                }
                String command = commandJson.getAsString();
                List<String> whitelist = MCMEScripts.getInstance().getConfig().getStringList("commandWhitelist");
                boolean done = false;
                action = null;
                for(String search: whitelist) {
                    //Logger.getGlobal().info("- "+search + " equal: "+search.equalsIgnoreCase(command)+" wildcard: "+(search.charAt(search.length()-1)=='*') + " similar: "+command.toLowerCase().startsWith(search.substring(0,search.length()-1).toLowerCase()));
                    if(search.equalsIgnoreCase(command)
                            || search.charAt(search.length()-1)=='*'
                                && command.toLowerCase().startsWith(search.substring(0,search.length()-1).toLowerCase())) {
                        action = new ExecuteCommandAction(playerSelector, command);
                        done = true;
                        break;
                    }
                }
                if(!done) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class), "Can't compile " + VALUE_EXECUTE_COMMAND + " action. Command not whitelisted: " + command);
                    return Optional.empty();
                }
                break;
            case VALUE_FIREWORK:
                location = LocationCompiler.compile(jsonObject.get(KEY_TARGET)).orElse(null);
                FireworkMeta fireworkMeta = FireworkMetaCompiler.compile(jsonObject);
                /*if(fireworkMeta == null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_FIREWORK+" action. Missing firework meta.");
                    return Optional.empty();
                }*/
                action = new FireworkAction(location, fireworkMeta);
                break;
            case VALUE_EXPLOSION:
                Explosion explosion = ExplosionCompiler.compile(jsonObject);
                if(explosion == null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_EXPLOSION+" action. Missing explosion data.");
                    return Optional.empty();
                }
                McmeEntitySelector unaffectedSelector = ExplosionCompiler.getUnaffectedSelector(jsonObject);
                McmeEntitySelector damagerSelector = ExplosionCompiler.getDamagerSelector(jsonObject);
                action = new ExplosionAction(explosion, unaffectedSelector, damagerSelector);
                break;
            case VALUE_RANDOM_SPAWN:
                JsonElement choicesJson = jsonObject.get(KEY_CHOICES);
                if(!(choicesJson instanceof JsonArray)) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_RANDOM_SPAWN+". Missing entity.");
                    return Optional.empty();
                }
                List<SpawnRandomSelectionAction.Choice> choices = new ArrayList<>();
                for(JsonElement choiceJson: choicesJson.getAsJsonArray()) {
//Logger.getGlobal().info("Creating factories!");
                    factories = VirtualEntityFactoryCompiler.compile(choiceJson.getAsJsonObject());
//factories.forEach(factory -> Logger.getGlobal().info("Factory type compiler: "+factory.getType()));
                    int weight = PrimitiveCompiler.compileInteger(choiceJson.getAsJsonObject().get(KEY_WEIGHT), 10);
                    choices.add(new SpawnRandomSelectionAction.Choice(weight, factories));
                }
                double probability = PrimitiveCompiler.compileDouble(jsonObject.get(KEY_PROBABILITY),1);
                boolean group = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_GROUP),true);
                int minRadius = PrimitiveCompiler.compileLowerInt(jsonObject.get(KEY_RADIUS),5);
                int maxRadius = PrimitiveCompiler.compileUpperInt(jsonObject.get(KEY_RADIUS),10);
                int minQuantity = PrimitiveCompiler.compileLowerInt(jsonObject.get(KEY_QUANTITY),2);
                int maxQuantity = PrimitiveCompiler.compileUpperInt(jsonObject.get(KEY_QUANTITY),5);
                serverSide = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_SERVER_SIDE),false);
                SpawnRandomSelectionAction.RandomSpawnData randomSpawnData = new SpawnRandomSelectionAction.RandomSpawnData(choices, serverSide)
                        .withMinQuantity(minQuantity).withMaxQuantity(maxQuantity)
                        .withMinRadius(minRadius).withMaxRadius(maxRadius)
                        .withProbability(probability).withGroup(group);
                JsonElement goalTargetJson = jsonObject.get(KEY_GOAL_TARGET);
                if(goalTargetJson instanceof JsonPrimitive) {
                    randomSpawnData.withGoalTargetSelector(new McmeEntitySelector(goalTargetJson.getAsString()));
                }
                VirtualEntityGoalFactoryCompiler.compile(jsonObject).ifPresent(randomSpawnData::withGoalFactory);
                lifespan = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_LIFESPAN),-1);
                Location center = LocationCompiler.compile(jsonObject.get(KEY_CENTER)).orElse(null);
                if(center != null) {
                    action = new SpawnRandomLocationAction(center, randomSpawnData, lifespan);
                } else {
                    mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                    action = new SpawnRandomSelectionAction(mcmeSelector,randomSpawnData, lifespan);
                }
                break;
            case VALUE_MUSIC_START:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                String musicFile = PrimitiveCompiler.compileString(jsonObject.get(KEY_MUSIC_FILE),null);
                if(musicFile == null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_MUSIC_START+". Missing music file.");
                    return Optional.empty();
                }
                String musicId = PrimitiveCompiler.compileString(jsonObject.get(KEY_MUSIC_ID),null);
                action = new SoundStartAction(playerSelector,musicFile, musicId);
                break;
            case VALUE_MUSIC_STOP:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                musicId = PrimitiveCompiler.compileString(jsonObject.get(KEY_MUSIC_ID),null);
                action = new SoundStopAction(playerSelector, musicId);
                break;
            case VALUE_GIVE_CHEST:
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                duration = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_DURATION),1200);
                items = ItemCompiler.compile(jsonObject.get(KEY_ITEM));
                items.addAll(ItemCompiler.compile(jsonObject.get(KEY_ITEMS)));
                itemChoices = LootTableCompiler.compileItemChoices(jsonObject).orElse(new HashSet<>());
                if(items.isEmpty() && itemChoices.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_GIVE_CHEST+". Missing items.");
                    return Optional.empty();
                }
                action = new GiveChestAction(mcmeSelector,items, itemChoices,duration);
                break;
            case VALUE_RAIN_ITEM:
                items = ItemCompiler.compile(jsonObject.get(KEY_ITEM));
                items.addAll(ItemCompiler.compile(jsonObject.get(KEY_ITEMS)));
                if(items.isEmpty()) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_RAIN_ITEM+". Missing item.");
                    return Optional.empty();
                }
                mcmeSelector = SelectorCompiler.compileMcmeEntitySelector(jsonObject);
                duration = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_DURATION),200);
                probability = PrimitiveCompiler.compileDouble(jsonObject.get(KEY_PROBABILITY),0.5);
                int radius = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_RADIUS),10);
                int drop_height = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_DROP_HEIGHT),5);
                action = new ItemRainAction(mcmeSelector,items,radius,drop_height,probability,duration);
                break;
            case VALUE_TITLE:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                String title = PrimitiveCompiler.compileString(jsonObject.get(KEY_TITLE),"").replace('&','§').replace('#','§');
                String subtitle = PrimitiveCompiler.compileString(jsonObject.get(KEY_SUBTITLE),"").replace('&','§').replace('#','§');
                if(title.equals("") && subtitle.equals("")) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_TITLE+" action. Missing title and subtitle.");
                    return Optional.empty();
                }
                int fadeIn = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_FADE_IN),10);
                int stay = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_STAY),70);
                int fadeout = PrimitiveCompiler.compileInteger(jsonObject.get(KEY_FADE_OUT),20);
                action = new TitleAction(playerSelector,title,subtitle,fadeIn,stay,fadeout);
                break;
            case VALUE_ACTION_BAR:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                title = PrimitiveCompiler.compileString(jsonObject.get(KEY_TITLE),"").replace('&','§').replace('#','§');
                if(title.equals("")) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),
                                       "Can't compile "+VALUE_ACTION_BAR+" action. Missing title.");
                    return Optional.empty();
                }
                action = new ActionBarAction(playerSelector,title);
                break;
            case VALUE_BOSS_BAR_ADD:
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                String name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_BOSS_BAR_ADD+" action. Missing bar name.");
                    return Optional.empty();
                }
                NamespacedKey barKey = new NamespacedKey(MCMEScripts.getInstance(),name);
                BossBar bar = Bukkit.getBossBar(barKey);
                title = PrimitiveCompiler.compileString(jsonObject.get(KEY_TITLE),null).replace('&','§').replace('#','§');
                Boolean fog = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_FOG),null);
                Boolean dark = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_DARKEN),null);
                Boolean music = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_MUSIC),null);
                Double progress = PrimitiveCompiler.compileDouble(jsonObject.get(KEY_PROGRESS),null);
                Boolean visible = PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_VISIBLE),null);
                BarColor color = BossBarCompiler.compileBarColor(jsonObject.get(KEY_COLOR));
                BarStyle style = BossBarCompiler.compileBarStyle(jsonObject.get(KEY_STYLE));
                if(bar == null) {
                    BarFlag[] flags = BossBarCompiler.compileBarFlags(fog,dark,music);
                    if(color == null) color = BarColor.RED;
                    if(style == null) style = BarStyle.SOLID;
                    bar = Bukkit.createBossBar(barKey, title, color, style, flags);
                }
                BossBarEditAction.editBar(bar,title,style,color,fog,dark,music,progress,visible);
                action = new BossBarAddAction(playerSelector, bar);
                break;
            case VALUE_BOSS_BAR_EDIT:
                name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_BOSS_BAR_EDIT+" action. Missing bar name.");
                    return Optional.empty();
                }
                action = new BossBarEditAction(new NamespacedKey(MCMEScripts.getInstance(),name),
                                                PrimitiveCompiler.compileString(jsonObject.get(KEY_TITLE),null),
                                                BossBarCompiler.compileBarColor(jsonObject.get(KEY_COLOR)),
                                                BossBarCompiler.compileBarStyle(jsonObject.get(KEY_STYLE)),
                                                PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_FOG),null),
                                                PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_DARKEN),null),
                                                PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_MUSIC),null),
                                                PrimitiveCompiler.compileDouble(jsonObject.get(KEY_PROGRESS),null),
                                                PrimitiveCompiler.compileBoolean(jsonObject.get(KEY_VISIBLE),null));
                break;
            case VALUE_BOSS_BAR_REMOVE:
                name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_BOSS_BAR_REMOVE+" action. Missing bar name.");
                    return Optional.empty();
                }
                playerSelector = SelectorCompiler.compilePlayerSelector(jsonObject);
                action = new BossBarRemoveAction(playerSelector, new NamespacedKey(MCMEScripts.getInstance(),name));
                break;
            case VALUE_STAGE_ENABLE:
                name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_STAGE_ENABLE+" action. Missing stage name.");
                    return Optional.empty();
                }
                action = new StageEnableAction(name);
                break;
            case VALUE_STAGE_DISABLE:
                name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_STAGE_DISABLE+" action. Missing stage name.");
                    return Optional.empty();
                }
                action = new StageDisableAction(name);
                break;
            case VALUE_TAG_SET:
                name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                String value = PrimitiveCompiler.compileString(jsonObject.get(KEY_TAG_VALUE), null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_TAG_SET+" action. Missing tag name.");
                    return Optional.empty();
                }
                action = new TagSetAction(new StringTag(name,value));
                break;
            case VALUE_TAG_DELETE:
                name = PrimitiveCompiler.compileString(jsonObject.get(KEY_NAME),null);
                if(name==null) {
                    DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Can't compile "+VALUE_TAG_DELETE+" action. Missing tag name.");
                    return Optional.empty();
                }
                action = new TagDeleteAction(name);
                break;
            default:
                DebugManager.severe(Modules.Action.create(ActionCompiler.class),"Can't compile action. Unsupported action type.");
                return Optional.empty();
        }
        JsonElement delayJson = jsonObject.get(KEY_DELAY);
        if(delayJson instanceof JsonPrimitive) {
            try {
                action.setDelay(delayJson.getAsInt());
            } catch(ClassCastException ex) {
                DebugManager.warn(Modules.Action.create(ActionCompiler.class),"Invalid numeric data for action delay. Using delay 0!");
            }
        }
        return Optional.of(action);
    }

    private static Set<String> compileTriggerNames(JsonObject jsonObject) {
        JsonElement nameJson = jsonObject.get(KEY_TRIGGER_NAME);
        if(nameJson != null) {
            if (nameJson.isJsonPrimitive()) {
                return Collections.singleton(nameJson.getAsString());
            } else if (nameJson.isJsonArray()) {
                Set<String> result = new HashSet<>();
                nameJson.getAsJsonArray().forEach(element -> result.add(element.getAsString()));
                return result;
            }
        }
        return Collections.emptySet();
    }

}
