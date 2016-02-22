package com.kindredgames.wordclues.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Utils {

    public static final String ENCODING_ASCII = "US-ASCII";
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String ENCODING_UNICODE = "unicode";

    public static void logError(Exception error, String action) {
        if (error != null) {
            KGLog.e("Error %s: %s", action, error.toString());
        }
    }

    public static long timestamp() {
        return new Date().getTime();
    }

//    public static String jsonWithDictionary(NSDictionary dictionary {
//        NSError *error;
//        @try {
//            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary
//            options:0 // NSJSONWritingPrettyPrinted
//            error:&error];
//            if (jsonData) {
//                return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
//            } else {
//                LogError(@"Nil result serializing JSON: %@", error);
//            }
//        } @catch (NSException *exc) {
//            LogError(@"Error serializing JSON: %@", exc);
//        }
//        return null;
//    }

    public static JSONObject jsonData(String jsonMessage) {
        JSONObject messageData = null;
        if (jsonMessage != null) {
            try {
                messageData = new JSONObject(jsonMessage);
            } catch (JSONException exc) {
                KGLog.e("Error parsing json [%s]: %s", jsonMessage, exc.toString());
            }
        }
        return messageData;
    }

    public static String dataToJson(JSONObject jsonData) {
        if (jsonData != null) {
            try {
                return jsonData.toString(0);
            } catch (JSONException exc) {
                KGLog.e("Error stringifying json: %s", exc.toString());
            }
        }
        return null;
    }

    public static String jsonString(String jsonString) {
        return jsonString.replaceAll("'", "\"");
    }

    public static String getStringHash(String source) {
        return SecurityUtils.getStringMd5(source);
    }

/**
 * Calculate the SHA-256 hash using Common Crypto
 */
//    + (NSString *)getStringSHA256:(NSString *)source withSize:(const int)size {
//        unsigned char hashedChars[size];
//        const char *sourceString = [source UTF8String];
//        size_t sourceStringLength = strlen(sourceString);
//
//        // Confirm that the length of the user name is small enough
//        // to be recast when calling the hash function.
//        if (sourceStringLength > UINT32_MAX) {
//            KGLog(@"Account name too long to hash: %@", source);
//            return null;
//        }
//        CC_SHA256(sourceString, (CC_LONG) sourceStringLength, hashedChars);
//
//        // Convert the array of bytes into a string showing its hex representation.
//        NSMutableString *hash = [[NSMutableString alloc] init];
//        for (int i = 0; i < size; i++) {
//            // Add a dash every four bytes, for readability.
////        if (i != 0 && i % 4 == 0) {
////            [hash appendString:@"-"];
////        }
//            [hash appendFormat:@"%02x", hashedChars[i]];
//        }
//        return [NSString stringWithString:hash];
//    }

    public static boolean isEmptyString(String string) {
        return string == null || string.length() == 0;
    }

    public static String join(List<String> list) {
        StringBuilder out = new StringBuilder();
        for (String s : list) {
            out.append(s);
        }
        return out.toString();
    }

    public static String join(List<String> list, String delimiter) {
        StringBuilder out = new StringBuilder();
        for (String s : list) {
            out.append(s);
            out.append(delimiter);
        }
        if (out.length() > 0) {
            out.delete(out.length() - 1, out.length() - 1);
        }
        return out.toString();
    }

    public static String getDate(String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat(dateFormat, Locale.getDefault())
                .format(calendar.getTime());
    }

    public static String getDate(String dateFormat, long currenttimemillis) {
        return new SimpleDateFormat(dateFormat, Locale.getDefault())
                .format(currenttimemillis);
    }

    public static long getBuildDate(Context context) {

        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            return time;

        } catch (Exception e) {
        }

        return 0l;
    }


}
