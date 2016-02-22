package com.kindredgames.wordclues.amazon;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum AmazonWordCluesLeaderboards {

    TOTAL_POINTS("com_kindredgames_wordclues_MostPoints", "totalPoints"),
    GAMES_PLAYED("com_kindredgames_wordclues_GamesPlayed", "gamesPlayed"),
    LONGEST_STREAK("com_kindredgames_wordclues_LongestStreak", "longestStreak"),
    CURRENT_STREAK("com_kindredgames_wordclues_CurrentStreak", "currentStreak"),
    HIGH_SCORE("com_kindredgames_wordclues_highScore", "highScore"),
    AVG_POINTS("com_kindredgames_wordclues_avgPoints", "avgPoints");

    private String id;
    private String name;

	private AmazonWordCluesLeaderboards(String id, String name) {
		this.id = id;
        this.name = name;
	}

    public static AmazonWordCluesLeaderboards valueForId(String id) {
        Set<String> keys = LEADERBOARDS.keySet();
        for (String key : keys) {
            AmazonWordCluesLeaderboards sku = LEADERBOARDS.get(key);
            if (sku.getId().equals(id)) {
                return sku;
            }
        }
        return null;
    }

    public static AmazonWordCluesLeaderboards valueForName(String name) {
        return LEADERBOARDS.get(name);
    }

    public String getId() {
		return id;
	}

    public String getName() {
        return name;
    }

	private static Map<String, AmazonWordCluesLeaderboards> LEADERBOARDS = new HashMap<String, AmazonWordCluesLeaderboards>();
	static {
        LEADERBOARDS.put(TOTAL_POINTS.getName(), TOTAL_POINTS);
        LEADERBOARDS.put(GAMES_PLAYED.getName(), GAMES_PLAYED);
        LEADERBOARDS.put(LONGEST_STREAK.getName(), LONGEST_STREAK);
        LEADERBOARDS.put(CURRENT_STREAK.getName(), CURRENT_STREAK);
        LEADERBOARDS.put(HIGH_SCORE.getName(), HIGH_SCORE);
        LEADERBOARDS.put(AVG_POINTS.getName(), AVG_POINTS);
	}

	public static Set<String> getAll() {
		return LEADERBOARDS.keySet();
	}

}
