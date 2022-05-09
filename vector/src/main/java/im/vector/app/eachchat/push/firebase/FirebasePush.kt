package ai.workly.eachchat.android.push.firebase

import android.app.NotificationManager
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.push.AbsPush
import im.vector.app.eachchat.push.PushHelper.Companion.getInstance

/**
 * Created by zhouguanjie on 2020/12/24.
 */
class FirebasePush(context: Context?) : AbsPush(context) {

    var token : String? = null

    override fun init(context: Context) {
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                LogUtils.iTag("push", "Fetching FCM registration token failed")
//                PushUtils.downgrading()
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result
            getInstance().bindDevice(token)
            LogUtils.iTag("push", "Fetching FCM registration token success token = $token")
        })
    }

    override fun startPush() {

    }

    override fun stopPush() {
    }

    override fun getRegId(): String? {
//        val token = FirebaseMessaging.getInstance().token
//        return token.result
        return token
    }

    override fun setBadgeCount(context: Context?, count: Int) {
    }

    override fun clearPush() {
        val notificationManager = BaseModule.getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
