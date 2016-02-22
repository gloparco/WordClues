package com.kindredgames.wordclues.components;

//import android.webkit.WebView;

import android.webkit.WebView;

public class ScalableWebView extends WebView {
//public class ScalableWebView extends AmazonWebView {

    public ScalableWebView(android.content.Context context) {
        super(context);
    }

    public ScalableWebView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableWebView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    public ScalableWebView(android.content.Context context, android.util.AttributeSet attrs, int defStyle, boolean privateBrowsing) {
//        super(context, attrs, defStyle, privateBrowsing);
//    }

    public int getScrollWidth() {
        return computeHorizontalScrollRange();
    }

    public int getScrollHeight() {
        return computeVerticalScrollRange();
    }

}
