package com.kindredgames.wordclues.util;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

    private static final int BASE64_FLAGS = Base64.NO_WRAP; 
    
    public static String getStringMd5(String string) {
        return bytesToHexString(getStringMd5Bytes(string));
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            sb.append(Integer.toHexString((bytes[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static byte[] getStringMd5Bytes(String string) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            return md.digest(string.getBytes());
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String getStringSha256(String string) {
        return bytesToHexString(getStringSha256Bytes(string));
    }

    public static byte[] getStringSha256Bytes(String string) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return md.digest(string.getBytes());

        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String testSymmetricEncryption(String text) throws Exception {
        //TODO: get some embedded encryption for more serious data
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] input = text.getBytes();
        byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04,
                0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c,
                0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14,
                0x15, 0x16, 0x17 };

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        //Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

        // encryption pass
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);

        // decryption pass
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
        int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return new String(plainText);
    }

    public static String symmetricEncrypt(String text, String password) throws Exception {
        //TODO: get some embedded encryption for more serious data
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] input = text.getBytes(Utils.ENCODING_UTF8);
        byte[] keyBytes = getStringSha256Bytes(password);

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        return Base64.encodeToString(cipherText, BASE64_FLAGS);
    }

    public static String symmetricDecrypt(String base64EncryptedText, String password) throws Exception {
        //TODO: get some embedded encryption for more serious data
        //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] input = Base64.decode(base64EncryptedText, BASE64_FLAGS);
        byte[] keyBytes = getStringSha256Bytes(password);

        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // encryption pass
        cipher.init(Cipher.DECRYPT_MODE, key);
        int ctLength = input.length;

        byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
        int ptLength = cipher.update(input, 0, ctLength, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return new String(plainText);
    }

    public static void listSupportedAlgorithms() {
        String result = "";

        // get all the providers
        Provider[] providers = Security.getProviders();

        for (int p = 0; p < providers.length; p++) {
            // get all service types for a specific provider
            Set<Object> ks = providers[p].keySet();
            Set<String> servicetypes = new TreeSet<String>();
            for (Iterator<Object> it = ks.iterator(); it.hasNext(); ) {
                String k = it.next().toString();
                k = k.split(" ")[0];
                if (k.startsWith("Alg.Alias."))
                    k = k.substring(10);

                servicetypes.add(k.substring(0, k.indexOf('.')));
            }

            // get all algorithms for a specific service type
            int s = 1;
            for (Iterator<String> its = servicetypes.iterator(); its.hasNext(); ) {
                String stype = its.next();
                Set<String> algorithms = new TreeSet<String>();
                for (Iterator<Object> it = ks.iterator(); it.hasNext(); ) {
                    String k = it.next().toString();
                    k = k.split(" ")[0];
                    if (k.startsWith(stype + "."))
                        algorithms.add(k.substring(stype.length() + 1));
                    else if (k.startsWith("Alg.Alias." + stype + "."))
                        algorithms.add(k.substring(stype.length() + 11));
                }

                int a = 1;
                for (Iterator<String> ita = algorithms.iterator(); ita.hasNext(); ) {
                    result += ("[P#" + (p + 1) + ":" + providers[p].getName() + "]" +
                            "[S#" + s + ":" + stype + "]" +
                            "[A#" + a + ":" + ita.next() + "]\n");
                    a++;
                }

                s++;
            }
        }
        KGLog.d("Encryptions: %s", result);
    }

    /**
     *
     * @return String[] of base64 encryped Public Key and Private Key
     */
    public static String[] generateAsymmetricKeys() {
        // Original text
        String theTestText = "This is just a simple test!";

        try {
            // Generate key pair for 1024-bit RSA encryption and decryption
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            PublicKey publicKey = kp.getPublic();
            PrivateKey privateKey = kp.getPrivate();
            String[] keys = new String[2];
            keys[0] = Base64.encodeToString(publicKey.getEncoded(), BASE64_FLAGS);
            keys[1] = Base64.encodeToString(privateKey.getEncoded(), BASE64_FLAGS);
            return keys;
        } catch (Exception e) {
            KGLog.e("RSA key pair error");
        }
        return null;
    }

//    private static byte[] toByteArray(File file) throws IOException {
//        // java 7's try-with-resources statement
//        try (FileInputStream in = new FileInputStream(file);
//             FileChannel channel = in.getChannel()) {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            channel.transferTo(0, channel.size(), Channels.newChannel(out));
//            return out.toByteArray();
//        }
//    }

//    private static byte[] toDecodedBase64ByteArray(byte[] base64EncodedByteArray) {
//        return DatatypeConverter.parseBase64Binary(new String(base64EncodedByteArray, Charset.forName("UTF-8")));
//    }

    public static String asymmetricEncypt(String openText, String privateKeyText) {

        byte[] privateKeyBytes = Base64.decode(privateKeyText, BASE64_FLAGS);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            //KeySpec privateKeySpec = new X509EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            // Encode the original data with RSA private key
            byte[] encodedBytes = null;
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes = c.doFinal(openText.getBytes());
            return Base64.encodeToString(encodedBytes, BASE64_FLAGS);
        } catch (Exception exc) {
            KGLog.e("RSA encryption error: %s", exc);
        }
        return null;

    }

    public static String asymmetricDecrypt(String encryptedText, String publicKeyText) {
        byte[] publicKeyBytes = Base64.decode(publicKeyText, BASE64_FLAGS);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            // Decode the encoded data with RSA public key
            byte[] decodedBytes = null;
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, publicKey);
            decodedBytes = c.doFinal(encryptedText.getBytes());
            return new String(decodedBytes);
        } catch (Exception e) {
            KGLog.e("RSA decryption error %s", e);
        }
        return null;
    }

}
