package im.vector.app.eachchat.utils;

/**
 * Created by zhouguanjie on 2020/1/16.
 */
public class AppCache {

    private final static String KEY_GET_PNS = "key_get_pns";
    private static final String KEY_PUSH_ENABLE = "key_push_enable";


    public static String getPNS() {
        return SPUtils.get(KEY_GET_PNS, "jiguang");
    }

    public static void setPns(String pns) {
        SPUtils.put(KEY_GET_PNS, pns);
    }

    public static boolean getPushEnable() {
        return SPUtils.get(KEY_PUSH_ENABLE, true);
    }

    public static void setPushEnable(boolean isEnable) {
        SPUtils.put(KEY_PUSH_ENABLE, isEnable);
    }

}
