package com.kindredgames.wordclues.google;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kindredgames.wordclues.CluesWebViewManager;
import com.kindredgames.wordclues.LoadUrlCallback;
import com.kindredgames.wordclues.util.KGLog;

public class CluesGoogleWebViewClient extends WebViewClient {

    private CluesWebViewManager webViewManager;
    private LoadUrlCallback loadUrlCallback;

    public CluesGoogleWebViewClient(CluesWebViewManager webViewManager, LoadUrlCallback loadUrlCallback) {
        this.webViewManager = webViewManager;
        this.loadUrlCallback = loadUrlCallback;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        KGLog.e("WebView error errorCode=%d, description=%s, url=%s", errorCode, description, failingUrl);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        KGLog.d("onScaleChanged: oldScale=%f, newScale=%f", oldScale, newScale);

        KGLog.d("WebView size: width=%d, height=%d, contentHeight=%d", view.getWidth(), view.getHeight(), view.getContentHeight());
        super.onScaleChanged(view, oldScale, newScale);
    }


    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, java.lang.String url) {
        return webViewManager.shouldOverrideUrlLoading(loadUrlCallback, url);
    }
}
