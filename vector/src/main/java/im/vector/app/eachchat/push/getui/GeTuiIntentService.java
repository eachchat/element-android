package im.vector.app.eachchat.push.getui;

import android.content.Context;

import com.facebook.stetho.common.LogUtil;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;

import im.vector.app.eachchat.push.PushHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhouguanjie on 2021/3/1.
 */
public class GeTuiIntentService extends GTIntentService {
    @Override
    public void onReceiveServicePid(Context context, int i) {

    }

    @Override
    public void onReceiveClientId(Context context, String clientId) {
        // 接收 cid
        LogUtil.i("## geTui onReceiveClientId clientId = " + clientId);
        Observable.create(emitter -> {
            PushHelper.getInstance().bindDevice(clientId);
            emitter.onNext(new Object());
        }).subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    // do nothing
                });
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        // 透传消息的处理，详看 SDK demo
        LogUtil.i("## geTui onReceiveMessageData gtTransmitMessage = " + gtTransmitMessage.toString());
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        // cid 离线上线通知
        LogUtil.i("## geTui onReceiveOnlineState");
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        // 各种事件处理回执
        LogUtil.i("## geTui onReceiveCommandResult");
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        // 通知到达，只有个推通道下发的通知会回调此方法
        LogUtil.i("## geTui onNotificationMessageArrived");
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        // 通知点击，只有个推通道下发的通知会回调此方法
        LogUtil.i("## geTui onNotificationMessageClicked");
    }
}
