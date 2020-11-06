package net.teamfruit.papi.expansion.leaderboard;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LeaderboardExpansion extends PlaceholderExpansion {

    public boolean canRegister() {
        return true;
    }

    public @NotNull String getAuthor() {
        return "Kamesuta";
    }

    public @NotNull String getName() {
        return "ScoreboardLeaderboard";
    }

    public @NotNull String getIdentifier() {
        return "leaderboard";
    }

    public @NotNull String getVersion() {
        return "1.0.0";
    }

    public @NotNull List<String> getPlaceholders() {
        return Collections.singletonList(this.plc("rank_<obj-name>_<ranking>"));
    }

    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.startsWith("rank_{"))
            return parseScoreTop(player, getArgsAdv(identifier, "rank_{"));
        else if (identifier.startsWith("rank_"))
            return parseScoreTop(player, getArgs(identifier, "rank_"));
        else
            return null;
    }

    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        return this.onRequest(player, identifier);
    }

    private String plc(String str) {
        return "%" + this.getIdentifier() + "_" + str + "%";
    }

    private static ArrayList<String> getArgs(String str, String ident) {
        ArrayList<String> result = new ArrayList<>();
        String argsStr = str.replace(ident, "");
        String[] args = argsStr.split("_");
        if (args.length == 2) {
            result.add(args[0]);
            result.add(args[1]);
        } else {
            result.add(args[0]);
        }

        return result;
    }

    private static ArrayList<String> getArgsAdv(String str, String ident) {
        ArrayList<String> result = new ArrayList<>();
        String argsStr = str.replace(ident, "");
        if (argsStr.contains("}_{")) {
            List<String> args = Arrays.asList(argsStr.split(Pattern.quote("}_{")));

            try {
                result.addAll(args.subList(0, args.size() - 1));
                result.add(args.get(args.size() - 1).replaceAll(Pattern.quote("}"), ""));
            } catch (Exception ignored) {
            }
        } else {
            result.add(argsStr.replaceAll(Pattern.quote("}"), ""));
        }

        return result;
    }

    private static String parseScoreTop(OfflinePlayer player, ArrayList<String> args) {
        if (args.isEmpty())
            return "Missing args[0]";
        String objName = args.get(0);
        int ranking = args.size() <= 1 ? -1 : NumberUtils.toInt(args.get(1), -1);
        if (ranking == -1)
            return "Missing args[1]";
        String format = args.size() <= 2 ? "%d) %s: %d" : args.get(2).replace("％", "%").replace("＆", "§");
        String formatMe = args.size() <= 3 ? format : args.get(3).replace("％", "%").replace("＆", "§");

        Optional<String> playerName = Optional.ofNullable(player).map(OfflinePlayer::getName);

        try {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective obj = board.getObjective(objName);
            if (obj == null)
                return "ObjScoreTop:NotFound";
            List<AbstractMap.SimpleEntry<String, Integer>> list = board.getEntries().stream()
                    .map(e -> {
                        Score score = obj.getScore(e);
                        if (!score.isScoreSet())
                            return null;
                        int value = score.getScore();
                        return new AbstractMap.SimpleEntry<>(e, value);
                    })
                    .filter(Objects::nonNull)
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            return IntStream.range(0, list.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<>(i + 1, list.get(i)))
                    .skip(ranking)
                    .limit(1)
                    .findFirst()
                    .map(e -> String.format(playerName.map(f ->
                                    f.equals(e.getValue().getKey())).orElse(false)
                                    ? formatMe
                                    : format,
                            e.getKey(), e.getValue().getKey(), e.getValue().getValue()))
                    .orElse("");
        } catch (Exception e) {
            return "ObjScoreTop:" + e;
        }
    }

}
