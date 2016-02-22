package com.kindredgames.wordclues;

/**
 * Use to handle completion of a request without any result
 */
public interface SignalCallback {

    void onResult();

    void onFault();
}
