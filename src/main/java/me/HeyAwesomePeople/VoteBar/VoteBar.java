package me.HeyAwesomePeople.VoteBar;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class VoteBar extends JavaPlugin implements CommandExecutor {
    public static VoteBar instance;

    public HashMap<UUID, SuperPlayer> players = new HashMap<UUID, SuperPlayer>();
    private File configf = new File(this.getDataFolder() + File.separator + "config.yml");
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new me.HeyAwesomePeople.VoteBar.Listener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "votebar", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    if (!players.containsKey(placeholderReplaceEvent.getPlayer().getUniqueId())) {
                        return "Error.";
                    }
                    return players.get(placeholderReplaceEvent.getPlayer().getUniqueId()).getVoteHashes();
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "votebarpercent", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    if (!players.containsKey(placeholderReplaceEvent.getPlayer().getUniqueId())) {
                        return "Error.";
                    }
                    return players.get(placeholderReplaceEvent.getPlayer().getUniqueId()).getVotePercentage();
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "votebarminmax", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    if (!players.containsKey(placeholderReplaceEvent.getPlayer().getUniqueId())) {
                        return "Error.";
                    }
                    return players.get(placeholderReplaceEvent.getPlayer().getUniqueId()).getVoteSlash();
                }
            });
        }
        //TODO variables {Bar} {Percent}

        if (!configf.exists()) {
            List<String> cmds = new ArrayList<String>();
            cmds.add("say %player 10 repeating gained");

            config.set("maxVotes", 10);
            config.set("depletionTime", 30);// minutes
            //TODO detect if this value is changed. If so, change all votes according to it
            config.set("run.10.repeatingCommand.interval", 100);// ticks
            config.set("run.10.repeatingCommand.chance", 50); // percentage
            config.set("run.10.repeatingCommand.commands", new ArrayList<String>(cmds));
            config.set("run.10.singleCommand.gainedPercent", new ArrayList<String>(cmds));
            config.set("run.10.singleCommand.lostPercent", new ArrayList<String>(cmds));
            config.set("data.0f91ede5-54ed-495c-aa8c-d87bf405d2bb.votes", new ArrayList<String>());
            saveConfig();
        }
    }

    @Override
    public void onDisable() {
        reloadConfig();
    }


    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        if (!sender.hasPermission("votebar.admin")) {
            sender.sendMessage(ChatColor.RED + "No permissions.");
            return false;
        }
        if (commandLabel.equalsIgnoreCase("votebar")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "[VoteBar]");
                sender.sendMessage(ChatColor.AQUA + "/votebar add <player> <amount>");
                sender.sendMessage(ChatColor.AQUA + "/votebar remove <player> <amount>");
                sender.sendMessage(ChatColor.AQUA + "/votebar set <player> <amount>");
            } else {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Not enough arguments.");
                    return false;
                }

                OfflinePlayer p = null;
                p = (OfflinePlayer) Bukkit.getPlayer(args[1]);
                if (p == null) {
                    for (String s : config.getConfigurationSection("data").getKeys(false)) {
                        if (config.contains("data." + s + ".username")) {
                            p = Bukkit.getOfflinePlayer(UUID.fromString(s));
                        }
                    }
                }
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found! (Has never played before!)");
                    return false;
                }

                SuperPlayer sp = null;
                if (players.containsKey(p.getUniqueId())) {
                    sp = players.get(p.getUniqueId());
                } else {
                    sp = new SuperPlayer(p);
                }

                if (args[0].equalsIgnoreCase("add")) {
                    if (!isInteger(args[2])) {
                        sender.sendMessage(ChatColor.RED + "Amount of votes to add must be a number!");
                        return false;
                    }
                    sp.addVotes(Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.AQUA + "[VoteBar] Added " + args[2] + " votes.");
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!isInteger(args[2])) {
                        sender.sendMessage(ChatColor.RED + "Amount of votes to add must be a number!");
                        return false;
                    }
                    sp.removeVotes(Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.AQUA + "[VoteBar] Removed " + args[2] + " votes.");
                    return true;
                } else if (args[0].equalsIgnoreCase("set")) {
                    if (!isInteger(args[2])) {
                        sender.sendMessage(ChatColor.RED + "Amount of votes to add must be a number!");
                        return false;
                    }
                    sp.setVotes(Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.AQUA + "[VoteBar] Set " + args[2] + " votes.");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand!");
                }
            }
        }
        return false;
    }

    public Boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
