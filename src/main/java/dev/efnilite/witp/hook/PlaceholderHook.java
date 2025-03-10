package dev.efnilite.witp.hook;

import dev.efnilite.witp.WITP;
import dev.efnilite.witp.player.ParkourPlayer;
import dev.efnilite.witp.player.ParkourSpectator;
import dev.efnilite.witp.player.ParkourUser;
import dev.efnilite.witp.player.data.Highscore;
import dev.efnilite.witp.util.Util;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlaceholderHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "witp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Efnilite";
    }

    @Override
    public boolean canRegister(){
        return true;
    }
    
    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public @NotNull String getVersion() {
        return WITP.getInstance().getDescription().getVersion();
    }

    public static String translate(Player player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "player doesn't exist";
        }
        ParkourUser user = ParkourUser.getUser(player);
        ParkourPlayer pp = null;
        if (user instanceof ParkourPlayer) {
            pp = (ParkourPlayer) user;
        } else if (user instanceof ParkourSpectator) {
            pp = ((ParkourSpectator) user).getWatching().getPlayer();
        }

        if (pp != null) {
            switch (params) {
                case "score":
                case "current_score":
                    return Integer.toString(pp.getGenerator().score);
                case "time":
                case "current_time":
                    return pp.getGenerator().time;
                case "blocklead":
                case "lead":
                    return Integer.toString(pp.blockLead);
                case "style":
                    return pp.style;
                case "time_pref":
                case "time_preference":
                    return pp.time;
                case "scoreboard":
                    return pp.showScoreboard.toString();
                case "difficulty":
                    return Double.toString(pp.difficulty);
                case "difficulty_string":
                    return Util.parseDifficulty(pp.difficulty);
            }
        }

        switch (params) {
            case "highscore":
            case "high_score":
                Integer value = ParkourPlayer.getHighScoreValue(player.getUniqueId());
                if (value == null) {
                    value = 0;
                }
                return Integer.toString(value);
            case "version":
            case "ver":
                return WITP.getInstance().getDescription().getVersion();
            case "leader":
            case "record_player":
                UUID recordPlayer = ParkourPlayer.getAtPlace(1);
                return recordPlayer == null ? "N/A" : Bukkit.getOfflinePlayer(recordPlayer).getName();
            case "leader_score":
            case "record_score":
            case "record":
                UUID uuid = ParkourPlayer.getAtPlace(1);
                if (uuid == null) {
                    return "N/A";
                }
                Integer score = ParkourPlayer.getHighScoreValue(uuid);
                return score == null ? "N/A" : Integer.toString(score);
            default:
                if (params.contains("player_rank_")) {
                    String replaced = params.replaceAll("player_rank_", "");
                    int rank = Integer.parseInt(replaced);
                    if (rank > 0) {
                        UUID uuidRank = ParkourPlayer.getAtPlace(rank);
                        if (uuidRank == null) {
                            return "N/A";
                        }
                        Highscore highscore = ParkourPlayer.getHighScore(uuidRank);
                        if (highscore == null) {
                            return "N/A";
                        }
                        return highscore.name == null ? "N/A" : highscore.name;
                    } else {
                        return "N/A";
                    }
                } else if (params.contains("score_rank_")) {
                    String replaced = params.replaceAll("score_rank_", "");
                    int rank = Integer.parseInt(replaced);
                    if (rank > 0) {
                        UUID uuidRank1 = ParkourPlayer.getAtPlace(rank);
                        if (uuidRank1 == null) {
                            return "N/A";
                        }
                        Integer score1 = ParkourPlayer.getHighScoreValue(uuidRank1);
                        return score1 == null ? "N/A" : Integer.toString(score1);
                    } else {
                        return "N/A";
                    }
                } else if (params.contains("time_rank_")) {
                    String replaced = params.replaceAll("time_rank_", "");
                    int rank = Integer.parseInt(replaced);
                    if (rank > 0) {
                        UUID uuidRank1 = ParkourPlayer.getAtPlace(rank);
                        if (uuidRank1 == null) {
                            return "N/A";
                        }
                        String time = ParkourPlayer.getHighScoreTime(uuidRank1);
                        return time == null ? "N/A" : time;
                    } else {
                        return "N/A";
                    }
                }
        }

        return null;
    }
}
