package com.kindredgames.wordclues.google;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult; // If these imports don't resolve, try "Sync Project with Gradle Files"
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.kindredgames.wordclues.GameCenterManager;
import com.kindredgames.wordclues.ResultCallback;
import com.kindredgames.wordclues.SignalCallback;
import com.kindredgames.wordclues.util.KGLog;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GooglePlayGameManager implements GameCenterManager, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String LEADERBOARD_ID_BASE = "com.kindredgames.wordclues.";
    public static final String ACHIEVEMENT_ID_BASE = "";

    protected Activity activity;
    protected Context context;

    private GoogleApiClient googleApiClient;
    private Player currentPlayer;

    protected AchievementBuffer currentUserAchievements;
    protected List<Leaderboard> currentUserLeaderboards;
    protected Map<String, Long> currentUserLeaderboardScores;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean resolvingError = false;

    public GooglePlayGameManager(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

//    protected Player currentUserPlayer() {
//        return googleApiClient != null && googleApiClient.isConnected() ? Games.Players.getCurrentPlayer(googleApiClient) : null;
//    }

    private SignalCallback currentConnectedCallback;
    @Override
    public void connect(final SignalCallback callback) {
        KGLog.d("Google Play connecting.");
        if (googleApiClient == null) {
            currentConnectedCallback = callback;
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect();
    }

    @Override
    public void resume() {
        connect(currentConnectedCallback);
    }

    @Override
    public void pause() {
        disconnect();
    }

    @Override
    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        KGLog.d("Google Play connected.");
        currentPlayer = Games.Players.getCurrentPlayer(googleApiClient);
        if (currentConnectedCallback != null) {
            currentConnectedCallback.onResult();
            currentConnectedCallback = null;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        KGLog.d("Google Play suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        KGLog.d("Google Play connection failed: %s", result.toString());
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                KGLog.d("Google Play connection trying resolution");
                resolvingError = true;
                result.startResolutionForResult(activity, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                KGLog.d("Google Play connection try again");
                googleApiClient.connect();
            }
        } else {
            //showErrorDialog("Connection Failed", result.getErrorCode());
            resolvingError = false;
            KGLog.d("Google Play disconnected. No resolution");
            if (currentConnectedCallback != null) {
                currentConnectedCallback.onFault();
            }
        }
    }

    @Override
    public boolean isAuthenticated() {
        return getPlayerId() != null;
    }

    private Player getCurrentPlayer() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            currentPlayer = Games.Players.getCurrentPlayer(googleApiClient);
        }
        return (currentPlayer != null) ? currentPlayer : null;
    }

    @Override
    public String getPlayerId() {
        Player player = getCurrentPlayer();
        return player != null ? player.getPlayerId() : null;
    }

    @Override
    public String getPlayerAlias() {
        Player player = getCurrentPlayer();
        return player != null ? player.getDisplayName() : null;
    }


    /* Creates a dialog for an error message */
//    private void showErrorDialog(String message, int errorCode) {
//        // Create a fragment for the error dialog
//        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
//        // Pass the error that should be displayed
//        Bundle args = new Bundle();
//        args.putInt(DIALOG_ERROR, errorCode);
//        dialogFragment.setArguments(args);
//        //dialogFragment.show(dialogFragment.getFragmentManager(), "errordialog");
//        KGLog.e("Error: %s code=%d", errorCode);
//    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
//    public void onDialogDismissed() {
//        resolvingError = false;
//    }

    /* A fragment to display an error dialog */
//    public static class ErrorDialogFragment extends DialogFragment {
//        public ErrorDialogFragment() { }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // Get the error code and retrieve the appropriate dialog
//            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
//            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(), REQUEST_RESOLVE_ERROR);
//        }
//
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            //((MainActivity)getActivity()).onDialogDismissed();
//        }
//    }

    @Override
    public boolean checkGameCenterAvailability() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        switch (status) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_DISABLED:
                //GooglePlayServicesUtil.
                return false;
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.DATE_INVALID:
            default:
                KGLog.w("Google Play service isn't available code=%d", status);
                return false;
        }
    }

    @Override
    public void displayGameCenter() {
        activity.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(googleApiClient), 0);
//        GKGameCenterViewController *gameCenterController = [[GKGameCenterViewController alloc] init];
//        if (gameCenterController != null) {
//            gameCenterController.gameCenterDelegate = self;
//            gameCenterController.viewState = GKGameCenterViewControllerStateLeaderboards;
//            [self presentViewController:gameCenterController animated:YES completion:^() {
//                //[webViewManager sendMessage:RESPONSE_GAME_CENTER_CLOSED];
//            }];
//            [self loadLeaderboards];
//        }
    }

    public void updateAchievements(JSONObject values) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Iterator<String> keys = values.keys();
            while (keys.hasNext()) {
                String name = keys.next();
                try {
                    double value = values.getJSONObject(name).getDouble("value");
                    if (value > 0) {
                        Games.Achievements.unlock(googleApiClient, name);
                    }
                } catch (JSONException e) {
                    KGLog.e("Error parsing JSON: %s", e.toString());
                }
            }
        }
    }

    @Override
    public void updateLeaderboards(JSONObject values) {
        try {
            if (currentUserLeaderboards != null) {
                for (Leaderboard leaderboard : currentUserLeaderboards) {
                    final String name = getLeaderboardName(leaderboard.getLeaderboardId());
                    if (name != null) {
                        long value = values.getLong(name);
                        currentUserLeaderboardScores.put(name, Long.valueOf(value));
                        Games.Leaderboards.submitScore(googleApiClient, leaderboard.getLeaderboardId(), value);
                    }
                }
            }
        } catch (JSONException exc) {
            KGLog.w("Error submitting Leaderboard values: %s", exc.toString());
        }
    }

    @Override
    public JSONObject getLeaderboardValues() {
        JSONObject leaderboards = new JSONObject();
        if (currentUserLeaderboards != null) {
            for (Leaderboard leaderboard : currentUserLeaderboards) {
                String name = getLeaderboardName(leaderboard.getLeaderboardId());
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
    public JSONObject getLeaderboardValues(JSONArray message) {
        return getLeaderboardValues();
    }

    @Override
    public int getLeaderboardValue(String tokenName) {
        Long score = currentUserLeaderboardScores.get(tokenName);
        return score != null ? score.intValue() : -1;
    }

    protected String getLeaderboardId(String tokenName) {
        return String.format("%s%s", LEADERBOARD_ID_BASE, tokenName);
    }

    protected String getLeaderboardName(String leaderboardId) {
        return leaderboardId.replaceAll(LEADERBOARD_ID_BASE, "");
    }

    protected String getAchievementId(String tokenName) {
        return String.format("%s%s", ACHIEVEMENT_ID_BASE, tokenName);
    }

    protected String getAchievementName(String achievementId) {
        return achievementId.replaceAll(ACHIEVEMENT_ID_BASE, "");
    }

    @Override
    public String checkGameCenter() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (StringUtils.isNotEmpty(getPlayerId())) {
            sb.append(String.format("\"userId\":\"%s\",\"alias\":\"%s\"", getPlayerId(), getPlayerAlias()));
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void loadLeaderboards(final ResultCallback<JSONObject> messageCallback) {
        if (isAuthenticated()) {
            PendingResult<Leaderboards.LeaderboardMetadataResult> leaderboards = Games.Leaderboards.loadLeaderboardMetadata(googleApiClient, false);
            leaderboards.setResultCallback(new com.google.android.gms.common.api.ResultCallback<Leaderboards.LeaderboardMetadataResult>() {
                @Override
                public void onResult(Leaderboards.LeaderboardMetadataResult leaderboardMetadataResult) {
                    if (leaderboardMetadataResult.getStatus().isSuccess()) {
                        currentUserLeaderboards = new ArrayList<Leaderboard>();
                        Iterator<Leaderboard> iterator = leaderboardMetadataResult.getLeaderboards().iterator();
                        while (iterator.hasNext()) {
                            Leaderboard leaderboard = iterator.next();
                            //leaderboard.getLeaderboardId()
                            currentUserLeaderboards.add(leaderboard);
                        }
//                        //A workaround for if fewer leaderboards are supported than available on GameCircle
//                        for (int i = currentUserLeaderboards.size() - 1; i >= 0; i--) {
//                            Leaderboard leaderboard = currentUserLeaderboards.get(i);
//                            if (getLeaderboardName(leaderboard.getId()) == null) {
//                                KGLog.d("Ignoring unsupported Leaderboard: %s", leaderboard.getId());
//                                currentUserLeaderboards.remove(i);
//                            }
//                        }
                        loadScores(messageCallback);
                    } else {
                        String fault = leaderboardMetadataResult.getStatus().getStatusMessage();
                        KGLog.w("Error getting leaderboards: %s", fault);
                        messageCallback.onFault(fault);
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
        PendingResult<Leaderboards.LoadPlayerScoreResult> result = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(googleApiClient, leaderboard.getLeaderboardId(), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);
        result.setResultCallback(new com.google.android.gms.common.api.ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                if (loadPlayerScoreResult.getStatus().isSuccess()) {
                    try {
                        synchronized (scores) {
                            String name = getLeaderboardName(leaderboard.getLeaderboardId());
                            if (name != null) {
                                long scoreValue = loadPlayerScoreResult.getScore().getRawScore();
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
                } else {
                    // to expect - possibly due to a network failure
                    messageCallback.onFault(String.format("Error loading score for leaderboard %@: %",
                            leaderboard.getDisplayName(), loadPlayerScoreResult.getStatus().getStatusMessage()));
                }
            }
        });
    }

    @Override
    public void loadAchievements(final ResultCallback<JSONObject> messageCallback) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            KGLog.d("WS => AGResponseCallback<GetAchievementsResponse>");
            PendingResult<Achievements.LoadAchievementsResult> result = Games.Achievements.load(googleApiClient, false);
            result.setResultCallback(new com.google.android.gms.common.api.ResultCallback<Achievements.LoadAchievementsResult>() {
                @Override
                public void onResult(Achievements.LoadAchievementsResult loadAchievementsResult) {
                    if (!loadAchievementsResult.getStatus().isSuccess()) {
                        messageCallback.onFault(loadAchievementsResult.getStatus().getStatusMessage());
                    } else {
                        currentUserAchievements = loadAchievementsResult.getAchievements();
                        messageCallback.onResult(getAchievementValues());
                    }
                }
            });
        }
    }

    @Override
    public JSONObject getAchievementValues() {
        JSONObject response = new JSONObject();
        if (currentUserAchievements != null) {
            for (Achievement achievement : currentUserAchievements) {
                JSONObject item = new JSONObject();
                try {
                    String name = getAchievementName(achievement.getAchievementId());
                    int value = achievement.getState() == Achievement.STATE_UNLOCKED ? 100 : 0;
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
}
