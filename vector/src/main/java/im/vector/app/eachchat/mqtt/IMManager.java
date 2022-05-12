package im.vector.app.eachchat.mqtt;

/**
 * Created by zhouguanjie on 2019/8/6.
 */
public class IMManager {

    private static IMClient imClient;

    public static IMClient getClient() {
        if (imClient == null) {
            imClient = new IMClientImpl();
        }
        return imClient;
    }

}
