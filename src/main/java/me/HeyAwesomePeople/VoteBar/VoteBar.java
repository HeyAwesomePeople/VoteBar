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

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Map<String, UUID> players = null;
    public HashMap<String, Date> playersVoted = new HashMap<String, Date>();
    public HashMap<UUID, String> playerPercent = new HashMap<UUID, String>();
    private File configf = new File(this.getDataFolder() + File.separator + "config.yml");
    public String percentage = "#";

    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        try {
            players = new UUIDFetcher(Arrays.asList("HeyAwesomePeople")).call();
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Fatal Error, contact HeyAwesomePeople!");
            Bukkit.shutdown();
        }

        instance = this;
        getServer().getPluginManager().registerEvents(new me.HeyAwesomePeople.VoteBar.Listener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "votebar", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    return getPercentage(placeholderReplaceEvent.getPlayer()).split(":")[0];
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "votebarpercent", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    return getPercentage(placeholderReplaceEvent.getPlayer()).split(":")[1];
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "votebarminmax", new PlaceholderReplacer() {
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    return getPercentage(placeholderReplaceEvent.getPlayer()).split(":")[2];
                }
            });
        }
        //TODO variables {Bar} {Percent}

        if (!configf.exists()) {
            FileConfiguration config = getConfig();

            config.set("maxVotes", 10);
            config.set("depletionTime", 30);// minutes
            //TODO detect if this value is changed. If so, change all votes according to it
            config.set("run.10.repeatingCommand.interval", 60);// seconds
            config.set("run.10.repeatingCommand.gainedPercent", "manuaddp %player votebar.admin");
            config.set("run.10.repeatingCommand.lostPercent", "manudelp %player votebar.admin");
            config.set("run.10.singleCommand.gainedPercent", "msg %player Good job! You got to 10%");
            config.set("run.10.singleCommand.lostPercent", "msg %player You lost 10%...");

            List<String> list = new ArrayList<String>();
            Date now = new Date();
            list.add(dateFormat.format(now));
            config.set("data.HeyAwesomePeople.votes", list);
            saveConfig();
            reloadConfig();
        }
    }

    @Override
    public void onDisable() {
        reloadConfig();
    }

    public String getPercentage(Player p) {
        cleanVotes(p.getUniqueId().toString());
        List<String> listofStrings = new ArrayList<String>();
        int points = 0;
        if (config.contains("data." + p.getUniqueId().toString() + ".votes")) {
            List<String> votes = config.getStringList("data." + p.getUniqueId().toString() + ".votes");
            for (String s : votes) {
                try {
                    if (s.equalsIgnoreCase("ignore")) continue;
                    long minutes = getDateDiff(dateFormat.parse(s), new Date(), TimeUnit.MINUTES);
                    if (minutes > config.getInt("depletionTime")) continue;
                    points++;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 1; i <= config.getInt("maxVotes"); i++) {
                if (i <= points) {
                    listofStrings.add("&3#");
                } else {
                    listofStrings.add("&4#");
                }
            }
        } else {
            return "#Error";
        }

        String fin = "";
        for (String s : listofStrings) {
            fin += s;
        }

        // fin = ########:50%:5/10

        fin += ":";
        fin += "" + (double) (points * 100.0f) / config.getInt("maxVotes") + "%";
        fin += ":";
        fin += points + "/" + config.getInt("maxVotes");

        if (playerPercent.containsKey(p.getUniqueId())) {
            double perc = Double.parseDouble(playerPercent.get(p.getUniqueId()));
            double newperc = Double.parseDouble(fin.split(":")[1].replace("%", ""));
            double difference = Math.abs(perc - newperc);
            if (newperc - perc > 0) { // adding
                for (int c = 1; c <= 100; c++) {
                    if (config.contains("run." + Integer.toString(c) + ".singleCommand.gainedPercent")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), config.getString("run." + Integer.toString(c) + ".singleCommand.gainedPercent").replace("%player", p.getName()));
                    }
                }
            } else if (newperc - perc < 0) { // subtracting
                for (int c = 1; c <= 100; c++) {
                    if (config.contains("run." + Integer.toString(c) + ".singleCommand.lostPercent")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), config.getString("run." + Integer.toString(c) + ".singleCommand.gainedPercent").replace("%player", p.getName()));
                    }
                }
            }
        }
        playerPercent.put(p.getUniqueId(), fin.split(":")[1].replace("%", ""));

        return fin;
    }

    public void startRepeater(final Player p) {

    }

    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        Player p = (Player) sender;
        if (commandLabel.equalsIgnoreCase("votebar")) {
            Date now = new Date();
            if (config.contains("data." + p.getUniqueId().toString() + ".votes")) {
                this.addVotes(p.getUniqueId().toString(), 1, now);
            } else {
                List<String> list = new ArrayList<String>();
                list.add(dateFormat.format(now));
                config.set("data." + p.getUniqueId().toString() + ".votes", list);
            }
            saveConfig();
        }
        return false;
    }

    // ****** Methods ****** //

    public Date stringToDate(String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String dateToString(Date d) {
        return dateFormat.format(d);
    }

    public void addVotes(String uuid, Integer i, Date d) {
        List<String> list = config.getStringList("data." + uuid + ".votes");
        for (int r = 1; r <= i; r++) {
            if (list.isEmpty()) {
                list.add(dateFormat.format(d));
                continue;
            }
            if (list.get(list.size() - 1).equalsIgnoreCase("ignore")) {
                list.add(dateFormat.format(d));
                continue;
            }
            Date pre = stringToDate(list.get(list.size() - 1));
            Bukkit.broadcastMessage("Mins: " + getDateDiff(pre, new Date(), TimeUnit.MINUTES));
            d = DateUtils.addMinutes(d, 30 - (int) getDateDiff(pre, new Date(), TimeUnit.MINUTES));
            list.add(dateFormat.format(d));
        }
        config.set("data." + uuid + ".votes", list);
        saveConfig();
    }

    public void removeVotes(String uuid, Integer i) {
        List<String> list = config.getStringList("data." + uuid + ".votes");
        int loops = 0;
        for (String l : new ArrayList<String>(list)) {
            if (l.equalsIgnoreCase("ignore")) continue;
            if (loops == i) break;
            list.remove(list.get(0));
            loops++;
        }
        config.set("data." + uuid + ".votes", list);
        saveConfig();
    }

    public void cleanVotes(String uuid) {
        List<String> list = config.getStringList("data." + uuid + ".votes");
        for (String l : new ArrayList<String>(list)) {
            if (l.equalsIgnoreCase("ignore")) continue;
            if (getDateDiff(stringToDate(l), new Date(), TimeUnit.MINUTES) > 30) {
                list.remove(list.get(0));
            }
        }
        config.set("data." + uuid + ".votes", list);
        saveConfig();
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }


}
