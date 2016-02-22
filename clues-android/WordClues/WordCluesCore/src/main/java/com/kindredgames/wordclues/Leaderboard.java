package com.kindredgames.wordclues;

import java.util.List;

public class Leaderboard {

    public static void loadLeaderboardsWithCompletionHandler(LeaderboardCompletionHandler handler) {
        handler.handle(null, null);
    }

    public static void loadScoresWithCompletionHandler(ScoreCompletionHandler handler) {
        handler.handle(null, null);
    }


    public Leaderboard() {
        super();
    }

    public String identifier;
    public Score localPlayerScore;

    public interface LeaderboardCompletionHandler {
        void handle(List<Leaderboard> leaderboards, Error error);
    }

    public interface ScoreCompletionHandler {
        void handle(List<Score> lbScores, Error error);
    }

}
