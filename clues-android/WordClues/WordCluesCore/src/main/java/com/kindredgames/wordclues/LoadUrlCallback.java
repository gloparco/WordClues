package com.kindredgames.wordclues;

import android.content.Intent;

public interface LoadUrlCallback {
    void loadUrl(String newUrl);
    void runJavascript(String code);
    void startActivity(Intent intent);
}
