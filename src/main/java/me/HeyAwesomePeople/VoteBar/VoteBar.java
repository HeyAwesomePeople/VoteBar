package me.HeyAwesomePeople.VoteBar;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
                    return players.get(placeholderReplaceEvent.getPlayer().getUniqueId()).getVoteHashes();
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "votebarpercent", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    return players.get(placeholderReplaceEvent.getPlayer().getUniqueId()).getVotePercentage();
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "votebarminmax", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    return players.get(placeholderReplaceEvent.getPlayer().getUniqueId()).getVoteSlash();
                }
            });
        }
        //TODO variables {Bar} {Percent}

        if (!configf.exists()) {
            List<String> cmds = new ArrayList<String>();
            cmds.add("manuaddp %player votebar.admin");
            cmds.add("msg %player Good Job!");

            config.set("maxVotes", 10);
            config.set("depletionTime", 30);// minutes
            //TODO detect if this value is changed. If so, change all votes according to it
            config.set("run.10.repeatingCommand.interval", 60);// seconds
            config.set("run.10.repeatingCommand.gainedPercent", cmds);
            config.set("run.10.repeatingCommand.lostPercent", new ArrayList<String>());
            config.set("run.10.singleCommand.gainedPercent", cmds);
            config.set("run.10.singleCommand.lostPercent", new ArrayList<String>());
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
        Player p = (Player) sender;
        if (commandLabel.equalsIgnoreCase("votebar")) {
            if (players.containsKey(p.getUniqueId())) {
                players.get(p.getUniqueId()).addVotes(1);
            } else {
                p.sendMessage(ChatColor.RED + "[VoteBar] SuperPlayer data not found!");
            }
        }
        return false;
    }
}
