package me.HeyAwesomePeople.VoteBar;


import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Listener implements org.bukkit.event.Listener {

    private VoteBar plugin = VoteBar.instance;

    @EventHandler
    public void onPlayerVote(VotifierEvent e) {
        if (plugin.players == null) {
            try {
                Date date = new Date();
                Date date3 = plugin.dateFormat.parse(plugin.dateFormat.format(date));
                plugin.playersVoted.put(e.getVote().getUsername(), date3);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            return;
        }
        // add a timestamp of the vote for the player in the config. This is how we determine how long a vote shall last.
        FileConfiguration config = plugin.getConfig();
        Vote v = e.getVote();
        try {
            Date now = new Date();
            if (config.contains("data." + plugin.players.get(v.getUsername()).toString() + ".votes")) {
                plugin.addVotes(plugin.players.get(v.getUsername()).toString(), 1, now);
            } else {
                List<String> list = new ArrayList<String>();
                list.add(plugin.dateFormat.format(now));
                config.set("data." + plugin.players.get(v.getUsername()).toString() + ".votes", list);
            }
            plugin.saveConfig();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    //TODO make sure our player join runs before the other plugins'

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("data." + e.getPlayer().getUniqueId() + ".votes")) {
            List<String> list = new ArrayList<String>();
            list.add("ignore");
            config.set("data." + e.getPlayer().getUniqueId() + ".votes", list);
            plugin.saveConfig();
        }
        final List<String> online = new ArrayList<String>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            online.add(p.getName());
        }
        plugin.players = null;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                try {
                    plugin.players = new UUIDFetcher(online).call();

                    FileConfiguration config = plugin.getConfig();
                    for (String user : plugin.playersVoted.keySet()) {
                        Date now = new Date();
                        if (config.contains("data." + plugin.players.get(user).toString() + ".votes")) {
                            plugin.addVotes(plugin.players.get(user).toString(), 1, now);
                        } else {
                            List<String> list = new ArrayList<String>();
                            list.add(plugin.dateFormat.format(now));
                            config.set("data." + plugin.players.get(user).toString() + ".votes", list);
                        }
                    }
                    plugin.playersVoted.clear();
                    plugin.saveConfig();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Fatal Error, contact HeyAwesomePeople!");
                }
            }
        });
    }

}
