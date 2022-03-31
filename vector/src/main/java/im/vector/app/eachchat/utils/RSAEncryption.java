package im.vector.app.eachchat.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import im.vector.app.eachchat.BaseModule;

/**
 * Created by chengww on 2019-12-27
 *
 * @author chengww
 */
public class RSAEncryption {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_SHA = "SHA";


    private X509Certificate publicKey;
    private PrivateKey privateKey;

    public static void init(Context context) {
//        if (RSAEncryption.context != null) return;
//        RSAEncryption.context = context;
    }

    private RSAEncryption() {
        initKeys();
    }

    private void initKeys() {
        privateKey = getPrivateKeyFromPem();
        publicKey = getPublicKeyFromCrt();
    }

    public static RSAEncryption getInstance() {
        return EncryptionHolder.holder;
    }

    private static class EncryptionHolder {
        private static RSAEncryption holder = new RSAEncryption();
    }

    private PrivateKey getPrivateKeyFromPem() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(BaseModule.getContext().getResources().getAssets().open("private")))) {
            String s = br.readLine();
            StringBuilder str = new StringBuilder();
            s = br.readLine();
            while (s.charAt(0) != '-') {
                str.append(s).append("\r");
                s = br.readLine();
            }
            byte[] b = Base64.decode(str.toString(), Base64.DEFAULT);
            KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(b);
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private X509Certificate getPublicKeyFromCrt() {
        try (InputStream is = BaseModule.getContext().getResources().getAssets().open("public")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decryptByPublicKey(byte[] data) {
        byte[] result = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            result = cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public byte[] encryptByPrivateKey(byte[] data) {
        byte[] result = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            result = cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String encrypt(String unBase64edString) {
        if (TextUtils.isEmpty(unBase64edString)) return unBase64edString;
        byte[] encrypt = encryptPKCS1(unBase64edString.getBytes());
        return encrypt == null ? null : Base64.encodeToString(encrypt, Base64.NO_WRAP);
    }

    public String decrypt(String base64edString) {
        if (TextUtils.isEmpty(base64edString)) return base64edString;
        byte[] encryptedData = Base64.decode(base64edString, Base64.NO_WRAP);
        byte[] decrypt = decryptPKCS1(encryptedData);
        return decrypt == null ? null : new String(decrypt);
    }

    public byte[] encryptSHA(byte[] data) {
        byte[] result = null;
        try {
            MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
            sha.update(data);
            result = sha.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String sign(byte[] data) {
        String result = null;
        byte[] sha = encryptSHA(data);
        if (sha == null) return null;
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(sha);
            byte[] sign = signature.sign();
            if (sign == null) return null;
            result = Base64.encodeToString(sign, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean verify(byte[] data, PublicKey publicKey, String sign) {
        if (data == null) return false;
        boolean result = false;
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data);
            result = signature.verify(Base64.decode(sign, Base64.NO_WRAP));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String encryptPKCS1(String unBase64edString) {
        if (TextUtils.isEmpty(unBase64edString)) return unBase64edString;
        byte[] encrypt = encryptPKCS1(unBase64edString.getBytes());
        return encrypt == null ? null : Base64.encodeToString(encrypt, Base64.NO_WRAP);
    }

    public String decryptPKCS1(String base64edString) {
        if (TextUtils.isEmpty(base64edString)) return base64edString;
        byte[] encryptedData = Base64.decode(base64edString, Base64.NO_WRAP);
        byte[] decrypt = decryptPKCS1(encryptedData);
        return decrypt == null ? null : new String(decrypt);
    }

    public byte[] encryptPKCS1(byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decryptPKCS1(byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
