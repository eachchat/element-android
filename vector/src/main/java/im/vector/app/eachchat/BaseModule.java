package im.vector.app.eachchat;

import android.content.Context;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class BaseModule {

    private static Context mContext;

    public static void init(Context context) {
        if (mContext != null) return;
        mContext = context;
    }

    public static Context getContext() {
        return mContext;
    }

}
