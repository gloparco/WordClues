package com.kindredgames.wordclues;

import org.json.JSONArray;
import org.json.JSONObject;

public interface GameCenterManager {

    void connect(SignalCallback callback);
    void resume();
    void pause();
    void disconnect();
    boolean checkGameCenterAvailability();
    void displayGameCenter();
    boolean isAuthenticated();
    String getPlayerId();
    String getPlayerAlias();
    void loadLeaderboards(ResultCallback<JSONObject> messageCallback);
    void loadAchievements(ResultCallback<JSONObject> messageCallback);
    void updateAchievements(JSONObject values);

    void updateLeaderboards(JSONObject values);

//        if (currentUserLeaderboards != null) {
//            NSMutableArray *scores = [[NSMutableArray alloc] init];
//            for (GKLeaderboard *leaderboard in currentUserLeaderboards) {
//                String shortId = [leaderboard.identifier stringByReplacingOccurrencesOfString:(String )LEADERBOARD_ID_BASE withString:"" options:NSCaseInsensitiveSearch range:NSMakeRange(0, LEADERBOARD_ID_BASE.length)];
//                String value = [values objectForKey:shortId];
//                if (value != null) {
//                    GKScore *score = [[GKScore alloc] initWithLeaderboardIdentifier:leaderboard.identifier forPlayer:currentUserId];
//                    score.context = 0;
//                    score.value = [value intValue];
//                    [scores addObject:score];
//                }
//            }
//            if (scores.count > 0) {
//                [GKScore reportScores:scores withCompletionHandler:^(NSError *error) {
//                    if (error != null) {
//                        KGLog("Error updating leaderboard: %", error);
//                    }
//                }];
//            }
//        }
//    }

    JSONObject getLeaderboardValues();

//        JSONObject leaderboards = new JSONObject();
//        if (currentUserLeaderboards != null) {
//            for (int i = (int)currentUserLeaderboards.count - 1; i >= 0; i--) {
//                GKLeaderboard *leaderboard = [currentUserLeaderboards objectAtIndex:i];
//                GKScore *score = leaderboard.localPlayerScore;
//                [leaderboards setObject:[NSNumber numberWithLongLong:score.value] forKey:[self getLeaderboardName:leaderboard.identifier]];
//            }
//        }
//        return leaderboards;
//    }

//    public int getLeaderboardValue(String tokenName) {
//        String leaderboardId = getLeaderboardId(tokenName);
//        if (currentUserLeaderboards == null) {
//            [self loadLeaderboardInfo];
//        }
//        if (currentUserLeaderboards != null) {
//            for (int i = (int)currentUserLeaderboards.count - 1; i >= 0; i--) {
//                GKLeaderboard *leaderboard = [currentUserLeaderboards objectAtIndex:i];
//                if ([leaderboard.identifier isEqualToString:leaderboardId]) {
//                    GKScore *score = leaderboard.localPlayerScore;
//                    return (int)score.value;
//                }
//            }
//        }
//        return 0;
//    }

    JSONObject getLeaderboardValues(JSONArray message);

//        NSArray *names;
//        if ([message isKindOfClass:[NSArray class]]) {
//            names = message;
//        } else {
//            names = [(NSDictionary *) message allKeys];
//        }

//        JSONObject leaderboards = new JSONObject();
//        if (currentUserLeaderboards != null) {
//            for (GKLeaderboard *leaderboard in currentUserLeaderboards) {
//                String lbName = [self getLeaderboardName:leaderboard.identifier];
//                for (String name in names) {
//                    if ([name isEqualToString:lbName]) {
//                        GKScore *score = leaderboard.localPlayerScore;
//                        [leaderboards setObject:[NSNumber numberWithLongLong:score.value] forKey:name];
//                        break;
//                    }
//                }
//            }
//        }
//        return leaderboards;
//    }

    int getLeaderboardValue(String tokenName);

//    private void gameCenterViewControllerDidFinish:(GKGameCenterViewController *)gameCenterViewController {
//        [gameCenterViewController dismissViewControllerAnimated:YES completion:^(void){}];
//        [webViewManager sendMessage:(String )RESPONSE_GAME_CENTER_CLOSED];
//    }

    String checkGameCenter();

    JSONObject getAchievementValues();

}
