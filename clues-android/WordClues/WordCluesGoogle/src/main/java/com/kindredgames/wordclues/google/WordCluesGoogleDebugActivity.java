package com.kindredgames.wordclues.google;

public class WordCluesGoogleDebugActivity extends WordCluesGoogleActivity {
//    private final String START_PAGE = "file:///android_asset/clues-html-dev/index-dev-google.html";
    private final String START_PAGE = "file:///android_asset/clues-html-dev/index-dev-android.html";

//    private final String START_PAGE = "file:///android_asset/test.html";
//    private final String START_PAGE = "https://www.google.com";

    @Override
    protected String getStartPage() {
        return START_PAGE;
    }
}
