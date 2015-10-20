package me.HeyAwesomePeople.VoteBar;


import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listener implements org.bukkit.event.Listener {

    private VoteBar plugin = VoteBar.instance;

    @EventHandler
    public void onPlayerVote(final VotifierEvent e) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "votebar add " + e.getVote().getUsername() + " 1");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.players.put(e.getPlayer().getUniqueId(), new SuperPlayer((OfflinePlayer) e.getPlayer()));
        plugin.players.get(e.getPlayer().getUniqueId()).saveUsername();
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
            plugin.players.get(e.getPlayer().getUniqueId()).saveData();
            plugin.players.get(e.getPlayer().getUniqueId()).remove();
        }
    }

}
