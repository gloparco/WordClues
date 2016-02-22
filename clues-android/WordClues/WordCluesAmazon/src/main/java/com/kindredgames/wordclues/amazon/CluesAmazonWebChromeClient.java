package com.kindredgames.wordclues.amazon;

import android.app.Activity;
import android.webkit.WebView;

import com.amazon.android.webkit.AmazonWebChromeClient;
import com.kindredgames.wordclues.util.KGLog;

public class CluesAmazonWebChromeClient extends AmazonWebChromeClient {

    private Activity activity;

    public CluesAmazonWebChromeClient(Activity activity) {
        this.activity = activity;
    }

    public void onProgressChanged(WebView view, int progress) {
        // Activities and WebViews measure progress with different scales.
        // The progress meter will automatically disappear when we reach 100%
        KGLog.d("Progress change: %d", progress);
        activity.setProgress(progress * 1000);
    }

    @Override
    public boolean onConsoleMessage(com.amazon.android.webkit.AmazonConsoleMessage consoleMessage) {
        onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId());
        return true;
    }

    @Override
    public void onConsoleMessage(java.lang.String message, int lineNumber, java.lang.String sourceID) {
        if (message != null && message.toLowerCase().indexOf("error") >= 0) {
            KGLog.e("JS-> %s in %s:%d", message, sourceID, lineNumber);
        } else {
            KGLog.d("JS-> %s in %s:%d", message, sourceID, lineNumber);
        }
    }
}
