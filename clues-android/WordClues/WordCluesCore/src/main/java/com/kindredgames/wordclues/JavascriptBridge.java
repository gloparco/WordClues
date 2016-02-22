package com.kindredgames.wordclues;

import android.webkit.JavascriptInterface;

import com.kindredgames.wordclues.util.KGLog;

public class JavascriptBridge {

    private CluesWebViewManager cluesWebViewManager;
    private LoadUrlCallback loadUrlCallback;

    public JavascriptBridge(CluesWebViewManager cluesWebViewManager, LoadUrlCallback loadUrlCallback) {
        this.cluesWebViewManager = cluesWebViewManager;
        this.loadUrlCallback = loadUrlCallback;
    }

    @JavascriptInterface
    public void handleJavascript(String topic, int callbackId, String args) {
        KGLog.d("@JSI Received topic=[%s] message[%d]: %s", topic, callbackId, args);
        String[] topicParams = topic != null ? topic.split("\\.") : null;
        cluesWebViewManager.handleCall(loadUrlCallback, topic, topicParams, callbackId, args);
    }

}
