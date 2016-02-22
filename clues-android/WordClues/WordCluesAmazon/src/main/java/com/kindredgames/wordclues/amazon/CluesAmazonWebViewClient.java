package com.kindredgames.wordclues.amazon;

import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonWebViewClient;
import com.kindredgames.wordclues.CluesWebViewManager;
import com.kindredgames.wordclues.LoadUrlCallback;
import com.kindredgames.wordclues.util.KGLog;

public class CluesAmazonWebViewClient extends AmazonWebViewClient {

    private CluesWebViewManager webViewManager;
    private LoadUrlCallback loadUrlCallback;

    public CluesAmazonWebViewClient(CluesWebViewManager webViewManager, LoadUrlCallback loadUrlCallback) {
        this.webViewManager = webViewManager;
        this.loadUrlCallback = loadUrlCallback;
    }

    @Override
    public void onReceivedError(AmazonWebView view, int errorCode, String description, String failingUrl) {
        KGLog.e("WebView error errorCode=%d, description=%s, url=%s", errorCode, description, failingUrl);
    }

    @Override
    public void onScaleChanged(AmazonWebView view, float oldScale, float newScale) {
        KGLog.d("onScaleChanged: oldScale=%f, newScale=%f", oldScale, newScale);
    }

    @Override
    public boolean shouldOverrideUrlLoading(final AmazonWebView view, java.lang.String url) {
        return webViewManager.shouldOverrideUrlLoading(loadUrlCallback, url);
    }

}
