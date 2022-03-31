package im.vector.app.yiqia.mqtt;

import android.content.Context;

import im.vector.app.yiqia.mqtt.data.IMParam;

/**
 * Created by zhouguanjie on 2019/8/6.
 */
public interface IMClient {

    public void init(Context context, IMParam param);

    public void connect(String token, IMCallback.ConnectCallback callback);

    public void reConnect();

    public Context getContext();

    public void disconnect();
}
