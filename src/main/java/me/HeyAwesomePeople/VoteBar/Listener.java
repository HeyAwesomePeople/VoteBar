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
        for (SuperPlayer p : plugin.players.values()) {
            if (p.getPlayer().getName().equalsIgnoreCase(e.getVote().getUsername())) {
                p.addVotes(1);
                return;
            }
        }
        //TODO get offline player's UUID from CONFIG if possible, then check uuidfetcher.
        /* Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
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
        }); */
    }

    //TODO make sure our player join runs before the other plugins

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.players.put(e.getPlayer().getUniqueId(), new SuperPlayer(e.getPlayer()));
    }

}
