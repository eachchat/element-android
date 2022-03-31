package im.vector.app.yiqia.mqtt;

import android.content.Context;

import im.vector.app.yiqia.mqtt.data.IMParam;

/**
 * Created by zhouguanjie on 2019/8/6.
 */
public class IMClientImpl implements IMClient {

    private Context mContext;

    private MQTTManager mMQTT = new MQTTManager();

    @Override
    public void init(Context context, IMParam param) {
        this.mContext = context;
        mMQTT.init(context, param);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void connect(String token, IMCallback.ConnectCallback callback) {
        mMQTT.connect(token, callback);
    }

    @Override
    public void reConnect() {
        mMQTT.reConnect();
    }

    @Override
    public void disconnect() {
        mMQTT.disconnect();
    }
}
