package com.kindredgames.wordclues.util;

import android.util.Log;

public final class KGLog {

    public final static String TAG = "==WC==";

    public static final boolean DEBUG = true; // Always set the value to false when doing a release

    private static final boolean SHOW_VERBOSE = DEBUG;
    private static final boolean SHOW_DEBUG = DEBUG;
    private static final boolean SHOW_INFO = DEBUG;
    private static final boolean SHOW_WARN = true;
    private static final boolean SHOW_ERROR = true;

    public static void v(java.lang.String msg) {
        if (SHOW_VERBOSE) {
            Log.w(TAG, msg);
        }
    }

    public static void v(java.lang.String msg, Object... params) {
        if (SHOW_VERBOSE) {
            Log.w(TAG, String.format(msg, params));
        }
    }

    public static void verbose(java.lang.String msg, Object... params) {
        if (SHOW_VERBOSE) {
            Log.w(TAG, String.format(msg, params));
        }
    }

    public static void d(java.lang.String msg) {
        if (SHOW_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void d(java.lang.String msg, Object... params) {
        if (SHOW_DEBUG) {
            Log.d(TAG, String.format(msg, params));
        }
    }

    public static void debug(java.lang.String msg, Object... params) {
        if (SHOW_DEBUG) {
            Log.d(TAG, String.format(msg, params));
        }
    }

    public static void i(java.lang.String msg) {
        if (SHOW_INFO) {
            Log.i(TAG, msg);
        }
    }

    public static void i(java.lang.String msg, Object... params) {
        if (SHOW_INFO) {
            Log.i(TAG, String.format(msg, params));
        }
    }

    public static void info(java.lang.String msg, Object... params) {
        if (SHOW_INFO) {
            Log.i(TAG, String.format(msg, params));
        }
    }

    public static void w(java.lang.String msg) {
        if (SHOW_WARN) {
            Log.w(TAG, msg);
        }
    }

    public static void w(java.lang.String msg, Object... params) {
        if (SHOW_WARN) {
            Log.w(TAG, String.format(msg, params));
        }
    }

    public static void warn(java.lang.String msg, Object... params) {
        if (SHOW_WARN) {
            Log.w(TAG, String.format(msg, params));
        }
    }

    public static void e(java.lang.String msg) {
        if (SHOW_ERROR) {
            Log.e(TAG, msg);
        }
    }

    public static void e(java.lang.String msg, Object... params) {
        if (SHOW_ERROR) {
            Log.e(TAG, String.format(msg, params));
        }
    }

    public static void error(java.lang.String msg, Object... params) {
        if (SHOW_ERROR) {
            Log.e(TAG, String.format(msg, params));
        }
    }

}
