package im.vector.app.yiqia.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import im.vector.app.BuildConfig;
import im.vector.app.eachchat.BaseModule;
import im.vector.app.eachchat.net.NetConstant;
import im.vector.app.eachchat.utils.CommonUtils;
import im.vector.app.yiqia.contact.event.MQTTConnectEvent;
import im.vector.app.yiqia.mqtt.cmd.AbstractCMD;
import im.vector.app.yiqia.mqtt.cmd.UpdateContactCMD;
import im.vector.app.yiqia.mqtt.cmd.UpdateContactRoomCMD;
import im.vector.app.yiqia.mqtt.cmd.UpdateDepartmentCMD;
import im.vector.app.yiqia.mqtt.cmd.UpdateUserCMD;
import im.vector.app.yiqia.mqtt.data.IMParam;
import im.vector.app.yiqia.mqtt.data.MQTTMsgBean;
import timber.log.Timber;

/**
 * Created by zhouguanjie on 2019/8/27.
 */
public class MQTTManager implements MqttCallback {

    private String userName = NetConstant.USER_NAME;
    private String passWord = NetConstant.PASS_WORD;
    private MqttAndroidClient mClient;
    private MqttConnectOptions mOptions;
    private String TAG = "mqtt";
    private Context mContext;
    private ConnectStatus status = ConnectStatus.NotConnected;
    private IMqttToken mToken;
    private IMCallback.ConnectCallback mConnectCallback;
    private Gson gson;
    private int TIME[] = new int[]{1, 2, 4, 8, 12, 16, 30};
    private int reTryCount = 0;
    private AtomicBoolean isTrying = new AtomicBoolean(false);

    public enum ConnectStatus {
        NotConnected, Connected, Connecting, ConnectError
    }

    private Map<String, AbstractCMD> cmds;

    public void init(Context context, IMParam param) {
        mContext = context;
        gson = new Gson();
        initClient();
        cmds = new HashMap<>();

        UpdateUserCMD updateUserCMD = new UpdateUserCMD();
        cmds.put(updateUserCMD.getCMD(), updateUserCMD);

        UpdateContactCMD updateContactCMD = new UpdateContactCMD();
        cmds.put(updateContactCMD.getCMD(), updateContactCMD);

        UpdateContactRoomCMD updateContactRoomCMD = new UpdateContactRoomCMD();
        cmds.put(updateContactRoomCMD.getCMD(), updateContactRoomCMD);

        UpdateDepartmentCMD updateDepartmentCMD = new UpdateDepartmentCMD();
        cmds.put(updateDepartmentCMD.getCMD(), updateDepartmentCMD);


    }

    private void initClient() {
        String ANDROID_ID = CommonUtils.getAndroidId();
        // Mqtt url may changed, every time call init, we need get url
        String serverURI = NetConstant.getMqttHostWithProtocol();
        mClient = new MqttAndroidClient(mContext, serverURI, BaseModule.getSession().getMyUserId() + "|" + ANDROID_ID);
        mClient.setCallback(this); //设置监听订阅消息的回调
        mOptions = new MqttConnectOptions();
        mOptions.setCleanSession(true); //设置是否清除缓存
        mOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
        mOptions.setKeepAliveInterval(20); //设置心跳包发送间隔，单位：秒
        mOptions.setUserName(userName); //设置用户名
        mOptions.setPassword(passWord.toCharArray()); //设置密码
    }

    public void connect(String token, IMCallback.ConnectCallback callback) {
        Timber.i(TAG +"connect");
        if (mClient == null) {
            initClient();
        }
        this.mConnectCallback = callback;
        if (mClient.isConnected()) {
            status = ConnectStatus.Connected;
            reTryCount = 0;
        } else if (status != ConnectStatus.Connecting) {
            synchronized (this) {
                if (status != ConnectStatus.Connecting) {
                    status = ConnectStatus.Connecting;
                    try {
                        mToken = mClient.connect(mOptions, null, mActionListener);
                        reTryCount = 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                        connectionLost(e);
                        mToken = null;
                    }
                }
            }
        }
    }

    public void reConnect() {
        if (mClient == null) {
            return;
        }
        if (mClient.isConnected()) {
            status = ConnectStatus.Connected;
            reTryCount = 0;
        } else if (status != ConnectStatus.Connecting) {
            Timber.i(TAG, "reConnect");
            synchronized (this) {
                if (status != ConnectStatus.Connecting) {
                    status = ConnectStatus.Connecting;
                    EventBus.getDefault().post(new MQTTConnectEvent(MQTTConnectEvent.CONNECT_RETRY));
                    try {
                        mToken = mClient.connect(mOptions, null, mActionListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                        connectionLost(e);
                        mToken = null;
                    }
                }
            }
        }
    }

    public void disconnect() {
        try {
            Timber.i(TAG, "disconnect");
            if (mClient != null) {
                mClient.disconnect(); //断开连接
                mClient = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        Timber.i(TAG, "connectionLost ");
        status = ConnectStatus.ConnectError;
        if (mConnectCallback != null) {
            mConnectCallback.onError(MessageConstant.CONNECT_LOST);
        }
        EventBus.getDefault().post(new MQTTConnectEvent(MQTTConnectEvent.CONNECT_LOST));
        tryConnect();
    }

    public void tryConnect() {
        if (isTrying.get()) {
            return;
        }
        isTrying.set(true);
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (status != ConnectStatus.Connected) {
                    if (reTryCount < TIME.length - 1) {
                        reTryCount++;
                    }
                    Timber.e("time:%s", TIME[reTryCount] * 1000);
                    try {
                        sleep(TIME[reTryCount] * 1000L);
                        if (NetworkUtils.isConnected()
                                && status != ConnectStatus.Connected
                                && status != ConnectStatus.Connecting) {
                            reConnect();
                        }
                        if (mClient == null) {
                            isTrying.set(false);
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isTrying.set(false);
                    }
                }
            }
        }.start();


    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        EventBus.getDefault().post(new MQTTConnectEvent(MQTTConnectEvent.CONNECT_SUCCESS));
        try {
            byte[] payload = message.getPayload();
            MQTTMsgBean bean = gson.fromJson(new String(payload), MQTTMsgBean.class);
            if (bean != null && !TextUtils.isEmpty(bean.getName())) {
                if (bean.getValue() != null) {
                    LogUtils.i(TAG, "messageArrived " + bean.getClientId() + " " + bean.getName() + " " + bean.getValue().toString());
                } else {
                    LogUtils.i(TAG, "messageArrived " + bean.getClientId() + " " + bean.getName());
                }
                if (cmds == null) {
                    return;
                }
                AbstractCMD cmd = cmds.get(bean.getName());
                if (cmd != null && cmd.isCanExecute()) {
                    try {
                        //fix me use ARouter
                        ComponentName componentName = new ComponentName(BuildConfig.APPLICATION_ID, "ai.workly.eachchat.android.base.MQTTService");
                        Intent intent = new Intent();
                        intent.setComponent(componentName);
                        mContext.startService(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cmd.execute(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Timber.i(TAG, "deliveryComplete ");
    }

    private IMqttActionListener mActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Timber.i(TAG, "onSuccess");
            try {
                if (mClient != null) {
                    mClient.subscribe(BaseModule.getSession().getMyUserId(), 2);
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
            if (mConnectCallback != null) {
                mConnectCallback.onSuccess();
            }
            status = ConnectStatus.Connected;
            EventBus.getDefault().post(new MQTTConnectEvent(MQTTConnectEvent.CONNECT_SUCCESS, true));
            isTrying.set(false);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Timber.i(TAG, "onFailure");
            exception.printStackTrace();
            if (mConnectCallback != null) {
                mConnectCallback.onError(MessageConstant.CONNECT_FAILED);
            }
            status = ConnectStatus.ConnectError;
            EventBus.getDefault().post(new MQTTConnectEvent(MQTTConnectEvent.CONNECT_LOST));
            tryConnect();
        }
    };

}
