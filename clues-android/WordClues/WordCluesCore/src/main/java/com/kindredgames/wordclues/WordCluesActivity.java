package com.kindredgames.wordclues;

import com.kindredgames.wordclues.logic.MainViewController;
import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.Utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.util.Date;

public abstract class WordCluesActivity extends Activity implements LoadUrlCallback, MessageDispatcher {

    protected MainViewController controller;

    abstract protected StoreManager getStoreManager(Cashier cashier);

    abstract protected GameCenterManager getGameCenterManager();

    private static int createCount = 0;

    protected abstract int getContentViewId();
    protected abstract int getMainLayoutId();

    private final String START_PAGE = "file:///android_asset/clues-html/index.html";

    protected String getStartPage() {
        return START_PAGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printBuildTime();
        KGLog.i("onCreate() %d", ++createCount);
        initWindow();

//        final View controlsView = findViewById(R.id.fullscreen_content_controls);
//        final View contentView = findViewById(R.id.fullscreen_content);

        controller = new MainViewController(
                this,
                getApplicationContext(),
                getStartPage(),
                getGameCenterManager()
        );
        controller.setStoreManager(getStoreManager(controller));

        initWebView();
    }

    protected abstract void initWebView();

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(getContentViewId());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //loadPage();
    }

    protected void loadPage() {
        loadUrl(getStartPage());
    }

    private static int startCount = 0;

    @Override
    protected void onStart() {
        KGLog.d("onStart %d", ++startCount);
        super.onStart();
        controller.onStart();
    }

    private static int resumeCount = 0;

    @Override
    protected void onResume() {
        KGLog.d("onResume %d", ++resumeCount);
        super.onResume();
//        if (resumeCount == 1) {
        controller.onResume();
//        } else {
//            KGLog.d("Ignoring onResume %d", resumeCount);
//        }
    }

    private static int pauseCount = 0;
    @Override
    protected void onPause() {
        KGLog.d("onPause %d", ++pauseCount);
        super.onPause();
        controller.onPause();
    }

    private static int stopCount = 0;
    @Override
    protected void onStop() {
        KGLog.d("onStop %d", ++stopCount);
        controller.onStop();
        super.onStop();
    }

    private void printBuildTime() {
        try {

            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            // install datetime
//            String appInstallDate = DateUtils.getDate("yyyy/MM/dd hh:mm:ss.SSS", packageInfo.lastUpdateTime);
            String appInstallDate = new Date(packageInfo.lastUpdateTime).toString();

            // build datetime
//            String appBuildDate = DateUtils.getDate("yyyy/MM/dd hh:mm:ss.SSS", Utils.getBuildDate(this));
            String appBuildDate = new Date(Utils.getBuildDate(this)).toString();

            KGLog.i("===========================================");
            KGLog.i("appBuildDate = " + appBuildDate);
            KGLog.i("appInstallDate = " + appInstallDate);

        } catch (Exception e) {
        }

    }

    void zoomFitContent() {
        FrameLayout f = (FrameLayout) findViewById(getMainLayoutId());
//        Rect bounds = f.getClipBounds();
//        int height = f.getMeasuredHeight();
//        int width = f.getMeasuredWidth();
//        int bottom = f.getBottom();
//        KGLog.i("h=%d, w=%d, b=%d", height, width, bottom);
        // try to fit web content
//        float zoomX = webView.getWidth() / bounds.width();
//        float zoomY = webView.getHeight() / bounds.height();
//        float zoom = Math.min(zoomX, zoomY);
//        webView.setScaleX(zoom);
//        webView.setScaleY(zoom);
    }

    public void onMeasure(int width, int height) {
//        int viewWidth = getDefaultSize(getWidth(), widthMeasureSpec);
//        int viewHeight = getDefaultSize(getHeight(), heightMeasureSpec);
//        zoomFitContent();

//        if (width > 0 && height > 0) {
//            KGLog.i("w=%d, h=%d", width, height);
//            KGLog.i("scaleX=%f, scaleY=%f", webView.getScaleX(), webView.getScaleY());
//            float ratio = getResources().getDisplayMetrics().scaledDensity;
//            float rWidth = width / ratio;
//            float rHeight = height / ratio;
//            KGLog.i("vW=%f, vH=%f", rWidth, rHeight);
//            KGLog.i("Ratio=%f", ratio);
//
//            float zoomX = rWidth / getApplicationContext().getResources().getInteger(R.integer.htmlWidthPx);
//            float zoomY = rHeight / getApplicationContext().getResources().getInteger(R.integer.htmlHeightPx);
//            if (zoomX > 0.0 && zoomY > 0.0) {
//                webView.setScaleX(zoomX);
//                webView.setScaleY(zoomY);
//                float transX = width * (zoomX - 1) / 2;
//                float transY = height * (zoomY - 1) / 2;
//                webView.setTranslationX(transX);
//                webView.setTranslationY(transY);
//                KGLog.i("zoomX=%f, zoomY=%f", zoomX, zoomY);
//                KGLog.i("transX=%f, transY=%f", transX, transY);
//            }
//        }
    }

    @Override
    public void dispatchMessage(JSONObject message) {
        controller.dispatchMessage(message);
    }
}
