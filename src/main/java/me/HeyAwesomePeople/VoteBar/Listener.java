package me.HeyAwesomePeople.VoteBar;


import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.ParseException;
import java.util.*;

public class Listener implements org.bukkit.event.Listener {

    private VoteBar plugin = VoteBar.instance;

    @EventHandler
    public void onPlayerVote(final VotifierEvent e) {
        for (SuperPlayer p : plugin.players.values()) {
            if (p.getPlayer().getName().equalsIgnoreCase(e.getVote().getUsername())) {
                p.addVotes(1);
                return;
            }
        }

        for (String s : plugin.config.getConfigurationSection("data").getKeys(false)) {
            if (plugin.config.getString("data." + s + ".username").equalsIgnoreCase(e.getVote().getUsername())) {
                if (plugin.players.containsKey(UUID.fromString(s))) {
                    plugin.players.get(UUID.fromString(s)).addVotes(1);
                    return;
                }
            }
        }


        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                try {
                    Map<String, UUID> fetch = new UUIDFetcher(Collections.singletonList(e.getVote().getUsername())).call();

                    plugin.players.put(fetch.get(e.getVote().getUsername()), new SuperPlayer(Bukkit.getPlayer(fetch.get(e.getVote().getUsername()))));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Fatal Error!, contact HeyAwesomePeople!");
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.players.put(e.getPlayer().getUniqueId(), new SuperPlayer(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (plugin.players.containsKey(e.getPlayer().getUniqueId())) {
            plugin.players.get(e.getPlayer().getUniqueId()).remove();
        }
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent e) {
        if (plugin.players.containsKey(e.getPlayer().getUniqueId())) {
            plugin.players.get(e.getPlayer().getUniqueId()).remove();
        }
    }

}
