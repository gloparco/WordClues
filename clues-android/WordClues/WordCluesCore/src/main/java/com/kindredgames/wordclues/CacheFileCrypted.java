package com.kindredgames.wordclues;

import android.content.Context;

import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.SecurityUtils;
import com.kindredgames.wordclues.util.Utils;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CacheFileCrypted extends CacheFile {

    private static final String SALT = "27hGfoCmwWK5nvywsYQ/DEytKAC9SaXozSazzT9";

    public CacheFileCrypted(Context context) {
        super(context);
    }

    @Override
    protected String encode(String value, String userId) {
        try {
            return SecurityUtils.symmetricEncrypt(value, userId + SALT);
        } catch (Exception exc) {
            KGLog.e("Can't encode cache: %s", exc);
        }
        return null;
    }

    @Override
    public String decode(String value, String userId) {
        try {
            return StringUtils.trim(SecurityUtils.symmetricDecrypt(value, userId + SALT));
        } catch (Exception exc) {
            KGLog.e("Can't decode cache: %s", exc);
        }
        return null;
    }

}
