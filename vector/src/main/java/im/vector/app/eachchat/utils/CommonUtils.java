package im.vector.app.eachchat.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import im.vector.app.eachchat.base.BaseModule;
import timber.log.Timber;

/**
 * Created by zhouguanjie on 2019/8/28.
 */
public class CommonUtils {

    public synchronized static String getAndroidId() {
        return getAndroidId(BaseModule.getSession().getMyUserId());
    }

    public synchronized static String getAndroidId(String userId) {
        String ANDROID_ID;
        FileUtils.checkFileExist(BaseModule.getContext().getFilesDir().getPath() + File.separator + userId, "mqtt");
        String path = BaseModule.getContext().getFilesDir().getPath() + File.separator + userId + "mqtt";
        ANDROID_ID = DBEncryptUtils.getPassword(path);
        if (TextUtils.isEmpty(ANDROID_ID)) {
            ANDROID_ID = DBEncryptUtils.generatePassword(path);
            Timber.d("androidId"+"generate ANDROID_ID :" + ANDROID_ID);
        }
        return ANDROID_ID;
    }

    public static String listToString(List<String> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < list.size(); index++) {
            if (index != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(list.get(index));
        }
        return stringBuilder.toString();
    }

    public static int getVersionCode(Context context) {
        try {
            String thisPackageName = context.getPackageName();
            return context.getPackageManager().getPackageInfo(thisPackageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static String getVersionName(Context context) {
        try {
            String thisPackageName = context.getPackageName();
            return context.getPackageManager().getPackageInfo(thisPackageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    public static String statusFormat(String status) {
        return status;
//        return TextUtils.isEmpty(status) ? status : String.format("[%s]", status);
    }

    public static boolean isUpdate(String oldVersion, String newVersion) {
        if (oldVersion == null || newVersion == null) {
            return false;
        }
        String[] lArr = oldVersion.split("\\.");
        String[] sArr = newVersion.split("\\.");
        for (int i = 0; i < sArr.length; i++) {
            Integer lI = Integer.valueOf(lArr[i]);
            Integer sI = Integer.valueOf(sArr[i]);
            if (sI > lI) {
                return true;
            } else if (sI < lI) {
                return false;
            }
        }
        return false;
    }

    public static boolean isZN() {
        Locale locale = BaseModule.getContext().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return true;
        } else {
            return false;
        }
    }
    public static final Pattern MAIL_PATTERN = Pattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?");

    public static final Pattern WEB_URL = Pattern.compile("((?:(http|https|Http|Https|rtsp|Rtsp|ftp|Ftp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?(?:(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]){0,1}\\.)+[a-zA-Z]{2,63}|((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))))(?:\\:\\d{1,5})?)((?:\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))+[\\;\\.\\=\\?\\/\\+\\)][a-zA-Z0-9\\%\\#\\&\\-\\_\\.\\~]*)|(?:\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*))?(?:\\b|$|(?=[ -\uD7FF豈-\uFDCFﷰ-\uFFEF]))|([\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?)");

    public static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(^|(?<=\\D))((1\\d{10})|(0\\d{10,11})|(1\\d{2}-\\d{4}-\\d{4})|(0\\d{2,3}-\\d{7,8})|(\\d{7,8})|((4|8)00\\d{1}-\\d{3}-\\d{3})|((4|8)00\\d{7}))(?!\\d)");
}
