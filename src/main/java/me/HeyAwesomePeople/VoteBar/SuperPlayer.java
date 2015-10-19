package me.HeyAwesomePeople.VoteBar;


import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SuperPlayer {
    private VoteBar plugin = VoteBar.instance;

    private Player player = null;
    public List<String> voted = new ArrayList<String>();
    public double oldPercent = 0.0;
    public HashMap<Double, BukkitTask> tasks = new HashMap<Double, BukkitTask>();

    public SuperPlayer(Player p) {
        player = p;
        this.loadData();
    }

    public void updatePercentage(double perc) {
        if (perc != oldPercent) {
            // Run singleton commands
            this.oldPercent = perc;
        } else {
            // Make sure repeating commands running
        }
    }

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

    public Integer getVotes() {
        cleanVotes();
        return voted.size();
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

    public void removeVotes(Integer i) {
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

    public void startRepeater(final Player p, final Double perc) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
            }
        }, 0L, 0L);
        tasks.put(perc, task);
    }

    public String getVoteHashes() {
        cleanVotes();
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
        cleanVotes();
        return (double) (this.getVotes() * 100.0f) / plugin.config.getInt("maxVotes") + "%";
    }

    public String getVoteSlash() {
        cleanVotes();
        return this.getVotes() + "/" + plugin.config.getInt("maxVotes") + "%";
    }

    public Player getPlayer() {
        return this.player;
    }

    /* * Config Options * */
    //TODO

    public void saveData() {

    }

    public void loadData() {

    }

}
