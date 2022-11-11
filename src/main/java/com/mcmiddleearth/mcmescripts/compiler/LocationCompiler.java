package com.mcmiddleearth.mcmescripts.compiler;

import com.google.gson.JsonElement;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

public class LocationCompiler {

    public static Optional<Location> compile(JsonElement element) {
        if(element == null) {
            //DebugManager.info(Modules.Location.create(LocationCompiler.class),"Can't compile location. Missing json element.");
            return Optional.empty();
        }
        String[] split = element.getAsString().split(",");
        if(split.length<4) {
            DebugManager.warn(Modules.Location.create(LocationCompiler.class),"Can't compile location. To few coordinates.");
            return Optional.empty();
        }
        World world = Bukkit.getWorld(split[0]);
        if(world == null) {
            DebugManager.warn(Modules.Location.create(LocationCompiler.class),"Can't compile location. World not found.");
            return Optional.empty();
        }
        try {
            double x = Double.parseDouble(split[1].trim());
            double y = Double.parseDouble(split[2].trim());
            double z = Double.parseDouble(split[3].trim());
            return Optional.of(new Location(world, x, y, z));
        } catch(NumberFormatException ex) {
            DebugManager.warn(Modules.Location.create(LocationCompiler.class),"Can't compile location. NumberFormatException.");
            return Optional.empty();
        }
    }
}
