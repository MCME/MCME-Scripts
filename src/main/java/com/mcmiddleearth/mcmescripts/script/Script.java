package com.mcmiddleearth.mcmescripts.script;

import com.google.gson.JsonObject;
import com.mcmiddleearth.entities.EntitiesPlugin;
import com.mcmiddleearth.entities.entities.McmeEntity;
import com.mcmiddleearth.mcmescripts.compiler.ConditionCompiler;
import com.mcmiddleearth.mcmescripts.compiler.EntityCompiler;
import com.mcmiddleearth.mcmescripts.compiler.ScriptCompiler;
import com.mcmiddleearth.mcmescripts.compiler.TriggerCompiler;
import com.mcmiddleearth.mcmescripts.condition.Condition;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.trigger.Trigger;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;
import com.mcmiddleearth.mcmescripts.utils.JsonUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Script {

    private final String name;

    private final Set<Trigger> triggers = new HashSet<>();
    private final Set<McmeEntity> entities = new HashSet<>();

    private final Set<Condition> conditions;
    private boolean metAllConditions = true;

    private boolean active = false;

    private final File dataFile;

    public Script(File file) throws IOException {
        dataFile = file;
        JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
        assert jsonData != null;
        name = ScriptCompiler.getName(jsonData).orElse(System.currentTimeMillis()+"_"+Math.random());
        conditions = ConditionCompiler.compile(jsonData);
        if(!conditions.isEmpty()) metAllConditions = TriggerCompiler.getMetAllConditions(jsonData);
        DebugManager.info(Modules.Script.create(this.getClass()), getDescriptor().print(""));
    }

    public void load() throws IOException {
        if(!active) {
            JsonObject jsonData = JsonUtils.loadJsonData(dataFile);
            assert jsonData!=null;
            Set<Trigger> triggers = EntityCompiler.compile(jsonData);
            triggers.forEach(trigger -> trigger.register(this));
            triggers = TriggerCompiler.compile(jsonData);
            triggers.forEach(trigger -> trigger.register(this));
            //ScriptCompiler.load(jsonData,this);
            DebugManager.info(Modules.Script.load(this.getClass()), getDescriptor().print(""));
            active = true;
        }
    }

    public void unload() {
        new HashSet<>(triggers).forEach(Trigger::unregister);
        entities.forEach(entity -> EntitiesPlugin.getEntityServer().removeEntity(entity));
        entities.clear();
        active = false;
        DebugManager.info(Modules.Script.unload(this.getClass()), name);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isTriggered() {
        if(conditions.isEmpty()) return true;
        Script instance = this;
        TriggerContext context = new TriggerContext(new Trigger() {
            @Override
            public Script getScript() {
                return instance;
            }
            @Override
            public void call(TriggerContext context) {}
        });
        for(Condition condition: conditions) {
            boolean testResult = condition.test(context);
            if(metAllConditions && !testResult) {
                return false;
            } else if(!metAllConditions && testResult) {
                return true;
            }
        }
        return metAllConditions;
    }

    public void addEntity(McmeEntity entity) {
        entities.add(entity);
    }

    public void removeEntity(McmeEntity entity) {
        entities.remove(entity);
    }

    public void addTrigger(Trigger trigger) {
        triggers.add(trigger);
    }

    public void removeTrigger(Trigger trigger) {
        triggers.remove(trigger);
    }

    public String getName() {
        return name;
    }

    public File getDataFile() {
        return dataFile;
    }

    public Set<Trigger> getTriggers(String name) {
        if(name.equals("*")) return new HashSet<>(triggers);
        if(name.endsWith("*")) {
            return triggers.stream().filter(trigger->trigger.getName()!=null
                                                  && trigger.getName().startsWith(name.substring(0,name.length()-1)))
                    .collect(Collectors.toSet());
        } else {
            return triggers.stream().filter(trigger->trigger.getName()!=null && trigger.getName().equals(name))
                    .collect(Collectors.toSet());
        }
    }

    public Set<Trigger> getTriggers() {
        return triggers;
    }

    public Set<McmeEntity> getEntities() {
        return entities;
    }

    public Descriptor getDescriptor() {
        Descriptor descriptor = new Descriptor("Script: "+name)
                .indent()
                .addLine("Data file: "+dataFile.getName())
                .addLine("Met all conditions: "+metAllConditions)
                .addLine("Active: "+active);
        if(!conditions.isEmpty()) {
            descriptor.addLine("Conditions:").indent();
            conditions.forEach(condition -> descriptor.add(condition.getDescriptor()));
            descriptor.outdent();
        } else {
            descriptor.addLine("Conditions: --none--");
        }
        if(!triggers.isEmpty()) {
            descriptor.addLine("Triggers:").indent();
            triggers.forEach(trigger -> descriptor.add(trigger.getDescriptor()));
            descriptor.outdent();
        } else {
            descriptor.addLine("Triggers: --none--");
        }
        if(!entities.isEmpty()) {
            descriptor.addLine("Entities:").indent();
            entities.forEach(entity -> descriptor.addLine(entity.getName()+" at "+entity.getLocation()));
        } else {
            descriptor.addLine("Entities: --none--");
        }
        return descriptor;
    }

    /*public String print(String indent) {
        String subIndent = DebugManager.INDENT;
        StringBuilder builder = new StringBuilder();
        builder.append(getDescriptor().print(indent));
        if(!conditions.isEmpty()) {
            builder.append(indent).append(subIndent).append("Conditions:").append("\n");
            conditions.forEach(condition -> builder.append(condition.getDescriptor().print(indent+subIndent+subIndent)));
        } else {
            builder.append(indent).append(subIndent).append("Conditions: --none--").append("\n");
        }
        if(!triggers.isEmpty()) {
            builder.append(indent).append(subIndent).append("Triggers:").append("\n");
            triggers.forEach(trigger -> builder.append(trigger.print(indent+subIndent+subIndent)));
        } else {
            builder.append(indent).append(subIndent).append("Triggers: --none--").append("\n");
        }
        if(!entities.isEmpty()) {
            builder.append(indent).append(subIndent).append("Triggers:").append("\n");
            entities.forEach(entity -> builder.append(indent).append(subIndent).append(subIndent)
                                              .append(entity.getName())
                                              .append(" at ").append(entity.getLocation().toString()));
        } else {
            builder.append(indent).append(subIndent).append("Entities: --none--").append("\n");
        }
        return builder.toString();
    }*/
}
