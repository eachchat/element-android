package ai.workly.eachchat.android.usercenter.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import im.vector.app.BuildConfig
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.eachchat.service.LoginApi.Companion.GMS_URL
import im.vector.app.eachchat.ui.dialog.AlertDialog
import im.vector.app.eachchat.utils.SPUtils
import im.vector.app.eachchat.utils.ToastUtil
import im.vector.app.eachchat.version_update.EachChatSettingsService
import im.vector.app.eachchat.version_update.NewVersionEvent
import im.vector.app.eachchat.version_update.VersionUpdateResult
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.matrix.android.sdk.api.extensions.orFalse
import java.lang.ref.WeakReference

/**
 * Created by chengww on 2020/11/11
 * @author chengww
 */
class VersionUpdateHelper(activity: VectorBaseActivity<*>) {
    private val contextHolder: WeakReference<VectorBaseActivity<*>> = WeakReference(activity)

    private val _service = EachChatSettingsService.getInstance(GMS_URL)

    private var job: Job? = null

    /**
     *
     * Check the app's version, and <strong>show the update dialog</strong> when need update
     * @param withLoading if should show the loading while check the version
     *
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun checkVersionUpdate(withLoading: Boolean = false) {
        if (contextHolder.get() == null || job?.isActive.orFalse()) return
        if (withLoading) contextHolder.get()?.showWaitingView()
        job = GlobalScope.launch(Dispatchers.IO) {
            val result = kotlin.runCatching {
                return@runCatching _service.checkUpdate()
            }
            // After checked, first dismiss the loading dialog
            // And check the result's state(success/fail)
            contextHolder.get()?.let {
                withContext(Dispatchers.Main) {
                    if (withLoading) it.hideWaitingView()
                    // Failure
                    if (result.isFailure) {
                        // Get the local version and check
                        internalVersionCheck(getLastVersion(), it, withLoading)
                    }
                }
            }
            // Check the version object
            result.getOrNull()?.obj?.let { version ->
                contextHolder.get()?.let {
                    // Successful get the version object
                    setLastVersion(version)
                    internalVersionCheck(version, it, withLoading)
                }

            }
        }
    }

    private suspend fun internalVersionCheck(version: VersionUpdateResult, it: VectorBaseActivity<*>, withLoading: Boolean) {
        withContext(Dispatchers.Main) {
            if (version.needUpdate()) showUpdateDialog(it, version)
            // already_up_to_date with remote success/local state
            else if (withLoading) ToastUtil.showSuccess(it, R.string.toast_already_up_to_date)
            // With no need to update, and user don't need loading(toast) hint
            // Do nothing here
        }
    }

    /**
     * Call this function on [BaseActivity]'s destroy
     */
    fun onDestroy() {
        job?.cancel()
        contextHolder.clear()
    }

    companion object {
        private const val SP_VERSION_UPDATE_TIME = "sp_version_update_time"
        private const val SP_LAST_VERSION = "sp_last_version"
        private const val SP_LAST_VERSION_CODE = "sp_last_version_code"
        // private const val UPDATE_KEY = "each-chat-android-update"
        private const val SHOW_UPDATE_RED_TIPS = "SHOW_UPDATE_RED_TIPS"

        @JvmStatic
        fun setAppCheckUpdateTime(time: Long = System.currentTimeMillis()) = SPUtils.put(SP_VERSION_UPDATE_TIME, time)

        @JvmStatic
        fun getAppCheckUpdateTime(): Long = SPUtils.get(SP_VERSION_UPDATE_TIME, 0L)

        @JvmStatic
        fun setAppUpdateVersionCode(code: String) = SPUtils.put(SP_LAST_VERSION_CODE, code)

        @JvmStatic
        fun getAppUpdateVersionCode(): String = SPUtils.get(SP_LAST_VERSION_CODE, "")


        @JvmStatic
        fun setShowUpdateTips(show: Boolean) = SPUtils.put(SHOW_UPDATE_RED_TIPS, show)

        @JvmStatic
        fun canShowUpdateTips(): Boolean = SPUtils.get(SHOW_UPDATE_RED_TIPS, true)

        fun getLastVersion(): VersionUpdateResult = SPUtils.get(SP_LAST_VERSION, VersionUpdateResult())

        fun setLastVersion(version: VersionUpdateResult) {
            SPUtils.put(SP_LAST_VERSION, version)
            setAppUpdateVersionCode(BuildConfig.VERSION_NAME)
            setAppCheckUpdateTime()
            EventBus.getDefault().post(NewVersionEvent())
        }

        @JvmStatic
        fun showUpdateDialog(activity: Activity, version: VersionUpdateResult) {
            if (activity.isFinishing || activity.isDestroyed) return
            val view: View = activity.layoutInflater.inflate(R.layout.update_dialog, null, false)
            val negBtn = view.findViewById<View>(R.id.btn_neg)
            val posBtn = view.findViewById<View>(R.id.btn_pos)
            val titleTV = view.findViewById<TextView>(R.id.update_title)
            val updateTV = view.findViewById<TextView>(R.id.update_tv)
            val alertDialog = AlertDialog(activity).builder()
                    .onlyShowCustom()
                    .setNotShowHint(true)
                    .setCustomLayout(view)
                    .hideButtonDivider()
            titleTV.text = version.verName
            updateTV.text = version.description
            updateTV.movementMethod = ScrollingMovementMethod.getInstance()
            // Show cancel button if it is not force update
            if (version.isForceUpdate) {
                negBtn.visibility = View.GONE
                alertDialog.setCancelable(false)
            }
            negBtn.setOnClickListener {
                alertDialog.dismiss()
            }
            posBtn.setOnClickListener {
                val result = kotlin.runCatching {
                    val intent = Intent().also {
                        it.action = Intent.ACTION_VIEW
                        it.data = Uri.parse(version.downloadUrl)
                    }
                    activity.startActivity(intent)
                }
                if (result.isFailure) {
                    ToastUtil.showError(activity, activity.getString(R.string.toast_no_supported_app_download))
                }
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }
}
