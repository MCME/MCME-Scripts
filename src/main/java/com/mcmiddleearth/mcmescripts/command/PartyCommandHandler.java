package com.mcmiddleearth.mcmescripts.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.command.AbstractCommandHandler;
import com.mcmiddleearth.command.SimpleTabCompleteRequest;
import com.mcmiddleearth.command.TabCompleteRequest;
import com.mcmiddleearth.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.command.builder.HelpfulRequiredArgumentBuilder;
import com.mcmiddleearth.mcmescripts.Permission;
import com.mcmiddleearth.mcmescripts.quest.party.Party;
import com.mcmiddleearth.mcmescripts.quest.party.PartyManager;
import com.mcmiddleearth.mcmescripts.quest.party.PartyPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class PartyCommandHandler extends AbstractCommandHandler implements TabExecutor {

    public PartyCommandHandler(String command) {
        super(command);
    }

    @Override
    protected HelpfulLiteralBuilder createCommandTree(HelpfulLiteralBuilder commandNodeBuilder) {
        commandNodeBuilder
            .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.USER.getNode()))
            .then(HelpfulLiteralBuilder.literal("create")
                .requires(sender -> ((ScriptsCommandSender)sender).isPlayer())
                .then(HelpfulRequiredArgumentBuilder.argument("name", word())
                    .executes(context -> {
                        String name = context.getArgument("name",String.class);
                        PartyManager.createParty(name,
                                                 PartyManager.getOrCreatePartyPlayer(((ScriptsCommandSender)context.getSource())
                                                             .getPlayer().getUniqueId()));
                        context.getSource().sendMessage("New party '"+name+"' is now your active party.");
                        return 0;
                    })))
            .then(HelpfulLiteralBuilder.literal("disband")
                .requires(sender -> ((ScriptsCommandSender)sender).isPlayer())
                .then(HelpfulRequiredArgumentBuilder.argument("name", word())
                    .executes(context -> {
                        String name = context.getArgument("name",String.class);
                        Party party = PartyManager.getParty(name);
                        if(party!=null) {
                            PartyManager.disbandParty(party.getUniqueId());
                            context.getSource().sendMessage("Party '" + name + "' disbanded.");
                        } else {
                            context.getSource().sendMessage("Party not found.");
                        }
                        return 0;
                    })))
            .then(HelpfulLiteralBuilder.literal("join")
                .requires(sender -> ((ScriptsCommandSender)sender).isPlayer())
                .then(HelpfulRequiredArgumentBuilder.argument("name", word())
                    .executes(context -> {
                        String name = context.getArgument("name",String.class);
                        Party party = PartyManager.getParty(name);
                        PartyPlayer player = PartyManager.getOrCreatePartyPlayer(((ScriptsCommandSender) context.getSource()).getPlayer().getUniqueId());
                        if(party!=null) {
                            if(PartyManager.joinParty(player, party)) {
                                context.getSource().sendMessage("You joined party '" + name + "'.");
                            } else {
                                context.getSource().sendMessage("You are already a member of party '" + name + "'.");
                            }
                        } else {
                            context.getSource().sendMessage("Party not found.");
                        }
                        return 0;
                    })))
            .then(HelpfulLiteralBuilder.literal("leave")
                .requires(sender -> ((ScriptsCommandSender)sender).isPlayer())
                .then(HelpfulRequiredArgumentBuilder.argument("name", word())
                    .executes(context -> {
                        String name = context.getArgument("name",String.class);
                        Party party = PartyManager.getParty(name);
                        PartyPlayer player = PartyManager.getOrCreatePartyPlayer(((ScriptsCommandSender) context.getSource()).getPlayer().getUniqueId());
                        if(party!=null) {
                            if(PartyManager.leaveParty(player, party)) {
                                context.getSource().sendMessage("You left party '" + name + "'.");
                            } else {
                                context.getSource().sendMessage("You are not a member of party '" + name + "'.");
                            }
                        } else {
                            context.getSource().sendMessage("Party not found.");
                        }
                        return 0;
                    })))
            .then(HelpfulLiteralBuilder.literal("list")
                .executes(context -> {
                    context.getSource().sendMessage("Loaded parties: ");
                    PartyManager.getParties().forEach(party -> {
                        context.getSource().sendMessage("- "+party.getName());
                    });
                    return 0;
                }))
            .then(HelpfulLiteralBuilder.literal("info")
                .then(HelpfulRequiredArgumentBuilder.argument("name", word())
                    .executes(context -> {
                        String name = context.getArgument("name",String.class);
                        Party party = PartyManager.getParty(name);
                        if(party!=null) {
                            context.getSource().sendMessage("Members of Party '" + name + "'.");
                            party.getPartyPlayers().forEach(player -> {
                                context.getSource().sendMessage("- "+player.getUniqueId()+ " "
                                        +(player.isOnline()?player.getOnlinePlayer().getName()+" ":"")
                                        +(party.equals(player.getActiveParty())?"(active)":""));
                            });
                        } else {
                            context.getSource().sendMessage("Party not found.");
                        }
                        return 0;
                    })));
        return commandNodeBuilder;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ScriptsCommandSender wrappedSender = new ScriptsCommandSender(sender);
        execute(wrappedSender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        TabCompleteRequest request = new SimpleTabCompleteRequest(new ScriptsCommandSender(sender),
                                                                  String.format("/%s %s", alias, Joiner.on(' ').join(args)));
        onTabComplete(request);
        return request.getSuggestions();
    }

}
