package im.vector.app.eachchat.push.getui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.facebook.stetho.common.LogUtil;

import im.vector.app.eachchat.push.PushHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhouguanjie on 2021/3/1.
 */
public class GeTuiIntentService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
