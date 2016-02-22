package com.kindredgames.wordclues.amazon;

import android.app.Activity;
import android.content.Context;

import com.amazon.ags.api.AGResponseCallback;
import com.amazon.ags.api.AGResponseHandle;
import com.amazon.ags.api.AmazonGamesCallback;
import com.amazon.ags.api.AmazonGamesClient;
import com.amazon.ags.api.AmazonGamesFeature;
import com.amazon.ags.api.AmazonGamesStatus;
import com.amazon.ags.api.achievements.Achievement;
import com.amazon.ags.api.achievements.AchievementsClient;
import com.amazon.ags.api.achievements.GetAchievementsResponse;
import com.amazon.ags.api.leaderboards.GetLeaderboardsResponse;
import com.amazon.ags.api.leaderboards.GetPlayerScoreResponse;
import com.amazon.ags.api.leaderboards.Leaderboard;
import com.amazon.ags.api.leaderboards.LeaderboardsClient;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;
import com.amazon.ags.api.player.Player;
import com.amazon.ags.api.player.PlayerClient;
import com.amazon.ags.api.player.RequestPlayerResponse;
import com.amazon.ags.constants.LeaderboardFilter;
import com.kindredgames.wordclues.GameCenterManager;
import com.kindredgames.wordclues.ResultCallback;
import com.kindredgames.wordclues.SignalCallback;
import com.kindredgames.wordclues.util.KGLog;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AmazonGameCircleManager implements GameCenterManager, AmazonGamesCallback {

//    public static final String LEADERBOARD_ID_BASE = "com_kindredgames_wordclues_";
    public static final String ACHIEVEMENT_ID_BASE = "";

    protected Activity activity;
    protected Context context;

    AmazonGamesClient agsClient;
    PlayerClient playerClient;
    Player currentUserPlayer = null;
    protected List<Leaderboard> currentUserLeaderboards;
    protected Map<String, Long> currentUserLeaderboardScores;
    protected List<Achievement> currentUserAchievements;

    public AmazonGameCircleManager(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.currentUserLeaderboardScores = new ConcurrentHashMap<String, Long>();
    }

    private SignalCallback serviceReadyCallback;

    @Override
    public void connect(final SignalCallback callback) {
        if (agsClient != null && agsClient.isInitialized()) {
            KGLog.d("agsClient already initialized");
            return;
        }
//        if (serviceReadyCallback != null) {
//            KGLog.d("There is a pending AGS request");
//            return;
//        }
        if (callback != null) {
            this.serviceReadyCallback = callback;
        }
        final AmazonGamesCallback gamesCallback = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EnumSet<AmazonGamesFeature> gameFeatures = EnumSet.of(
                        AmazonGamesFeature.Leaderboards
                        , AmazonGamesFeature.Achievements
                        //, AmazonGamesFeature.Whispersync
                );
                AmazonGamesClient.initialize(activity, gamesCallback, gameFeatures);
                KGLog.d("Connecting to GameCircle");
            }
        });
    }

    @Override
    public void onServiceNotReady(AmazonGamesStatus status) {
        KGLog.w("Amazon Games Circle not ready: %s", status.toString());
        currentUserPlayer = null;
        if (serviceReadyCallback != null) {
            synchronized (serviceReadyCallback) {
                serviceReadyCallback.onFault();
                serviceReadyCallback = null;
            }
        }
    }

    @Override
    public void onServiceReady(AmazonGamesClient amazonGamesClient) {
        agsClient = amazonGamesClient;
        playerClient = agsClient.getPlayerClient();
        //AGResponseHandle<RequestPlayerResponse> playerRequest = playerClient.getLocalPlayer(null);
        playerClient.getLocalPlayer().setCallback(
                new AGResponseCallback<RequestPlayerResponse>() {
                    @Override
                    public void onComplete(RequestPlayerResponse r) {
                        currentUserPlayer = r.getPlayer();
                        if (currentUserPlayer != null) {
                            KGLog.d("Amazon Games Circle currentUserPlayer: %s/%s", currentUserPlayer.getAlias(), currentUserPlayer.getPlayerId());
                        } else {
                            KGLog.d("No Amazon Games Circle currentUserPlayer");
                        }
                        if (serviceReadyCallback != null) {
                            synchronized (serviceReadyCallback) {
                                serviceReadyCallback.onResult();
                                serviceReadyCallback = null;
                            }
                        }
                    }
                }
        );
        KGLog.d("Amazon Games Circle is ready");
    }

    @Override
    public void resume() {
        connect(null);
    }

    @Override
    public void pause() {
        if (agsClient != null) {
            agsClient.release();
            KGLog.d("Released agsClient");
            agsClient = null;
        }
    }

    @Override
    public void disconnect() {
        pause();
    }

    @Override
    public boolean isAuthenticated() {
        if (agsClient != null) {
            return agsClient.getPlayerClient().isSignedIn();
        }
        return false;
    }

    @Override
    public String getPlayerId() {
        return currentUserPlayer != null ? currentUserPlayer.getPlayerId() : null;
    }

    @Override
    public String getPlayerAlias() {
        return currentUserPlayer != null ? currentUserPlayer.getAlias() : null;
    }

    @Override
    public boolean checkGameCenterAvailability() {
        return agsClient != null;
    }

    @Override
    public void displayGameCenter() {
        if (agsClient != null) {
            agsClient.showGameCircle();
        } else {
            KGLog.e("Can't show GameCircle: null client. Trying to reconnect");
            connect(new SignalCallback() {
                @Override
                public void onResult() {
                    if (agsClient != null) {
                        agsClient.showGameCircle();
                    }
                }

                @Override
                public void onFault() {
                    KGLog.e("Giving up connecting to Amazon Game Circle");
                }

            });
        }
    }

    @Override
    public void loadLeaderboards(final ResultCallback<JSONObject> messageCallback) {
        if (isAuthenticated()) {
            KGLog.d("WS => AGResponseHandle<GetLeaderboardsResponse>");
            AGResponseHandle<GetLeaderboardsResponse> handle = agsClient.getLeaderboardsClient().getLeaderboards();
            handle.setCallback(new AGResponseCallback<GetLeaderboardsResponse>() {
                @Override
                public void onComplete(GetLeaderboardsResponse getLeaderboardsResponse) {
                    if (getLeaderboardsResponse.isError()) {
                        String fault = getLeaderboardsResponse.getError().toString();
                        KGLog.w("Error getting leaderboards: %s", fault);
                        messageCallback.onFault(fault);
                    } else {
                        currentUserLeaderboards = getLeaderboardsResponse.getLeaderboards();
                        // A workaround for if fewer leaderboards are supported than available on GameCircle
                        for (int i = currentUserLeaderboards.size() - 1; i >= 0; i--) {
                            Leaderboard leaderboard = currentUserLeaderboards.get(i);
                            if (getLeaderboardName(leaderboard.getId()) == null) {
                                KGLog.d("Ignoring unsupported Leaderboard: %s", leaderboard.getId());
                                currentUserLeaderboards.remove(i);
                            }
                        }
                        loadScores(messageCallback);
                    }
                }
            });
        }
    }

    public void loadScores(ResultCallback<JSONObject> messageCallback) {
        JSONObject scores = new JSONObject();
        for (Leaderboard leaderboard : currentUserLeaderboards) {
            loadScore(messageCallback, scores, leaderboard);
        }
    }

    private void loadScore(final ResultCallback<JSONObject> messageCallback, final JSONObject scores, final Leaderboard leaderboard) {
        KGLog.d("WS => AGResponseCallback<GetPlayerScoreResponse>");
        agsClient.getLeaderboardsClient().getLocalPlayerScore(leaderboard.getId(), LeaderboardFilter.GLOBAL_ALL_TIME, leaderboard).setCallback(new AGResponseCallback<GetPlayerScoreResponse>() {
            @Override
            public void onComplete(GetPlayerScoreResponse getPlayerScoreResponse) {
                if (getPlayerScoreResponse.isError()) {
                    // to expect - possibly due to a network failure
                    messageCallback.onFault(String.format("Error loading score for leaderboard %@: %", leaderboard.getName(), getPlayerScoreResponse.getError().toString()));
                } else {
                    try {
                        synchronized (scores) {
                            String name = getLeaderboardName(leaderboard.getId());
                            if (name != null) {
                                long scoreValue = getPlayerScoreResponse.getScoreValue();
                                scores.put(name, scoreValue < 0 ? 0 : scoreValue); // -1 appears to be the default value. Assuming we don't have negatives
                                currentUserLeaderboardScores.put(name, Long.valueOf(scoreValue));
                                KGLog.d("Leaderboard: %s score=%d", name, scoreValue);
                                if (scores.length() >= currentUserLeaderboards.size()) {
//                                    JSONObject message = new JSONObject();
//                                    message.put("topic", "leaderboards");
//                                    message.put("scores", scores);
                                    messageCallback.onResult(scores);
                                }
                            }
                        }
                    } catch (JSONException exc) {
                        KGLog.e("Error creating Leaderboards JSON: %s", exc.toString());
                    }
                }
            }
        });
    }

    @Override
    public void loadAchievements(final ResultCallback<JSONObject> messageCallback) {
        if (agsClient != null) {
            KGLog.d("WS => AGResponseCallback<GetAchievementsResponse>");
            agsClient.getAchievementsClient().getAchievements().setCallback(new AGResponseCallback<GetAchievementsResponse>() {
                @Override
                public void onComplete(GetAchievementsResponse result) {
                    if (result.isError()) {
                        messageCallback.onFault(result.getError().toString());
                    } else {
                        currentUserAchievements = result.getAchievementsList();
                        messageCallback.onResult(getAchievementValues());
                    }
                }
            });
        }
    }

    @Override
    public void updateAchievements(JSONObject values) {
        if (agsClient != null) {
            AchievementsClient achi = agsClient.getAchievementsClient();
            Iterator<String> keys = values.keys();
            while (keys.hasNext()) {
                String name = keys.next();
                try {
                    achi.updateProgress(name, (float)values.getJSONObject(name).getDouble("value"));
                } catch (JSONException e) {
                    KGLog.e("Error parsing JSON: %s", e.toString());
                }
            }
        }
    }

    @Override
    public void updateLeaderboards(JSONObject values) {
        if (currentUserLeaderboards != null) {
            LeaderboardsClient lbClient = agsClient.getLeaderboardsClient();
            try {
                for (Leaderboard leaderboard : currentUserLeaderboards) {
                    final String name = getLeaderboardName(leaderboard.getId());
                    if (name != null) {
                        long value = values.getLong(name);
                        currentUserLeaderboardScores.put(name, Long.valueOf(value));
                        AGResponseHandle<SubmitScoreResponse> handle = lbClient.submitScore(leaderboard.getId(), value);
                        handle.setCallback(new AGResponseCallback<SubmitScoreResponse>() {
                            @Override
                            public void onComplete(SubmitScoreResponse submitScoreResponse) {
                                if (submitScoreResponse.isError()) {
                                    KGLog.w("Error on submitted Leaderboard %s: %s", name, submitScoreResponse.getError().toString());
                                } else {
                                    KGLog.d("Updated Leaderboard %s, %s", name, submitScoreResponse.getNewRank().keySet().toString());
                                }
                            }
                        });
                    }
                }
            } catch (JSONException exc) {
                KGLog.w("Error submitting Leaderboard values: %s", exc.toString());
            }
        }
    }

    @Override
    public JSONObject getLeaderboardValues() {
        JSONObject leaderboards = new JSONObject();
        if (currentUserLeaderboards != null) {
            for (Leaderboard leaderboard : currentUserLeaderboards) {
                String name = getLeaderboardName(leaderboard.getId());
                if (name != null) {
                    try {
                        leaderboards.put(name, getLeaderboardValue(name));
                    } catch (JSONException exc) {
                        KGLog.e("Error getting Leaderboard %s score: %s", name, exc.toString());
                    }
                }
            }
        }
        return leaderboards;
    }

    @Override
    public int getLeaderboardValue(String tokenName) {
        Long score = currentUserLeaderboardScores.get(tokenName);
        return score != null ? score.intValue() : -1;
    }

    @Override
    public JSONObject getLeaderboardValues(JSONArray message) {
        return getLeaderboardValues();
    }

    @Override
    public JSONObject getAchievementValues() {
        JSONObject response = new JSONObject();
        if (currentUserAchievements != null) {
            for (Achievement achievement : currentUserAchievements) {
                JSONObject item = new JSONObject();
                try {
                    String name = getAchievementName(achievement.getId());
                    int value = (int)achievement.getProgress();
                    item.put("value", value);
                    KGLog.d("Achievement: %s value=%d", name, value);
                    response.put(name, item);
                } catch (JSONException exc) {
                    KGLog.e("Error creating Achievement JSON: %s", exc.toString());
                }
            }
        }
        return response;
    }

    @Override
    public String checkGameCenter() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (currentUserPlayer != null && StringUtils.isNotEmpty(currentUserPlayer.getPlayerId())) {
            sb.append(String.format("\"userId\":\"%s\",\"alias\":\"%s\"", currentUserPlayer.getPlayerId(), currentUserPlayer.getAlias()));
        }
        sb.append("}");
        return sb.toString();
    }

    protected String getLeaderboardId(String leaderboardName) {
        return AmazonWordCluesLeaderboards.valueForName(leaderboardName).getId();
    }

    protected String getLeaderboardName(String leaderboardId) {
        AmazonWordCluesLeaderboards leaderboard = AmazonWordCluesLeaderboards.valueForId(leaderboardId);
        if (leaderboard == null) {
            KGLog.e("Unsupported Leaderboard: %s", leaderboardId);
            return null;
        }
        return leaderboard.getName();
    }

    protected String getAchievementId(String achievementName) {
        return String.format("%s%s", ACHIEVEMENT_ID_BASE, achievementName);
    }

    protected String getAchievementName(String achievementId) {
        return achievementId.replaceAll(ACHIEVEMENT_ID_BASE, "");
    }

}
