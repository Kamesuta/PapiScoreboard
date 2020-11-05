//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.extendedclip.papi.expansion.scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ObjectivesExpansion extends PlaceholderExpansion {
    public ObjectivesExpansion() {
    }

    public boolean canRegister() {
        return true;
    }

    public String getAuthor() {
        return "LethalBunny";
    }

    public String getName() {
        return "ScoreboardObjectives";
    }

    public String getIdentifier() {
        return "objective";
    }

    public String getVersion() {
        return "4.0.0";
    }

    public List<String> getPlaceholders() {
        List<String> list = new ArrayList();
        list.add(this.plc("displayname_<obj-name>"));
        list.add(this.plc("score_<obj-name>"));
        list.add(this.plc("score_<obj-name>_[otherEntry]"));
        list.add(this.plc("scorep_<obj-name>"));
        list.add(this.plc("scorep_<obj-name>_[otherPlayer]"));
        list.add(this.plc("displayname_{<obj-name>}"));
        list.add(this.plc("score_{<obj-name>}"));
        list.add(this.plc("score_{<obj-name>}_{[otherEntry]}"));
        list.add(this.plc("scorep_{<obj-name>}"));
        list.add(this.plc("scorep_{<obj-name>}_{[otherPlayer]}"));
        return list;
    }

    public String onRequest(OfflinePlayer player, String identifier) {
        ArrayList args;
        if (identifier.startsWith("scorep_{")) {
            args = getArgsAdv(identifier, "scorep_{");
            return parseScorePlayer(player, args);
        } else if (identifier.startsWith("scorep_")) {
            args = getArgs(identifier, "scorep_");
            return parseScorePlayer(player, args);
        } else if (identifier.startsWith("score_{")) {
            args = getArgsAdv(identifier, "score_{");
            return parseScoreAlone(player, args);
        } else if (identifier.startsWith("score_")) {
            args = getArgs(identifier, "score_");
            return parseScoreAlone(player, args);
        } else if (identifier.startsWith("scoret_{")) {
            args = getArgsAdv(identifier, "scoret_{");
            return parseScoreTop(player, args);
        } else if (identifier.startsWith("scoret_")) {
            args = getArgs(identifier, "scoret_");
            return parseScoreTop(player, args);
        } else if (identifier.startsWith("displayname_{")) {
            args = getArgsAdv(identifier, "displayname_{");
            return parseDisplayName(args);
        } else if (identifier.startsWith("displayname_")) {
            args = getArgs(identifier, "displayname_");
            return parseDisplayName(args);
        } else {
            return null;
        }
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        return this.onRequest(player, identifier);
    }

    private String plc(String str) {
        return "%" + this.getIdentifier() + "_" + str + "%";
    }

    private static ArrayList<String> getArgs(String str, String ident) {
        ArrayList<String> result = new ArrayList();
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
        ArrayList<String> result = new ArrayList();
        String argsStr = str.replace(ident, "");
        if (argsStr.contains("}_{")) {
            String[] args = argsStr.split(Pattern.quote("}_{"));

            try {
                result.add(args[0]);
                result.add(args[1].replaceAll(Pattern.quote("}"), ""));
            } catch (Exception var6) {
            }
        } else {
            result.add(argsStr.replaceAll(Pattern.quote("}"), ""));
        }

        return result;
    }

    private static String parseScorePlayer(OfflinePlayer player, ArrayList<String> args) {
        String objName = null;
        String entry = player == null ? null : player.getName();
        if (args.size() == 2) {
            objName = (String)args.get(0);
            entry = (String)args.get(1);
            String p = getOnlinePlayer(entry);
            if (p == null) {
                p = getOfflinePlayer(entry);
                if (p == null) {
                    return "PNF";
                }
            }
        } else {
            if (args.size() != 1) {
                return "";
            }

            objName = (String)args.get(0);
        }

        if (entry == null) {
            return "PNF";
        } else if (objName == null) {
            return "";
        } else {
            int score = getScore(objName, entry);
            return "" + score;
        }
    }

    private static String parseScoreAlone(OfflinePlayer player, ArrayList<String> args) {
        String objName = null;
        String entry = player == null ? null : player.getName();
        if (args.size() == 2) {
            objName = (String)args.get(0);
            entry = (String)args.get(1);
        } else {
            if (args.size() != 1) {
                return "";
            }

            objName = (String)args.get(0);
        }

        if (entry != null && objName != null) {
            int score = getScore(objName, entry);
            return "" + score;
        } else {
            return "";
        }
    }

    private static String parseScoreTop(OfflinePlayer player, ArrayList<String> args) {
        if (args.isEmpty())
            return "Missing args[0]";
        String objName = args.get(0);
        int ranking = args.size() <= 1 ? -1 : NumberUtils.toInt(args.get(1), -1);
        if (ranking == -1)
            return "Missing args[1]";
        String format = args.size() <= 2 ? "%s: %d" : args.get(2);
        String formatMe = args.size() <= 3 ? format : args.get(3);

        try {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective obj = board.getObjective(objName);
            if (obj == null)
                return "ObjScoreTop:NotFound";
            return board.getEntries().stream()
                    .map(obj::getScore)
                    .filter(Score::isScoreSet)
                    .map(Score::getScore)
                    .sorted(Comparator.reverseOrder())
                    .skip(ranking)
                    .limit(1)
                    .findFirst()
                    .map(e -> String.format(format, e))
                    .orElse("");
        } catch (Exception var4) {
            return "ObjScoreTop";
        }
    }

    private static String parseDisplayName(ArrayList<String> args) {
        if (args.size() >= 1) {
            String objName = (String)args.get(0);

            try {
                Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective obj = board.getObjective(objName);
                return obj.getDisplayName();
            } catch (Exception var4) {
                return "ObjDNE";
            }
        } else {
            return "";
        }
    }

    private static int getScore(String objName, String entry) {
        int num = 0;

        try {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective obj = board.getObjective(objName);
            Score score = obj.getScore(entry);
            num = score.getScore();
        } catch (Exception var6) {
        }

        return num;
    }

    private static String getOnlinePlayer(String player) {
        Iterator var2 = Bukkit.getOnlinePlayers().iterator();

        while(var2.hasNext()) {
            Player p = (Player)var2.next();
            if (p.getName().equalsIgnoreCase(player)) {
                return p.getName();
            }
        }

        return null;
    }

    private static String getOfflinePlayer(String player) {
        OfflinePlayer[] var4;
        int var3 = (var4 = Bukkit.getOfflinePlayers()).length;

        for(int var2 = 0; var2 < var3; ++var2) {
            OfflinePlayer p = var4[var2];
            if (p.getName().equalsIgnoreCase(player)) {
                return p.getName();
            }
        }

        return null;
    }
}
