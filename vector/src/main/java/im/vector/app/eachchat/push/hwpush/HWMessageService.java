package im.vector.app.eachchat.push.hwpush;

import com.facebook.stetho.common.LogUtil;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import im.vector.app.eachchat.push.PushHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhouguanjie on 2020/1/16.
 */
public class HWMessageService extends HmsMessageService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Observable.create(emitter -> {
            PushHelper.getInstance().bindDevice(token);
            emitter.onNext(new Object());
        }).subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    // do nothing
                    LogUtil.i("## hw onNewToken");
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
//            PushUtils.syncMessage(getApplicationContext());
//            PushUtils.setBadge(getApplicationContext());
            LogUtil.i("## hw onMessageReceived remoteMessage = " + remoteMessage.getNotification().getNotifyId());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
