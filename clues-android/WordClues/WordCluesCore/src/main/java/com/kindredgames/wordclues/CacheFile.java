package com.kindredgames.wordclues;

import android.content.Context;

import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CacheFile implements CacheController {

    private static final String CACHE_PREFIX = "cache";

//    static final String FILE_ENCODING = Utils.ENCODING_UNICODE;
    static final String FILE_ENCODING = Utils.ENCODING_UTF8;

    private Context context;

    public CacheFile(Context context) {
        this.context = context;
    }

    protected String getKeyFileName(String key, String userId) {
        String fileName =  String.format("%s_%s_%s", userId, CACHE_PREFIX, key);
        KGLog.d("Access cache file %s", fileName);
        return fileName;
    }

    protected String encode(String value, String userId) {
        return value;
    }

    protected String decode(String value, String userId) {
        return value;
    }

    @Override
    public boolean set(String key, String value, String userId) {
        String fileName = getKeyFileName(key, userId);
        if (value == null) {
            File file = context.getFileStreamPath(fileName);
            if (file.exists()) {
                if (!file.delete()) {
                    KGLog.w("Didn't delete file %s?", fileName);
                } else {
                    KGLog.d("Deleted file %s", fileName);
                }
            } else {
                KGLog.d("File %s didn't exist", fileName);
            }
        } else {
            FileOutputStream fos = null;
            try {
                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fos, FILE_ENCODING);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(encode(value, userId));
                bw.flush();
            } catch (IOException exc) {
                KGLog.e("Error setting cache file %s: %s", fileName, exc.toString());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException exc2) {
                        KGLog.e("Error closing cache file %s: %s", fileName, exc2.toString());
                    }
                }
            }
        }

        return true;
    }

    public boolean append(String key, String value, String userId) {
        String fileName = getKeyFileName(key, userId);
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_APPEND | Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos, FILE_ENCODING);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(encode(value, userId));
            bw.flush();
        } catch (IOException exc) {
            KGLog.e("Error appending file: %s", exc.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException exc2) {
                    KGLog.e("Error closing cache file %s: %s", fileName, exc2.toString());
                }
            }
        }
        return true;
    }

    public String get(String key, String userId) {
        String fileName = getKeyFileName(key, userId);
        File file = context.getFileStreamPath(fileName);
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = context.openFileInput(fileName);
                InputStreamReader isr = new InputStreamReader(fis, FILE_ENCODING);
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(decode(line, userId));
                }
                return sb.toString();
            } catch (FileNotFoundException fnf) {
                // Do nothing, return default
            } catch (IOException exc) {
                KGLog.e("Error reading %s: %s", fileName, exc.toString());
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException exc2) {
                        KGLog.e("Error closing cache file %s: %s", fileName, exc2.toString());
                    }
                }
            }
        }
        return null;
    }

}
