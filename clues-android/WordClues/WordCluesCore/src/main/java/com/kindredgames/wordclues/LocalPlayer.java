package com.kindredgames.wordclues;

public class LocalPlayer {

    private boolean authenticated;
    private String playerId;
    private String alias;
    private String avatartUrl;


    public LocalPlayer(Boolean authenticated, String playerId, String alias, String avatartUrl) {
        super();
        this.authenticated = authenticated;
        this.playerId = playerId;
        this.alias = alias;
        this.avatartUrl = avatartUrl;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getAlias() {
        return alias;
    }

    public String getAvatartUrl() {
        return avatartUrl;
    }
}
