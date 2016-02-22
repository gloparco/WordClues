package com.kindredgames.wordclues;

public interface CacheController {

    String get(String key, String userId);
    boolean set(String key, String value, String userId);
    boolean append(String key, String value, String userId);

}
