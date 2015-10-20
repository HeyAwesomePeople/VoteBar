package me.HeyAwesomePeople.VoteBar;


import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SuperPlayer {
    private VoteBar plugin = VoteBar.instance;

    private OfflinePlayer player = null;
    public List<String> voted = new ArrayList<String>();
    public Integer oldPercent = 0;
    public HashMap<Integer, BukkitTask> tasks = new HashMap<Integer, BukkitTask>();

    public SuperPlayer(OfflinePlayer p) {
        player = p;
        this.loadData();
        this.updatePercentage();
    }

    public Integer getVotes() {
        cleanVotes();
        return voted.size();
    }

    public Integer getVoteDoublePercentage() {
        cleanVotes();
        return (int) ((double) (this.getVotes() * 100.0f) / plugin.config.getInt("maxVotes"));
    }


    /*********** Placeholder Calls ************/

    public String getVoteHashes() {
        updatePercentage();
        String fin = "";
        for (int i = 1; i <= plugin.config.getInt("maxVotes"); i++) {
            if (i <= this.getVotes()) {
                fin += "#";
            } else {
                fin += "#";
            }
        }

        StringBuilder sb = new StringBuilder(fin);
        sb.insert(0, "&3");
        sb.insert(this.getVotes() + 2, "&4");
        return "" + sb.toString();
    }

    public String getVotePercentage() {
        updatePercentage();
        return (double) (this.getVotes() * 100.0f) / plugin.config.getInt("maxVotes") + "%";
    }

    public String getVoteSlash() {
        updatePercentage();
        return this.getVotes() + "/" + plugin.config.getInt("maxVotes") + "%";
    }

    public void cleanVotes() {
        for (String l : new ArrayList<String>(voted)) {
            if (l.equalsIgnoreCase("ignore")) {
                removeInstanceOf("ignore", voted);
                continue;
            }
            if (Methods.getDateDiff(Methods.stringToDate(l), new Date(), TimeUnit.MINUTES) > 30) {
                removeInstanceOf(l, voted);
            }
        }
        saveData();
    }

    public void updatePercentage() {
        int perc = getVoteDoublePercentage();
        boolean increasing;
        increasing = perc > oldPercent;
        if (perc != oldPercent) {
            // Cancel any repeating tasks
            for (BukkitTask bt : tasks.values()) {
                bt.cancel();
            }
            // Run singleton commands
            for (int i = Math.max(oldPercent, perc); i >= Math.min(oldPercent, perc); i--) {
                if (plugin.config.contains("run." + i)) {
                    if (increasing) {
                        for (String s : plugin.config.getStringList("run." + perc + ".singleCommand.gainedPercent")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", this.player.getName()));
                        }
                    } else {
                        for (String s : plugin.config.getStringList("run." + oldPercent + ".singleCommand.lostPercent")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", this.player.getName()));
                        }
                    }
                }
            }
            this.oldPercent = perc;

            this.startRepeater(this.player, perc);
        } else {
            // Make sure repeating commands running
            if (!tasks.containsKey(perc)) {
                this.startRepeater(this.player, perc);
            }
        }
    }

    /************* Editing Votes ****************/

    public void addVotes(Integer i) {
        cleanVotes();
        for (int r = 1; r <= i; r++) {
            if (voted.isEmpty()) {
                voted.add(Methods.dateToString(new Date()));
                continue;
            }
            if (voted.get(voted.size() - 1).equalsIgnoreCase("ignore")) {
                voted.clear();
                voted.add(Methods.dateToString(new Date()));
                continue;
            }
            Date pre = Methods.stringToDate(voted.get(voted.size() - 1));
            voted.add(Methods.dateToString(DateUtils.addMinutes(new Date(), plugin.config.getInt("depletionTime") - (int) Methods.getDateDiff(pre, new Date(), TimeUnit.MINUTES))));
        }
        saveData();
    }

    public void removeVotes(Integer i) {
        cleanVotes();
        for (int loops = 1; loops >= i; loops++) {
            if (voted.isEmpty()) continue;
            voted.remove(voted.get(0));
        }
        saveData();
    }

    public void removeInstanceOf(String s, List<String> list) {
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            if (itr.next().equals(s))
                itr.remove();
        }
    }

    public void setVotes(Integer i) {
        this.voted.clear();
        addVotes(i);
    }

    public void startRepeater(final OfflinePlayer p, final Integer perc) {
        long interval;
        if (plugin.config.contains("run." + perc + ".repeatingCommand")) {
            interval = plugin.config.getInt("run." + perc + ".repeatingCommand.interval");
        } else {
            return;
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                if (plugin.config.contains("run." + perc + ".repeatingCommand")) {
                    if (!(plugin.config.getInt("run." + perc + ".repeatingCommand.chance") >= getRandomNumber(0, 100))) {
                        return;
                    }
                        for (String s : plugin.config.getStringList("run." + perc + ".repeatingCommand.commands")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", p.getName()));
                        }
                }
            }
        }, 5L, interval);
        tasks.put(perc, task);
    }

    private Random r = new Random();

    public Integer getRandomNumber(int min, int max) {
        return r.nextInt(max-min) + min;
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /* * Config Options * */

    public void saveUsername() {
        plugin.config.set("data." + this.player.getUniqueId().toString() + ".username", this.player.getName());
        plugin.saveConfig();
    }

    public void saveData() {
        plugin.config.set("data." + this.player.getUniqueId().toString() + ".votes", this.voted);
        plugin.config.set("data." + this.player.getUniqueId().toString() + ".lastPercent", this.oldPercent);
        this.saveUsername();
    }

    public void loadData() {
        if (!plugin.config.contains("data." + this.player.getUniqueId().toString() + ".votes")) {
            this.saveUsername();
            this.saveData();
            plugin.saveConfig();
        }
        this.voted = plugin.config.getStringList("data." + this.player.getUniqueId().toString() + ".votes");
        this.oldPercent = plugin.config.getInt("data." + this.player.getUniqueId().toString() + ".lastPercent");
    }

    public void remove() {
        for (BukkitTask bt : tasks.values()) {
            bt.cancel();
        }
        tasks.clear();
        plugin.players.remove(this.player.getUniqueId());
    }

}
