package me.HeyAwesomePeople.VoteBar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class TagScoreboard
{
    private static VoteBar plugin = VoteBar.instance;

    public static void createScoreboard(Player p) {
        if (!plugin.players.containsKey(p.getUniqueId())) return;

        if (p.getScoreboard() != null) {
            Scoreboard board = p.getScoreboard();
            if (board.getObjective("votebarTag") != null) {
                Objective obj = board.getObjective("votebarTag");
                obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
                board.resetScores("[" + plugin.players.get(p.getUniqueId()).getVoteHashes() + "]");
                p.setScoreboard(board);
            }
            return;
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("votebarTag", "votebarthingy");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        board.resetScores("[" + plugin.players.get(p.getUniqueId()).getVoteHashes() + "]");
        p.setScoreboard(board);
    }
}