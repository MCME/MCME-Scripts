package com.mcmiddleearth.mcmescripts.trigger;

import com.mcmiddleearth.entities.ai.goal.Goal;
import com.mcmiddleearth.entities.entities.McmeEntity;
import com.mcmiddleearth.entities.events.events.McmeEntityEvent;
import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.quest.party.Party;
import com.mcmiddleearth.mcmescripts.quest.Stage;
import com.mcmiddleearth.mcmescripts.script.Script;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TriggerContext {

    private final Trigger trigger;

    private Player player;
    private String message;
    private String name;

    private Party party;

    private boolean firstJoin;

    private McmeEntity entity;

    private Location location;

    private Goal goal;

    private McmeEntityEvent entityEvent;

    private Descriptor descriptor;

    public TriggerContext(Trigger trigger) {
        this.trigger = trigger;
        if(trigger.getScript() instanceof Stage) {
            party = ((Stage) trigger.getScript()).getQuest().getParty();
        }
        this.location = trigger.getLocation();
        this.player = (trigger.getPlayer()!=null?trigger.getPlayer().selectPlayer(this).stream().findFirst().orElse(null):null);
        this.entity = (trigger.getEntity()!=null?trigger.getEntity().select(this).stream().findFirst().orElse(null):null);
        descriptor = new Descriptor("Event Log:").indent()
                .addLine(trigger.getClass().getSimpleName()+": "+trigger.getScript().getName()+"."+trigger.getName())
                .addLine("Event location: "+(location!=null?location:"--none--"))
                .addLine("Event player: "+(player!=null?player.getName():"--none--"))
                .addLine("Event entity: "+(entity!=null?entity.getName():"--none--"));
    }

    public TriggerContext(TriggerContext context) {
        this.player = context.player;
        this.trigger = context.trigger;
        this.party = context.party;
        this.message = context.message;
        this.firstJoin = context.firstJoin;
        this.entity = context.entity;
        this.goal = context.goal;
        this.location = context.location;
        this.entityEvent = context.entityEvent;
        this.name = context.name;
        this.descriptor = context.descriptor;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Script getScript() {
        return  trigger.getScript();
    }

    public Player getPlayer() {
        return player;
    }

    public TriggerContext withPlayer(Player player) {
        this.player = player;
        getDescriptor().addLine("Override event player: " + player.getName());
        return this;
    }

    public Party getParty() {
        return party;
    }

    public TriggerContext withParty(Party party) {
        this.party = party;
        return this;
    }

    public boolean isQuestContext() {
        return party != null;
    }

    public String getMessage() {
        return message;
    }

    public TriggerContext withMessage(String message) {
        this.message = message;
        getDescriptor().addLine("Message: "+message);
        return this;
    }

    public String getName() {
        return name;
    }

    public TriggerContext withName(String name) {
        this.name = name;
        getDescriptor().addLine("Context name: "+name);
        return this;
    }

    public McmeEntityEvent getEntityEvent() {
        return entityEvent;
    }

    public TriggerContext withEntityEvent(McmeEntityEvent entityEvent) {
        this.entityEvent = entityEvent;
        getDescriptor().addLine("Entity event: "+entityEvent.getClass().getSimpleName());
        return this;
    }

    public boolean isFirstJoin() {
        return firstJoin;
    }

    public TriggerContext withFirstJoin(boolean firstJoin) {
        this.firstJoin = firstJoin;
        getDescriptor().addLine("First join: "+firstJoin);
        return this;
    }

    public McmeEntity getEntity() {
        return entity;
    }

    public TriggerContext withEntity(McmeEntity entity) {
        this.entity = entity;
        getDescriptor().addLine("Override event entity: " + entity.getName());
        return this;
    }

    public Location getLocation() {
        /*if(location!=null) {
            return location;
        } else if(trigger.getLocation()!=null) {
            return trigger.getLocation();
        } else if(trigger.getPlayer()!=null) {
            return trigger.getPlayer().getLocation();
        } else if(trigger.getEntity()!=null) {
            return trigger.getEntity().getLocation();
        } else if(player!=null) {
            return player.getLocation();
        } else if(entity!=null) {
            return entity.getLocation();
        } else {
            return null;
        }*/
        return location;
    }

    public TriggerContext withLocation(Location location) {
        this.location = location;
        getDescriptor().addLine("Override event location: "+location);
        return this;
    }

    public Goal getGoal() {
        return goal;
    }

    public TriggerContext withGoal(Goal goal) {
        this.goal = goal;
        getDescriptor().addLine("Context goal: "+(goal!=null?goal.getType().name():"--none--"));
        return this;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
    }
}
