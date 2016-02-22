package com.kindredgames.wordclues.google;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.kindredgames.wordclues.Cashier;
import com.kindredgames.wordclues.GameCenterManager;
import com.kindredgames.wordclues.JavascriptBridge;
import com.kindredgames.wordclues.StoreManager;
import com.kindredgames.wordclues.WordCluesActivity;
import com.kindredgames.wordclues.util.KGLog;

/**
 * Allows configuring the application for Google Play
 *
 */
public class WordCluesGoogleActivity extends WordCluesActivity {

    private GoogleStoreManager storeManager;
    private GameCenterManager gameCenterManager;
    private CluesGoogleWebViewClient webViewClient;
    private WebView webView;

    @Override
    protected StoreManager getStoreManager(Cashier cashier) {
        if (storeManager == null) {
            storeManager = new GoogleStoreManager(this, cashier);
        }
        return storeManager;
    }

    @Override
    protected GameCenterManager getGameCenterManager() {
        if (gameCenterManager == null) {
            gameCenterManager = new GooglePlayGameManager(this);
        }
        return gameCenterManager;
    }

    @Override
    protected void initWebView() {
        webView = (WebView) findViewById(R.id.webView);

        webView.setWebChromeClient(new CluesGoogleWebChromeClient(this));
        webView.setWebViewClient(getWebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);

        // Disable zooming
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        // Enable zooming
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setSupportZoom(true);

        webView.getSettings().setUseWideViewPort(true);
        //webView.setInitialScale(1); // just 1% at the beginning?
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setAlpha(1.0f);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        webView.getSettings().setLoadWithOverviewMode(true); // Any difference for zoom?

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//            KGLog.d("WebView debugging activated");
//        }
//        webView.requestFocus(View.FOCUS_DOWN);

        if (KGLog.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //if ((getApplcationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                WebView.setWebContentsDebuggingEnabled(true);
//            KGLog.d("WebView debugging activated");
//            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.addJavascriptInterface(new JavascriptBridge(controller.getWebViewManager(), this), "NativeBridge");
        }
    }

    @Override
    public void runJavascript(String code) {
        loadUrl("javascript:" + code);
    }

    @Override
    public void loadUrl(final String newUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                KGLog.d("loadUrl: %s", newUrl);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    webView.evaluateJavascript(newUrl, new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String value) {
//                            KGLog.d("loadUrl response:", value);
//                        }
//                    });
//                } else {
                KGLog.d("WebView size: width=%d, height=%d", webView.getWidth(), webView.getHeight());
                KGLog.d("WebView size: before contentHeight=%d", webView.getContentHeight()); // may be not precise at the moment
                    webView.loadUrl(newUrl);
                KGLog.d("WebView size: after contentHeight=%d", webView.getContentHeight()); // may be not precise at the moment
//                }
            }
        });
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_word_clues_google;
    }

    @Override
    protected int getMainLayoutId() {
        return R.id.mainLayout;
    }

    private CluesGoogleWebViewClient getWebViewClient() {
        if (webViewClient == null) {
            webViewClient = new CluesGoogleWebViewClient(controller.getWebViewManager(), this);
        }
        return webViewClient;
    }

}
