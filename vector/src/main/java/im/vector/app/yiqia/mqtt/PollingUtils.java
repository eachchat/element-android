package im.vector.app.yiqia.mqtt;

import com.blankj.utilcode.util.LogUtils;

import java.util.concurrent.TimeUnit;

import im.vector.app.yiqia.contact.api.BaseConstant;
import im.vector.app.yiqia.contact.event.MQTTEvent;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * MQTT断线后轮询
 * <p>
 * Created by zhouguanjie on 2020/4/2.
 */
public class PollingUtils {

    //暂定为30秒轮询1次
    private int POLLING_TIME = 120;

    private Disposable disposable;

    public void togglePolling() {
        if (ModuleLoader.INSTANCE.isMQTTConnected()) {
            stopPolling();
        } else {
            startPolling();
        }
    }

    public void startPolling() {
        if (disposable != null) {
            return;
        }
        disposable = Observable.interval(0, POLLING_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    if (!UserCache.isLogin() || ModuleLoader.INSTANCE.isMQTTConnected()) {
                        stopPolling();
                        return;
                    }
                    Timber.d("PollingUtils" + System.currentTimeMillis() + "");
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_NEW_MESSAGE, null, 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_NEW_ENCRYPTION_MESSAGE, null, 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(BaseConstant.CMD_UPDATE_NOTIFY, null, 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_UPDATE_TEAM, null, 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_UPDATE_CONVERSATION, null, 0));
                });

    }

    public void pollingAndReconnectIfNeed() {
        if (disposable != null) {
            return;
        }
        disposable = Observable.interval(5, POLLING_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    LogUtils.iTag("mqtt", "120s looper");
                    if (!UserCache.isLogin() || ModuleLoader.INSTANCE.isMQTTConnected()) {
                        return;
                    }
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_UPDATE_USER, UserCache.getUpdateUserTime(), 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_UPDATE_CONTACT, "0", 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_UPDATE_CONTACT_ROOM, "0", 0));
                    MQTTService.sendMQTTEvent(new MQTTEvent(MessageConstant.CMD_UPDATE_DEPARTMENT, UserCache.getUpdateDepartmentTime(), 0));
                    // 重新连接MQTT
                    IMManager.getClient().reConnect();
                });

    }

    public void stopPolling() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }


}
