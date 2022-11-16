package com.mcmiddleearth.mcmescripts.selector;

import com.mcmiddleearth.entities.EntitiesPlugin;
import com.mcmiddleearth.entities.ai.goal.Goal;
import com.mcmiddleearth.entities.ai.goal.GoalType;
import com.mcmiddleearth.entities.api.McmeEntityType;
import com.mcmiddleearth.entities.entities.McmeEntity;
import com.mcmiddleearth.entities.entities.RealPlayer;
import com.mcmiddleearth.entities.entities.VirtualEntity;
import com.mcmiddleearth.entities.entities.composite.SpeechBalloonEntity;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class EntitySelector<T extends Entity> implements Selector<T> {

    private final String selector;

    protected VirtualEntitySelector.SelectorType selectorType;
    protected int limit = Integer.MAX_VALUE;
    protected double x,y,z;
    protected boolean xRelative = true, yRelative = true, zRelative = true;
    protected double dx = -1, dy = -1, dz = -1;

    protected List<SelectorCondition<T>> conditions = new ArrayList<>();

    public EntitySelector(String selector) throws IndexOutOfBoundsException {
        this.selector = selector;
        selector = selector.replace(" ","");
        switch(selector.charAt(1)) {
            case 'a':
                selectorType = EntitySelector.SelectorType.ALL_PLAYERS;
                break;
            case 'e':
                selectorType = EntitySelector.SelectorType.ALL_ENTITIES;
                break;
            case 'v':
                selectorType = EntitySelector.SelectorType.VIRTUAL_ENTITIES;
                break;
            case 's':
                selectorType = EntitySelector.SelectorType.TRIGGER_ENTITY;
                break;
            case 'r':
                selectorType = EntitySelector.SelectorType.RANDOM_PLAYER;
                break;
            case 'p':
            default:
                selectorType = EntitySelector.SelectorType.NEAREST_PLAYER;
        }
        if(selector.length()>2) {
            if(selector.charAt(2) != '[' || selector.charAt(selector.length()-1)!=']') return;
            String[] arguments = selector.substring(3,selector.length()-1).split(",");
            for(String argument : arguments) {
                String[] split = argument.split("=");
                switch(split[0]) {
                    case "limit":
                        limit = Integer.parseInt(split[1]);
                        break;
                    case "x":
                        if (split[1].startsWith("~")) {
                            split[1] = split[1].substring(1);
                        } else {
                            xRelative = false;
                        }
                        if (split[1].length() > 0) {
                            x = Double.parseDouble(split[1]);
                        }
                        break;
                    case "y":
                        if (split[1].startsWith("~")) {
                            split[1] = split[1].substring(1);
                        } else {
                            yRelative = false;
                        }
                        if (split[1].length() > 0) {
                            y = Double.parseDouble(split[1]);
                        }
                        break;
                    case "z":
                        if (split[1].startsWith("~")) {
                            split[1] = split[1].substring(1);
                        } else {
                            zRelative = false;
                        }
                        if (split[1].length() > 0) {
                            z = Double.parseDouble(split[1]);
                        }
                        break;
                    case "dx":
                        dx = Double.parseDouble(split[1]);
                        break;
                    case "dy":
                        dy = Double.parseDouble(split[1]);
                        break;
                    case "dz":
                        dz = Double.parseDouble(split[1]);
                        break;
                    case "distance":
                        conditions.add(new DistanceSelectorCondition<>(split[1]));
                        break;
                    case "type":
                        conditions.add(NegatingSelectorCondition.parse(
                                split[1],
                                rest -> new McmeEntityTypeSelectorCondition<>(McmeEntityType.valueOf(rest.toUpperCase()))
                        ));
                        break;
                    case "name":
                        conditions.add(NegatingSelectorCondition.parse(split[1], NameSelectorCondition::new));
                        break;
                    case "gamemode":
                        conditions.add(NegatingSelectorCondition.parse(
                                split[1],
                                rest -> new GameModeSelectorCondition<>(GameMode.valueOf(rest.toUpperCase()))
                        ));
                        break;
                    case "x_rotation":
                        conditions.add(new PitchSelectorCondition<>(split[1]));
                        break;
                    case "y_rotation":
                        conditions.add(new YawSelectorCondition<>(split[1]));
                        break;
                    case "goal_type":
                        conditions.add(NegatingSelectorCondition.parse(
                                split[1],
                                rest -> new GoalTypeSelectorCondition<>(GoalType.valueOf(rest.toUpperCase()))
                        ));
                        break;
                    case "talking":
                        conditions.add(new TalkingSelectorCondition<>(split[1].equals("true")));
                        break;
                    case "tag":
                        conditions.add(NegatingSelectorCondition.parse(
                                split[1],
                                TagSelectorCondition::new
                        ));
                        break;
                }
            }

            if (hasAreaLimit()) {
                conditions.add(new AreaSelectorCondition<>());
            }
        }
    }

    @Override
    public List<T> select(TriggerContext context) {
        EntitySelectorContext<T> selectorContext = new EntitySelectorContext<>(this, context);

        List<T> targets = provideTargets(selectorContext);
        List<T> results = new ArrayList<>(targets.size());

        // Filter targets
        for (T entity : targets) {
            if (testEntity(selectorContext, entity)) {
                results.add(entity);
            }
        }

        // Bail early if no results were found
        if (results.isEmpty()) return results;

        // Sort targets if requested
        // TODO: sort
        // TODO: original code sorted entities by distance before returning the results if mix or max distance was given. is this significant?
        if (false) { // if (sort != null) {
            Location center = selectorContext.getCenter();
            if (center == null) throw new IllegalStateException("Selector " + getSelector() + " received no location");

            results = results.stream()
                    .map(entity -> {
                        EntitySelectorElement<T> element = new EntitySelectorElement<>(entity);
                        element.setValue(entity.getLocation().distanceSquared(center));
                        return element;
                    })
                    .sorted() // TODO
                    .map(EntitySelectorElement::getEntity)
                    .collect(Collectors.toList());
        }

        // Pick a single entity if the selector type demands it
        switch (selectorType) {
            case NEAREST_PLAYER: {
                Location center = selectorContext.getCenter();
                if (center == null) throw new IllegalStateException("Selector " + getSelector() + " received no location");

                T entity = null;
                double maxDistance = Double.MAX_VALUE;
                for (T result : results) {
                    double distance = result.getLocation().distanceSquared(center);
                    if (maxDistance > distance) {
                        maxDistance = distance;
                        entity = result;
                    }
                }

                results = Collections.singletonList(entity);
            } break;
            case RANDOM_PLAYER:
                results = Collections.singletonList(results.get(new Random().nextInt(results.size())));
                break;
            default:
                break;
        }

        // Return results
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    /**
     * Returns a list of all targets this selector can select from.
     */
    public abstract List<T> provideTargets(EntitySelectorContext<T> selectorContext);

    public List<Player> providePlayerTargets(EntitySelectorContext<T> selectorContext) {
        List<Player> targets = new ArrayList<>();
        TriggerContext context = selectorContext.getTriggerContext();

        switch (selectorType) {
            case TRIGGER_ENTITY:
                if (context.getPlayer() != null)
                    targets.add(context.getPlayer());
            case NEAREST_PLAYER:
            case ALL_PLAYERS:
            case ALL_ENTITIES:
            case RANDOM_PLAYER:
                //if party is set in context, select players from that party only
                if (context.isQuestContext()) {
                    targets.addAll(context.getParty().getOnlinePlayers());
                } else {
                    targets.addAll(Bukkit.getOnlinePlayers());
                }
            default:
                DebugManager.warn(
                        Modules.Selector.select(this.getClass()),
                        "Selector: " + getSelector() + " Invalid player entity selector type!"
                );
        }

        return targets;
    }

    public List<RealPlayer> provideMcmePlayerTargets(EntitySelectorContext<T> selectorContext) {
        return providePlayerTargets(selectorContext).stream()
                .map(EntitiesPlugin.getEntityServer().getPlayerProvider()::getOrCreateMcmePlayer)
                .collect(Collectors.toList());
    }

    public List<VirtualEntity> provideVirtualEntityTargets(EntitySelectorContext<T> selectorContext) {
        List<VirtualEntity> targets = new ArrayList<>();

        switch (selectorType) {
            case TRIGGER_ENTITY: {
                McmeEntity entity = selectorContext.getTriggerContext().getEntity();
                if (entity instanceof VirtualEntity)
                    targets.add((VirtualEntity) entity);
            } break;
            case VIRTUAL_ENTITIES:
            case ALL_ENTITIES:
                Collection<? extends McmeEntity> entities;
                Location center = selectorContext.getCenter();
                World world = center == null ? null : center.getWorld();

                if (hasAreaLimit() && center != null) {
                    // Provide area to the server in hopes of using a location-optimized lookup
                    entities = EntitiesPlugin.getEntityServer().getEntitiesAt(
                            center,
                            dx < 0 ? Integer.MAX_VALUE : (int) dx,
                            dy < 0 ? Integer.MAX_VALUE : (int) dy,
                            dz < 0 ? Integer.MAX_VALUE : (int) dz
                    );
                } else {
                    entities = EntitiesPlugin.getEntityServer().getEntities(VirtualEntity.class);
                }

                for (McmeEntity entity : entities) {
                    if (!(entity instanceof VirtualEntity) || entity instanceof SpeechBalloonEntity) continue;
                    if (world != null && !entity.getLocation().getWorld().equals(world)) continue;

                    targets.add((VirtualEntity) entity);
                }

                break;
            default:
                DebugManager.warn(
                        Modules.Selector.select(this.getClass()),
                        "Selector: " + getSelector() + " Invalid virtual entity selector type!"
                );
        }

        return targets;
    }

    public Location getAbsoluteLocation(TriggerContext context) {
        Location location = context.getLocation();
        if (location == null) return null;

        return new Location(
                location.getWorld(),
                getAbsolute(location.getX(), xRelative, x),
                getAbsolute(location.getY(), yRelative, y),
                getAbsolute(location.getZ(), zRelative, z)
        );
    }

    public boolean testEntity(EntitySelectorContext<T> context, T entity) {
        for (SelectorCondition<T> condition : conditions) {
            if (!condition.testEntity(context, entity)) return false;
        }

        return true;
    }

    public boolean hasAreaLimit() {
        return dx >= 0 || dy >=  0 || dz >= 0;
    }

    public double getAbsolute(double trigger, boolean relative, double selector) {
        if(relative) {
            return trigger+selector;
        } else {
            return selector;
        }
    }

    public String getSelector() {
        return selector;
    }

    public enum SelectorType {
        NEAREST_PLAYER,
        RANDOM_PLAYER,
        ALL_PLAYERS,
        VIRTUAL_ENTITIES,
        ALL_ENTITIES,
        TRIGGER_ENTITY
    }

    public static enum Order {
        NEAREST, FURTHEST, RANDOM, ARBITRARY;
    }

    /**
     * Instantiated once for every selector selection.
     */
    public static class EntitySelectorContext<T extends Entity> {
        private final EntitySelector<T> selector;
        private final TriggerContext context;
        private final Location location;

        public EntitySelectorContext(EntitySelector<T> selector, TriggerContext context) {
            this.selector = selector;
            this.context = context;

            location = selector.getAbsoluteLocation(context);
        }

        public EntitySelector<T> getSelector() {
            return selector;
        }

        public TriggerContext getTriggerContext() {
            return context;
        }

        public Location getCenter() {
            return location;
        }

        public double getDX() {
            return selector.dx;
        }

        public double getDY() {
            return selector.dy;
        }

        public double getDZ() {
            return selector.dz;
        }
    }

    public static class EntitySelectorElement<T extends Entity> {
        private final T entity;
        private double value;

        public EntitySelectorElement(T entity) {
            this.entity = entity;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public T getEntity() {
            return entity;
        }
    }

    private static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }

    private static final Pattern REGEX_RANGE_INTEGER = Pattern.compile("^(\\d+)\\.\\.(\\d+)$");
    private static final Pattern REGEX_RANGE_DECIMAL = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\.\\.(\\d+(?:\\.\\d+)?)$");

    public static <R> Pair<R, R> parseRange(String range, Pattern pattern, Function<String, R> parse) {
        Matcher matcher = pattern.matcher(range);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range: " + range);
        }

        R min = null;
        R max = null;

        String minString = matcher.group(1);
        String maxString = matcher.group(2);

        if (minString != null && minString.length() > 0) {
            min = parse.apply(minString);
        }
        if (maxString != null && maxString.length() > 0) {
            max = parse.apply(maxString);
        }

        return new Pair<>(min, max);
    }

    public static Pair<Integer, Integer> parseIntegerRange(String range) {
        return parseRange(range, REGEX_RANGE_INTEGER, Integer::parseInt);
    }

    public static Pair<Float, Float> parseFloatRange(String range) {
        return parseRange(range, REGEX_RANGE_DECIMAL, Float::parseFloat);
    }

    public static Pair<Double, Double> parseDoubleRange(String range) {
        return parseRange(range, REGEX_RANGE_DECIMAL, Double::parseDouble);
    }

    public interface SelectorCondition<T extends Entity> {
        public boolean testEntity(EntitySelectorContext<T> context, T entity);
    }

    public static class NegatingSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final SelectorCondition<T> innerSelector;

        public NegatingSelectorCondition(SelectorCondition<T> innerSelector) {
            this.innerSelector = innerSelector;
        }
        
        public SelectorCondition<T> getInnerSelector() {
            return innerSelector;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            return !innerSelector.testEntity(context, entity);
        }

        public static <T extends Entity, R extends SelectorCondition<T>> SelectorCondition<T> parse(String value, Function<String, R> parseRest) {
            if (value.length() > 0 && value.charAt(0) == '!') {
                return new NegatingSelectorCondition<>(parseRest.apply(value.substring(1)));
            }

            return parseRest.apply(value);
        }
    }

    public static class AreaSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private boolean testPosition(double entity, double center, double tolerance) {
            if (tolerance < 0) return true;

            return Math.abs(entity - center) <= tolerance;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            Location entityLocation = entity.getLocation();
            Location centerLocation = context.getCenter();

            if (entityLocation == null || centerLocation == null) return false;

            return testPosition(entityLocation.getX(), centerLocation.getX(), context.getDX())
                    && testPosition(entityLocation.getY(), centerLocation.getY(), context.getDY())
                    && testPosition(entityLocation.getZ(), centerLocation.getZ(), context.getDZ())
                    && entityLocation.getWorld().equals(centerLocation.getWorld());
        }
    }

    public static class DistanceSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private double minDistanceSquared = -1d;
        private double maxDistanceSquared = Double.MAX_VALUE;

        public DistanceSelectorCondition(String range) {
            Pair<Double, Double> minMax = parseDoubleRange(range);

            if (minMax.getFirst() != null) {
                minDistanceSquared = minMax.getFirst();
                minDistanceSquared *= minDistanceSquared;
            }
            if (minMax.getSecond() != null) {
                maxDistanceSquared = minMax.getSecond();
                maxDistanceSquared *= maxDistanceSquared;
            }
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            Location entityLocation = entity.getLocation();
            Location centerLocation = context.getCenter();

            if (entityLocation == null || centerLocation == null) return false;

            double distance = centerLocation.distanceSquared(entityLocation);
            return distance >= minDistanceSquared && distance <= maxDistanceSquared && entityLocation.getWorld().equals(centerLocation.getWorld());
        }
    }

    public static class PitchSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private float minPitch = -90;
        private float maxPitch = 90;

        public PitchSelectorCondition(String range) {
            Pair<Float, Float> minMax = parseFloatRange(range);

            if (minMax.getFirst() != null) {
                minPitch = minMax.getFirst();
            }
            if (minMax.getSecond() != null) {
                maxPitch = minMax.getSecond();
            }
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            float pitch = entity.getLocation().getPitch();
            return pitch >= minPitch && pitch <= maxPitch;
        }
    }

    public static class YawSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private float minYaw = -180;
        private float maxYaw = 180;

        public YawSelectorCondition(String range) {
            Pair<Float, Float> minMax = parseFloatRange(range);

            if (minMax.getFirst() != null) {
                minYaw = minMax.getFirst();
            }
            if (minMax.getSecond() != null) {
                maxYaw = minMax.getSecond();
            }
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            float yaw = entity.getLocation().getYaw();
            return yaw >= minYaw && yaw <= maxYaw;
        }
    }

    public static class McmeEntityTypeSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final McmeEntityType type;

        public McmeEntityTypeSelectorCondition(McmeEntityType type) {
            this.type = type;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            return entity instanceof McmeEntity && type.equals(((McmeEntity) entity).getMcmeEntityType());
        }
    }

    public static class NameSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final String name;
        private final boolean startsWith;

        public NameSelectorCondition(String name) {
            if (name.length() == 0) {
                startsWith = false;
                this.name = "";
            } else {
                startsWith = name.charAt(name.length() - 1) == '*';
                this.name = startsWith ? name.substring(0, name.length() - 1) : name;
            }
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            if (startsWith) return entity.getName().startsWith(name);
            return entity.getName().equals(name);
        }
    }

    public static class GameModeSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final GameMode gameMode;

        public GameModeSelectorCondition(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            if (entity instanceof Player) return gameMode.equals(((Player) entity).getGameMode());
            if (entity instanceof RealPlayer) return gameMode.equals(((RealPlayer) entity).getBukkitPlayer().getGameMode());
            return false;
        }
    }

    public static class GoalTypeSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final GoalType goalType;

        public GoalTypeSelectorCondition(GoalType goalType) {
            this.goalType = goalType;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            if (!(entity instanceof VirtualEntity)) return false;
            Goal goal = ((VirtualEntity) entity).getGoal();
            return goal != null && goalType.equals(goal.getType());
        }
    }

    public static class TalkingSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final boolean isTalking;

        public TalkingSelectorCondition(boolean isTalking) {
            this.isTalking = isTalking;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            if (!(entity instanceof VirtualEntity)) return false;
            return ((VirtualEntity) entity).isTalking() == isTalking;
        }
    }

    public static class TagSelectorCondition<T extends Entity> implements SelectorCondition<T> {
        private final String tag;

        public TagSelectorCondition(String tag) {
            this.tag = tag;
        }

        @Override
        public boolean testEntity(EntitySelectorContext<T> context, T entity) {
            if (entity instanceof VirtualEntity) ((VirtualEntity) entity).hasTag(tag);
            if (entity instanceof RealPlayer) ((RealPlayer) entity).hasTag(tag);
            return false;
        }
    }
}
