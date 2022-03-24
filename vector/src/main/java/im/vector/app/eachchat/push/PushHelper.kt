/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.eachchat.push

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.di.DefaultSharedPreferences
import im.vector.app.eachchat.BaseModule
import im.vector.app.eachchat.bean.PNSInput
import im.vector.app.eachchat.net.CloseableCoroutineScope
import im.vector.app.eachchat.net.NetConstant
import im.vector.app.eachchat.push.getui.GeTuiPush
import im.vector.app.eachchat.push.hwpush.HWPush
import im.vector.app.eachchat.push.mipush.MiPush
import im.vector.app.eachchat.push.oppoPush.OppoPush
import im.vector.app.eachchat.push.vivo.VivoPush
import im.vector.app.eachchat.service.ApiService
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.utils.YQBadgeUtils
import im.vector.app.features.home.HomeActivity
import im.vector.app.features.settings.VectorPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.session.pushers.PushersService
import org.matrix.android.sdk.api.session.room.RoomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.model.Membership
import kotlin.math.abs

class PushHelper {

    private lateinit var activeSessionHolder: ActiveSessionHolder
    private var pushClient: AbsPush? = null
    private var hasReg = false
    private var hasBind = false
    private val scope: CloseableCoroutineScope by lazy { CloseableCoroutineScope() }

    fun init() {
        if (hasReg) {
            initClient(AppCache.getPNS())
            return
        }
        val input = PNSInput()
        input.model = Build.MODEL
        input.brand = Build.BRAND
        input.manufacturer = Build.MANUFACTURER
        input.sdk = Build.VERSION.SDK_INT.toString()
        input.osVersion = Build.VERSION.RELEASE
        input.rom = RomUtils.getRom() // room is a push identifier, used to determine which push to use
        scope.launch(Dispatchers.Main) {
            val response = withContext(Dispatchers.IO) {
                ApiService.getInstance().getPNS(input)
            }
            if (response.isSuccess && response.obj != null && !TextUtils.isEmpty(response.obj!!.pns)) {
                AppCache.setPns(response.obj!!.pns)
                hasReg = true
                hasBind = false
                initClient(AppCache.getPNS())
            } else {
                if (!TextUtils.isEmpty(AppCache.getPNS())) {
                    initClient(AppCache.getPNS())
                } else {
                    initClient(TYPE_JIGUNAG_PUSH)
                }
            }
        }
    }

    private fun initClient(type: String) {
        if (pushClient != null) {
            return
        }
        pushClient = when (type) {
            TYPE_HMS       -> HWPush(BaseModule.getContext())
            TYPE_MIPUSH    -> MiPush(BaseModule.getContext())
            TYPE_OPPO_PUSH -> OppoPush(BaseModule.getContext())
            TYPE_VIVO_PUSH -> VivoPush(BaseModule.getContext())
            TYPE_GETUI     -> GeTuiPush(BaseModule.getContext())
            else           -> GeTuiPush(BaseModule.getContext())
        }
        try {
            pushClient?.init(BaseModule.getContext())
            pushClient?.startPush()
            clearNotification()
            bindDevice(pushClient?.regId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * bind the Matrix service and the push service through regId
     */
    fun bindDevice(regId: String?) {
        if (hasBind) {
            return
        }
        if (TextUtils.isEmpty(regId)) {
            return
        }
//            if (!UserCache.isLogin()) {
//                return
//            }
        try {
            val session = activeSessionHolder.getSafeActiveSession() ?: return
            val pushGateWay = NetConstant.getPushHost()
            if (TextUtils.isEmpty(pushGateWay)) return
            val profileTag = "android_" + abs(session.myUserId.hashCode())
            var deviceDisplayName = session.sessionParams.deviceId
            if (TextUtils.isEmpty(deviceDisplayName)) {
                deviceDisplayName = "Android Mobile"
            }
            val locale = BaseModule.getContext().resources.configuration.locales[0]
            // "https://chat.yunify.com/_matrix/client/r0/_matrix/push/v1/notify"
            val httpPusher = PushersService.HttpPusher(
                    regId!!,
                    "android_${AppCache.getPNS()}",
                    profileTag,
                    locale.language,
                    BaseModule.getContext().getString(R.string.app_name),
                    deviceDisplayName!!,
                    pushGateWay,
                    append = false,
                    withEventIdOnly = false
            )
            scope.launch {
                session.addHttpPusher(httpPusher)
                hasBind = true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun registerPusher() {
        if (hasBind) {
            return
        }
        if (TextUtils.isEmpty(pushClient?.regId)) {
            return
        }
        runCatching {
            val session = activeSessionHolder.getSafeActiveSession() ?: return
            val pushGateWay = NetConstant.getPushHost()
            if (TextUtils.isEmpty(pushGateWay)) return
            val profileTag = "android_" + abs(session.myUserId.hashCode())
            var deviceDisplayName = session.sessionParams.deviceId
            if (TextUtils.isEmpty(deviceDisplayName)) {
                deviceDisplayName = "Android Mobile"
            }
            val locale = BaseModule.getContext().resources.configuration.locales[0]
            // "https://chat.yunify.com/_matrix/client/r0/_matrix/push/v1/notify"
            val httpPusher = PushersService.HttpPusher(
                    pushClient?.regId!!,
                    "android_${AppCache.getPNS()}",
                    profileTag,
                    locale.language,
                    BaseModule.getContext().getString(R.string.app_name),
                    deviceDisplayName!!,
                    pushGateWay,
                    append = false,
                    withEventIdOnly = false
            )
            scope.launch {
                session.addHttpPusher(httpPusher)
                hasBind = true
            }
        }.exceptionOrNull()?.printStackTrace()
    }

    fun unregisterPusher() {
        val currentSession = activeSessionHolder.getSafeActiveSession() ?: return
        scope.launch {
            pushClient?.regId?.let {
                currentSession.removeHttpPusher(it, "android_${AppCache.getPNS()}")
                hasBind = false
            }
        }
    }

    fun startPush(holder: ActiveSessionHolder) {
        activeSessionHolder = holder
//            if (!UserCache.isLogin()) {
//                return
//            }
        if (!DefaultSharedPreferences.getInstance(BaseModule.getContext())
                        .getBoolean(VectorPreferences.SETTINGS_ENABLE_THIS_DEVICE_PREFERENCE_KEY, true)) {
            return
        }
        if (pushClient == null) {
            init()
            return
        }
        pushClient?.startPush()
    }

    fun stopPush() {
        if (pushClient == null) {
            return
        }
        clearNotification()
        pushClient?.stopPush()
        pushClient = null
    }

    fun logout() {
        stopPush()
        hasReg = false
        hasBind = false
    }

    fun getRegId(): String? {
        return if (pushClient == null) {
            null
        } else pushClient?.regId
    }

    fun setBadgeCount(context: Context?, count: Int) {
        if (pushClient == null) {
            return
        }
        pushClient?.setBadgeCount(context, count)
    }

    fun setBadge(context: Context?) {
        val currentSession = activeSessionHolder.getSafeActiveSession() ?: return
        scope.launch {
            val roomSummaries = withContext(Dispatchers.IO) {
                val membershipList: MutableList<Membership> = ArrayList()
                membershipList.add(Membership.JOIN)
                val params = RoomSummaryQueryParams.Builder().apply {
                    memberships = membershipList
                }.build()
                currentSession.getRoomSummaries(params)
            }
            if (roomSummaries.isEmpty()) {
                YQBadgeUtils.setCount(0, context)
            }
            var count = 0
            roomSummaries.forEach {
                count += it.notificationCount
            }
            YQBadgeUtils.setCount(count, context)
        }
    }

    fun clickNotification(context: Context) {
        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun clearNotification() {
        if (pushClient == null) {
            return
        }
        pushClient?.clearPush()
    }

    companion object {
        private const val TYPE_HMS = "huawei"
        private const val TYPE_MIPUSH = "xiaomi"
        private const val TYPE_OPPO_PUSH = "oppo"
        private const val TYPE_VIVO_PUSH = "vivo"
        private const val TYPE_JIGUNAG_PUSH = "jiguang"
        private const val TYPE_GETUI = "getui"
        private const val TYPE_FIREBASE = "firebase"

        private var INSTANCE: PushHelper? = null

        @JvmStatic
        fun getInstance() = INSTANCE ?: PushHelper().also { INSTANCE = it }

        /**
         * Used to force [getInstance] to create a new instance
         * next time it's called.
         */
        @JvmStatic
        fun destroyInstance() {
            INSTANCE?.scope?.close()
            INSTANCE = null
        }
    }
}
