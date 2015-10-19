package me.HeyAwesomePeople.VoteBar;


import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SuperPlayer {
    private VoteBar plugin = VoteBar.instance;

    private Player player = null;
    public List<String> voted = new ArrayList<String>();
    public Integer oldPercent = 0;
    public HashMap<Integer, BukkitTask> tasks = new HashMap<Integer, BukkitTask>();

    public SuperPlayer(Player p) {
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
        String fin = "";
        for (int i = 1; i <= plugin.config.getInt("maxVotes"); i++) {
            if (i <= this.getVotes()) {
                fin += "&3#";
            } else {
                fin += "&4#";
            }
        }
        return fin;
    }

    public String getVotePercentage() {
        return (double) (this.getVotes() * 100.0f) / plugin.config.getInt("maxVotes") + "%";
    }

    public String getVoteSlash() {
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
        updatePercentage();
    }

    public void updatePercentage() {
        int perc = getVoteDoublePercentage();
        boolean increasing = false;
        //TODO loop through the difference of the percents to find all config commands that work for it. SINGLETON
        increasing = perc > oldPercent;
        if (perc != oldPercent) {
            // Cancel any repeating tasks
            for (BukkitTask bt : tasks.values()) {
                bt.cancel();
            }
            // Run singleton commands
            for (int i = Math.max(oldPercent, perc); i <= Math.min(oldPercent, perc); i--) {
                if (plugin.config.contains("run." + i)) {
                    if (increasing) {
                        for (String s : plugin.config.getStringList("run." + perc + ".singleCommand.gainedPercent")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", this.player.getName()));
                        }
                    } else {
                        for (String s : plugin.config.getStringList("run." + perc + ".singleCommand.lostPercent")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", this.player.getName()));
                        }
                    }
                }
            }
            this.oldPercent = perc;

            this.startRepeater(this.player, perc, increasing);
        } else {
            // Make sure repeating commands running
            if (!tasks.containsKey(perc)) {
                this.startRepeater(this.player, perc, increasing);
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
        int loops = 1;
        for (String l : new ArrayList<String>(voted)) {
            if (loops > i) break;
            voted.remove(voted.get(0));
            loops++;
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

    public void startRepeater(final Player p, final Integer perc, final Boolean gained) {
        long interval = 1;
        if (plugin.config.contains("run." + perc + ".repeatingCommand")) {
            interval = plugin.config.getInt("run." + perc + ".repeatingCommand.interval");
        } else {
            return;
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                if (plugin.config.contains("run." + perc + ".repeatingCommand")) {
                    //TODO test chance
                    if (!(plugin.config.getInt("run." + perc + ".repeatingCommand.chance") >= getRandomNumber(0, 100))) {
                        return;
                    }
                    if (gained) {
                        for (String s : plugin.config.getStringList("run." + perc + ".repeatingCommand.gainedPercent")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", p.getName()));
                        }
                    } else {
                        for (String s : plugin.config.getStringList("run." + perc + ".repeatingCommand.lostPercent")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player", p.getName()));
                        }
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

    public Player getPlayer() {
        return this.player;
    }

    /* * Config Options * */

    public void saveUsername() {
        plugin.config.set("data." + this.player.getUniqueId().toString() + ".votes", this.player.getName());
        plugin.saveConfig();
    }

    public void saveData() {
        plugin.config.set("data." + this.player.getUniqueId().toString() + ".votes", this.voted);
        plugin.config.set("data." + this.player.getUniqueId().toString() + ".lastPercent", this.oldPercent);
        plugin.saveConfig();
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
        plugin.players.remove(this.player.getUniqueId());
    }

}
