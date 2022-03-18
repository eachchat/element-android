package im.vector.app.eachchat.utils;

/**
 * Created by zhouguanjie on 2020/1/16.
 */
public class AppCache {

    private final static String KEY_GET_PNS = "key_get_pns";
    private final static String KEY_BIND_DEVICE = "key_bind_device";
    private static final String KEY_PUSH_ENABLE = "key_push_enable";
    private final static String KEY_REQUEST_BIND_TIME = "key_request_bind_time";


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

    public static boolean hasBindDevice() {
        return SPUtils.get(KEY_BIND_DEVICE, false);
    }

    public static void setBindDevice(boolean isBind) {
        SPUtils.put(KEY_BIND_DEVICE, isBind);
    }

    public static void setRequestPNSTime(long time) {
        SPUtils.put(KEY_REQUEST_BIND_TIME, time);
    }

    public static long getRequestPNSTime() {
        return SPUtils.get(KEY_REQUEST_BIND_TIME, 0L);
    }

}
