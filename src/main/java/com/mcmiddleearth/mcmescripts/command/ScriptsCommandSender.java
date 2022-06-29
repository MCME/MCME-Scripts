package com.mcmiddleearth.mcmescripts.command;

import com.mcmiddleearth.command.McmeCommandSender;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ScriptsCommandSender implements McmeCommandSender {

    CommandSender sender;

    public ScriptsCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(BaseComponent[] baseComponents) {
        sendMessage(baseComponents, NamedTextColor.AQUA);
    }

    public void sendMessage(String message) {
        sendMessage(new ComponentBuilder().append(message).create(), NamedTextColor.AQUA);
    }

    public void sendError(String message) {
        sendMessage(new ComponentBuilder().append(message).create(), NamedTextColor.RED);
    }

    public void sendMessage(BaseComponent[] baseComponents, NamedTextColor color) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String legacy = Arrays.stream(baseComponents).map(comp -> comp.toLegacyText()).collect(Collectors.joining());
                sender.sendMessage(Identity.nil(), Component.text("[Scripts] ").color(color)
                                .append(Component.text(legacy)),
                        MessageType.SYSTEM);
            }
        }.runTask(MCMEScripts.getInstance());
    }


    public CommandSender getCommandSender() {
        return sender;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player getPlayer() {
        if(sender instanceof Player) {
            return (Player) sender;
        } else {
            return null;
        }
    }

}
