package com.mcmiddleearth.mcmescripts.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.command.AbstractCommandHandler;
import com.mcmiddleearth.command.McmeCommandSender;
import com.mcmiddleearth.command.SimpleTabCompleteRequest;
import com.mcmiddleearth.command.TabCompleteRequest;
import com.mcmiddleearth.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.command.builder.HelpfulRequiredArgumentBuilder;
import com.mcmiddleearth.mcmescripts.ConfigKeys;
import com.mcmiddleearth.mcmescripts.MCMEScripts;
import com.mcmiddleearth.mcmescripts.Permission;
import com.mcmiddleearth.mcmescripts.command.arguments.ModuleArgument;
import com.mcmiddleearth.mcmescripts.command.arguments.ScriptArgument;
import com.mcmiddleearth.mcmescripts.command.arguments.TriggerArgument;
import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Level;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.drive.DriveUtil;
import com.mcmiddleearth.mcmescripts.listener.WandItemListener;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ScriptsCommandHandler extends AbstractCommandHandler implements TabExecutor {

    public ScriptsCommandHandler(String command) {
        super(command);
    }

    @Override
    protected HelpfulLiteralBuilder createCommandTree(HelpfulLiteralBuilder commandNodeBuilder) {
        commandNodeBuilder
            .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.USER.getNode()))
            .then(HelpfulLiteralBuilder.literal("debug")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.ADMIN.getNode()))
                .then(HelpfulLiteralBuilder.literal("modules")
                    .then(HelpfulRequiredArgumentBuilder.argument("module", new ModuleArgument(true, true))
                        .executes(context -> {
                            String module = context.getArgument("module",String.class);
                            Level level = DebugManager.cycleDebugLevel(module);
                            context.getSource().sendMessage("Setting debug module "+module+" to level "+level.name());
                            return 0; })
                        .then(HelpfulRequiredArgumentBuilder.argument("level",word())
                            .suggests(((commandContext, suggestionsBuilder) -> {
                                Arrays.stream(Level.values()).forEach(level -> suggestionsBuilder.suggest(level.name().toLowerCase()));
                                return suggestionsBuilder.buildFuture();}))
                            .executes(context -> {
                                String module = context.getArgument("module",String.class);
                                Level level = DebugManager.setDebugLevel(module,
                                        context.getArgument("level",String.class));
                                if(level == null) {
                                    context.getSource().sendError("Invalid debug level!");
                                } else {
                                    context.getSource().sendMessage("Setting debug module " + module + " to level " + level.name());
                                }
                                return 0; }))))
                .then(HelpfulLiteralBuilder.literal("filter")
                    .then(HelpfulRequiredArgumentBuilder.argument("player", word())
                        .then(HelpfulRequiredArgumentBuilder.argument("script", new ScriptArgument(false,true))
                            .executes(context -> {
                                String player = context.getArgument("player",String.class);
                                String script = context.getArgument("script",String.class);
                                setFilter(player, script);
                                context.getSource().sendMessage("Setting debug filter for "+player+" to script "+script);
                                return 0; })))))
            .then(HelpfulLiteralBuilder.literal("list")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.ADMIN.getNode()))
                .then(HelpfulRequiredArgumentBuilder.argument("module", new ModuleArgument(true,false))
                    .executes(context -> {
                        String module = context.getArgument("module",String.class);
                        context.getSource().sendMessage("Creating debug list for module "+module);
                        DebugManager.list(module);
                        return 0; })))
            .then(HelpfulLiteralBuilder.literal("reload")
                .executes(context -> {
                    context.getSource().sendMessage("Reloading scripts ....");
                    DebugManager.info(Modules.Command.execute(this.getClass()),"Reloading scripts ....");
                    MCMEScripts.getInstance().disableScripts();
                    Bukkit.getScheduler().runTaskLater(MCMEScripts.getInstance(), () -> {
                            MCMEScripts.getInstance().reloadConfig();
                            MCMEScripts.getInstance().enableScripts();
                            context.getSource().sendMessage("... scripts reloaded!");
                            DebugManager.info(Modules.Command.execute(this.getClass()),"... scripts reloaded!");}, 5);
                    return 0; }))
            .then(HelpfulLiteralBuilder.literal("disable")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.ADMIN.getNode()))
                .executes(context -> {
                    MCMEScripts.getInstance().disableScripts();
                    context.getSource().sendMessage("All scripts disabled!");
                    DebugManager.info(Modules.Command.execute(this.getClass()),"All scripts disabled!");
                    return 0; }))
            .then(HelpfulLiteralBuilder.literal("enable")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.ADMIN.getNode()))
                .executes(context -> {
                    context.getSource().sendMessage("Enabling scripts ....");
                    DebugManager.info(Modules.Command.execute(this.getClass()),"Enabling scripts ....");
                    MCMEScripts.getInstance().enableScripts();
                    DebugManager.info(Modules.Command.execute(this.getClass()),"... scripts enabled!");
                    return 0; }))
            .then(HelpfulLiteralBuilder.literal("wand")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender() instanceof Player)
                .then(HelpfulRequiredArgumentBuilder.argument("script", new ScriptArgument(true, false))
                    .then(HelpfulRequiredArgumentBuilder.argument("name", new TriggerArgument())
                        .executes(context -> {
                            bindWand(context);
                            return 0;
                        })
                        .then(HelpfulRequiredArgumentBuilder.argument("arguments",greedyString())
                            .executes(context -> {
                                bindWand(context);
                                return 0;
                            })))))
            .then(HelpfulLiteralBuilder.literal("external")
                .then(HelpfulRequiredArgumentBuilder.argument("script", new ScriptArgument(true, false))
                    .then(HelpfulRequiredArgumentBuilder.argument("name", new TriggerArgument())
                        .executes(context -> {
                            String script = context.getArgument("script",String.class);
                            String name = context.getArgument("name",String.class);
                            MCMEScripts.getExternalTriggerManager().call(script, name, new String[0]);
                            context.getSource().sendMessage("Calling event "+script+"."+name+".");
                            return 0;
                        })
                        .then(HelpfulRequiredArgumentBuilder.argument("arguments",greedyString())
                            .executes(context -> {
                                String script = context.getArgument("script",String.class);
                                String name = context.getArgument("name",String.class);
                                String arguments = context.getArgument("arguments", String.class);
                                MCMEScripts.getExternalTriggerManager().call(script, name, arguments.split(" "));
                                context.getSource().sendMessage("Calling event "+script+"."+name+"("+arguments+")");
                                return 0;
                            })))))
            .then(HelpfulLiteralBuilder.literal("import")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.ADMIN.getNode()))
                .then(HelpfulRequiredArgumentBuilder.argument("type",word())
                    .suggests((commandContext, suggestionsBuilder) -> suggestionsBuilder.suggest("animations").suggest("entities").suggest("scripts").buildFuture())
                    .then(HelpfulRequiredArgumentBuilder.argument("file",word())
                        .executes(context -> {
                            try {
                                String type = context.getArgument("type",String.class);
                                String file = context.getArgument("file",String.class);
                                DriveUtil.importFile(context.getSource(),type, file);
                                context.getSource().sendMessage("Imported "+type+" file "+file+".json from Google drive.");
                            } catch (IOException | GeneralSecurityException e) {
                                context.getSource().sendError("Error while accessing Google drive.");
                                e.printStackTrace();
                            }
                            return 0;
                        }))))
            .then(HelpfulLiteralBuilder.literal("export")
                .requires(sender -> ((ScriptsCommandSender)sender).getCommandSender().hasPermission(Permission.ADMIN.getNode()))
                .then(HelpfulRequiredArgumentBuilder.argument("type",word())
                    .suggests((commandContext, suggestionsBuilder) -> suggestionsBuilder.suggest("animations").suggest("entities").suggest("scripts").buildFuture())
                    .then(HelpfulRequiredArgumentBuilder.argument("file",word())
                        .executes(context -> {
                            try {
                                String type = context.getArgument("type",String.class);
                                String file = context.getArgument("file",String.class);
                                DriveUtil.exportFile(context.getSource(),type, file);
                                context.getSource().sendMessage("Exported "+type+" file "+file+".json to Google drive.");
                            } catch (IOException | GeneralSecurityException e) {
                                context.getSource().sendError("Error while accessing Google drive.");
                                e.printStackTrace();
                            }
                            return 0;
                        }))
                .then(HelpfulLiteralBuilder.literal("debug"))
                    .executes(context -> {
                        try {
                            DriveUtil.exportFile(context.getSource(),"scripts","debug.txt");
                            context.getSource().sendMessage("Exported debug.txt to Google drive.");
                        } catch (IOException | GeneralSecurityException e) {
                            context.getSource().sendError("Error while accessing Google drive.");
                            e.printStackTrace();
                        }
                        return 0;
                    })));
        return commandNodeBuilder;
    }

    private void setFilter(String playerName, String script) {
        DebugManager.info(Modules.Command.execute(this.getClass()),"Setting script filter for "+playerName +" to "+script);
        if("console".equalsIgnoreCase(playerName)) {
            DebugManager.setConsoleDebugScript(script);
        } else if("file".equalsIgnoreCase(playerName)) {
            DebugManager.setConsoleDebugScript(script);
        } else {
            Player player = Bukkit.getPlayer(playerName);
            if(player!=null) {
                DebugManager.setPlayerDebugScript(player, script);
            }
        }
    }

    private void bindWand(CommandContext<McmeCommandSender> context) {
        Player player = (Player) ((ScriptsCommandSender)context.getSource()).getCommandSender();
        String script = context.getArgument("script", String.class);
        String name = context.getArgument("name",String.class);
        String arguments = ".";
        try {
            arguments = "."+context.getArgument("arguments", String.class)+".";
        } catch (IllegalArgumentException ignore) {}
        if(!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            WandItemListener.addScript(player.getInventory().getItemInMainHand(), script + " " + name + arguments);
            context.getSource().sendMessage("Binding your hand item to event " + script + "." + name + arguments);
        } else {
            context.getSource().sendError("You need to hold an item in main hand.");
        }
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
