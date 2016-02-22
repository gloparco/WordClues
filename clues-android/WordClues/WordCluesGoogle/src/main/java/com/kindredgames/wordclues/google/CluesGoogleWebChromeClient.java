package com.kindredgames.wordclues.google;

import android.app.Activity;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.kindredgames.wordclues.util.KGLog;

public class CluesGoogleWebChromeClient extends WebChromeClient {

    private Activity activity;

    public CluesGoogleWebChromeClient(Activity activity) {
        this.activity = activity;
    }

    public void onProgressChanged(WebView view, int progress) {
        // Activities and WebViews measure progress with different scales.
        // The progress meter will automatically disappear when we reach 100%
        KGLog.d("Progress change: %d", progress);
        activity.setProgress(progress * 1000);
    }

    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        // Call the old version of this function for backwards compatibility.
        onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId());
        return false;
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
