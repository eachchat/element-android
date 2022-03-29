package im.vector.app.eachchat;

import android.content.Context;

import org.matrix.android.sdk.api.session.Session;

import im.vector.app.core.di.ActiveSessionHolder;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class BaseModule {

    private static Context mContext;

    private static Session mSession;

    public static void init(Context context) {
        if (mContext != null) return;
        mContext = context;
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setSession(Session session) {
        mSession = session;
    }

    public static Session getSession() {
        return mSession;
    }
}
