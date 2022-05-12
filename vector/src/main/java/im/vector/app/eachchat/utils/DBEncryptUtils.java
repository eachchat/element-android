package im.vector.app.eachchat.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Created by zhouguanjie on 2019/12/2.
 */
public class DBEncryptUtils {

    public static String generatePassword(String filePath) {
        return generatePassword(null, filePath);
    }

    private static String generatePassword(String password, String filePath) {
        String content = TextUtils.isEmpty(password) ? UUID.randomUUID().toString() : password;
        String encrypted = RSAEncryption.getInstance().encrypt(content);
        File file = new File(filePath);
        if (file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = encrypted.getBytes();
                fos.write(buffer);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public static String getPassword(String filePath) {
        String retString = "";
        File file = new File(filePath);
        if (file.exists()) {
            if (file.length() > 10 * 1024) {
                file.delete();
                return retString;
            }
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                String readString = new String(buffer);
                if (isBase64Encoded(readString)) {
                    retString = RSAEncryption.getInstance().decrypt(readString);
                } else {
                    retString = generatePassword(readString, filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return retString;
    }

    private static boolean isBase64Encoded(String content) {
        if (TextUtils.isEmpty(content)) return false;
        if (content.length() % 4 != 0) {
            return false;
        }
        String pattern = "^[a-zA-Z0-9/+]*={0,2}$";
        return Pattern.matches(pattern, content);
    }

}
