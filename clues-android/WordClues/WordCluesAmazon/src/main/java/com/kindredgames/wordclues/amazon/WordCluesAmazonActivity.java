package com.kindredgames.wordclues.amazon;

import android.os.Build;
import android.view.View;

import com.amazon.android.webkit.AmazonWebKitFactories;
import com.amazon.android.webkit.AmazonWebKitFactory;
import com.amazon.android.webkit.AmazonWebSettings;
import com.amazon.android.webkit.AmazonWebView;
import com.kindredgames.wordclues.Cashier;
import com.kindredgames.wordclues.GameCenterManager;
import com.kindredgames.wordclues.JavascriptBridge;
import com.kindredgames.wordclues.StoreManager;
import com.kindredgames.wordclues.WordCluesActivity;
import com.kindredgames.wordclues.util.KGLog;

/**
 * Configures the application for Amazon Store
 *
 */
public class WordCluesAmazonActivity extends WordCluesActivity {

    private AmazonStoreManager storeManager;
    private GameCenterManager gameCenterManager;
    private CluesAmazonWebViewClient amazonWebViewClient;
    private AmazonWebKitFactory factory = null;
    private AmazonWebView webView;
    private static boolean sFactoryInit = false;

    @Override
    protected StoreManager getStoreManager(Cashier cashier) {
        if (storeManager == null) {
            storeManager = new AmazonStoreManager(this, cashier);
        }
        return storeManager;
    }

    @Override
    protected GameCenterManager getGameCenterManager() {
        if (gameCenterManager == null) {
            gameCenterManager = new AmazonGameCircleManager(this);
        }
        return gameCenterManager;
    }

    private CluesAmazonWebViewClient getAmazonWebViewClient() {
        if (amazonWebViewClient == null) {
            amazonWebViewClient = new CluesAmazonWebViewClient(controller.getWebViewManager(), this);
        }
        return amazonWebViewClient;
    }
//    public void onMeasure(int width, int height) {
//        // Do nothing as currently CSS seems to size view correctly on Kindle, so we don't need manual adjustment
//    }

    @Override
    public void loadUrl(final String newUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    view.evaluateJavascript(response, resultCallback); // for SDK 19. Not supported by ScalableWebView
//                } else {
//                KGLog.d("loadUrl: %s", newUrl);
                webView.loadUrl(newUrl);
            }
        });
    }

    public void runJavascript(String code) {
        loadUrl("javascript:" + code);
    }

    private void initAmazonWebViewFactory() {
        if (!sFactoryInit) {
            factory = AmazonWebKitFactories.getDefaultFactory();
            if (factory.isRenderProcess(this)) {
                return; // Do nothing if this is on render process
            }
            factory.initialize(this.getApplicationContext());

            // factory configuration is done here, for example:
            factory.getCookieManager().setAcceptCookie(true);

            sFactoryInit = true;
        } else {
            factory = AmazonWebKitFactories.getDefaultFactory();
        }
    }

    @Override
    protected void initWebView() {
        initAmazonWebViewFactory();

        webView = (AmazonWebView) findViewById(R.id.webView);
        factory.initializeWebView(webView, 0xddddff, false, null);

        webView.setWebChromeClient(new CluesAmazonWebChromeClient(this));
        webView.setWebViewClient(getAmazonWebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        //webSettings.setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLayoutAlgorithm(AmazonWebSettings.LayoutAlgorithm.NORMAL);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setAlpha(1.0f);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//            KGLog.d("WebView debugging activated");
//        }
//        webView.requestFocus(View.FOCUS_DOWN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.addJavascriptInterface(new JavascriptBridge(controller.getWebViewManager(), this), "NativeBridge");
        }

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_word_clues;
    }

    @Override
    protected int getMainLayoutId() {
        return R.id.mainLayout;
    }

}
