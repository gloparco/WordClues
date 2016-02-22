package com.kindredgames.wordclues;

import org.json.JSONObject;

public interface GameController {

    void webViewDidLoad();

    String generateGames(int gamesCount);
    String generateGame();
    String getUserCache(String cacheName);
    String setUserCache(String cacheName, String data);

    boolean canOpenFacebookApp();
    boolean canOpenTwitterApp();

    String postFacebook(JSONObject message);
    String postTwitter(JSONObject message);
    void postEmail(JSONObject message);

    void linkStore();
    void linkTwitter();
    void linkFacebook();
    void linkCompany();

    GameCenterManager getGameCenter();
//    String checkGameCenter();
    String canMakePayments();
    String getAchievementValues();
    void updateAchievements(JSONObject message);

    void price(JSONObject invoice);
    void buy(JSONObject invoice);
    String owns(JSONObject productIdMessage);
    void restorePurchases();

    void playSound(String name);
}
