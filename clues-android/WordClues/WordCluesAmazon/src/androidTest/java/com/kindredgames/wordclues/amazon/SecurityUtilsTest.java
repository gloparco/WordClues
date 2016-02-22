package com.kindredgames.wordclues.amazon;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.SecurityUtils;
import com.kindredgames.wordclues.util.Utils;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SecurityUtilsTest extends ActivityInstrumentationTestCase2<WordCluesAmazonDebugActivity> {

    public SecurityUtilsTest() {
        super(WordCluesAmazonDebugActivity.class);
    }

    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }

    public void testSecurityTest() throws Exception {
        String nonPaddedInput = StringUtils.trim("Kindredgames");
        assertEquals("Decrypted string must be equal to the source", nonPaddedInput, StringUtils.trim(SecurityUtils.testSymmetricEncryption(nonPaddedInput)));
    }

    public void testSymmetricExcyptionDecryption() throws Exception {
        String nonPaddedInput = StringUtils.trim("Kindredgames");
        String password = "some Super duper password";
        assertEquals("Decrypted string must be equal to the source", nonPaddedInput, StringUtils.trim(SecurityUtils.symmetricDecrypt(SecurityUtils.symmetricEncrypt(nonPaddedInput, password), password)));
    }

//    public void testGenerateKeyPair() {
//        String[] keys = SecurityUtils.generateAsymmetricKeys();
//        KGLog.d("Public key: %s", keys[0]);
//        KGLog.d("Private key: %s", keys[1]);
//    }

//    public void testAsymmetricEncryptionDecryption() {
//        String nonPaddedInput = StringUtils.trim("Kindredgames");
//        String encrypted = SecurityUtils.asymmetricEncypt(nonPaddedInput, privateKey);
//        String decrypted = SecurityUtils.asymmetricDecrypt(encrypted, publicKey);
//        assertEquals("Decrypted string must be equal to the source", nonPaddedInput, decrypted);
//    }

    private static final String publicKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBDg3nTQV+AoPS+RSK6td9/Vq47Jlm38SejLD1\n" +
            "2N6tIJqz3+CxoXlqRHZ5YMhlkvYvhTNyjSArQGmBuFPEFintkvInD5bPwzZMVG+nAkafhCBUfHgm\n" +
            "Qv7wQGGaRDiJgH3Q5TgyzQE5oGcN1C3zuYcl4WhwfJJUShIg33PmygLfWQIDAQA";

//    private static final String privateKey =
//            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMEODedNBX4Cg9L5FIrq1339Wrjs\n" +
//            "mWbfxJ6MsPXY3q0gmrPf4LGheWpEdnlgyGWS9i+FM3KNICtAaYG4U8QWKe2S8icPls/DNkxUb6cC\n" +
//            "Rp+EIFR8eCZC/vBAYZpEOImAfdDlODLNATmgZw3ULfO5hyXhaHB8klRKEiDfc+bKAt9ZAgMBAAEC\n" +
//            "gYA2Y99G60xA1DSPm91NxhwwQtDsiLwvX5vlj33Lxe32mNj9FHYZlYS/i8pVKA9NjroNaVm1TE1j\n" +
//            "4iHXijcPZ68n4pmSCJveO0rXUw2XjMqNBAAZkQR9U0BxsrkLJNbWl81uffZGMe8mO+QjZIpSTjtq\n" +
//            "9VVwQEoRs/TDwvr1NYGAAQJBAN/IGA3KFpVQwub2cnCGAZ4mAAU20KKIi31PFSrfw7adLJeTmA8Y\n" +
//            "QMa/TuWwpH1RfFAJ3Hd0Lg7b2E6br/rR69kCQQDc2XevFWQdzG7WHMYsHzSLuusT5i6yrx2CJaCi\n" +
//            "27hGfoCmwWK5nvywsYQ/DEytKAC9SaXozSazzT9c89SHcd+BAkEAqXWAoZCTIWcS8ItXhdY0i2oD\n" +
//            "Qe+MdkgoOTAVu23Paw4CopXl5ChdhSOBv3XQWgXGuWV+Sgb0idP5LS3ASZOFoQJACdwzub9svt/X\n" +
//            "uvuB5YpnIC3yPOsz/opO+PaRG3RP3+XZ/Jf5vmvYwFLMorv72GdG0SyumZw/NBvfzlicjoeJAQJA\n" +
//            "L5wjO1Jz7brdB3jm2UsE/h5yDbBK16QOvVT442rfwstVwxVFcd1v+npXQUY4pOFJf7bG/afxATF7\n" +
//            "OgtW93wEaQ==";

    public void testEncryptDictinaryFile() {
        BufferedReader reader = null;
        FileOutputStream fos = null;
        String fileName = "english.txt";
        String encryptedFileName = "english.dic";
        try {
            reader = new BufferedReader(new InputStreamReader(getActivity().getApplicationContext().getAssets().open("english.txt"), Utils.ENCODING_UTF8));

            fos = getActivity().getApplicationContext().openFileOutput(encryptedFileName, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos, Utils.ENCODING_UTF8);
            BufferedWriter bw = new BufferedWriter(osw);

            String password = publicKey; // initial password is something hardcoded
            // do reading, usually loop until end of file reading
            String lineText = reader.readLine();
            while (lineText != null) {
                String encryptedLine = SecurityUtils.symmetricEncrypt(lineText, password);
                bw.write(encryptedLine);
                bw.write("\n");
                password = lineText; // the original text is the password for the next line
                lineText = reader.readLine();
            }
            bw.flush();
        } catch (Exception e) {
            KGLog.e("Error reading file %s: %s", fileName, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e2) {
                KGLog.e("Error closing file: %s", e2.getMessage());
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException exc2) {
                    KGLog.e("Error closing cache file %s: %s", fileName, exc2.toString());
                }
            }
        }
    }
}
