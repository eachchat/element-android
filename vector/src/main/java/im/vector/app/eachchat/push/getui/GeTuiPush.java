package im.vector.app.eachchat.push.getui;

import android.app.NotificationManager;
import android.content.Context;

import com.facebook.stetho.common.LogUtil;
import com.igexin.sdk.PushManager;

import im.vector.app.eachchat.BaseModule;
import im.vector.app.eachchat.push.AbsPush;


/**
 * Created by zhouguanjie on 2021/3/1.
 */
public class GeTuiPush extends AbsPush {

    public GeTuiPush(Context context) {
        super(context);
    }

    @Override
    public void init(Context context) {
        LogUtil.i("## GeTui init");
        PushManager.getInstance().initialize(context);
    }

    @Override
    public void startPush() {
        LogUtil.i("## GeTui startPush");
        PushManager.getInstance().turnOnPush(BaseModule.getContext());
    }

    @Override
    public void stopPush() {
        LogUtil.i("## GeTui stopPush");
        PushManager.getInstance().turnOffPush(BaseModule.getContext());
    }

    @Override
    public String getRegId() {
        LogUtil.i("## GeTui getRegId = " + PushManager.getInstance().getClientid(BaseModule.getContext()));
        return PushManager.getInstance().getClientid(BaseModule.getContext());
    }

    @Override
    public void setBadgeCount(Context context, int count) {

    }

    @Override
    public void clearPush() {
        LogUtil.i("## GeTui clearNotifications");
        NotificationManager notificationManager =
                (NotificationManager) BaseModule.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
