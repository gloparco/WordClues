package com.kindredgames.wordclues.amazon;

/**
 * Allows configuring the application to run in "Debug" mode
 *
 */
public class WordCluesAmazonDebugActivity extends WordCluesAmazonActivity {

    //private final String START_PAGE = "file:///android_asset/clues-html-dev/index-dev-amazon.html";
    private final String START_PAGE = "file:///android_asset/clues-html-dev/index-dev-android.html";

//    private final String START_PAGE = "file:///android_asset/test.html";
//    private final String START_PAGE = "https://www.google.com";

    @Override
    protected String getStartPage() {
        return START_PAGE;
    }
}
