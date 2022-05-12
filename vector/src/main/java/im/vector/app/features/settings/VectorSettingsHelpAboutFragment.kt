/*
 * Copyright 2019 New Vector Ltd
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

package im.vector.app.features.settings

import ai.workly.eachchat.android.usercenter.api.VersionUpdateHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import im.vector.app.BuildConfig
import im.vector.app.R
import im.vector.app.core.preference.VectorPreference
import im.vector.app.core.utils.FirstThrottler
import im.vector.app.core.utils.copyToClipboard
import im.vector.app.core.utils.openAppSettingsPage
import im.vector.app.core.utils.openUrlInChromeCustomTab
import im.vector.app.eachchat.contact.api.BaseConstant
import im.vector.app.eachchat.service.LoginApi.Companion.GMS_URL
import im.vector.app.eachchat.utils.ToastUtil
import im.vector.app.eachchat.version_update.EachChatSettingsService
import im.vector.app.features.version.VersionProvider
import im.vector.app.features.webview.VectorWebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.Matrix
import javax.inject.Inject

class VectorSettingsHelpAboutFragment @Inject constructor(
        private val versionProvider: VersionProvider
) : VectorSettingsBaseFragment() {

    override var titleRes = R.string.preference_root_help_about
    override val preferenceXmlRes = R.xml.vector_settings_help_about

    val newVersion = MutableLiveData(VersionUpdateHelper.getLastVersion())

    private val firstThrottler = FirstThrottler(1000)

    private var versionInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newVersion.observe(this) {
            if (savedInstanceState == null && !versionInitialized){
                versionInitialized = true
                return@observe
            }
            findPreference<VectorPreference>("SETTINGS_VERSION_CHECK_UPDATE_PREFERENCE_KEY")!!.drawableResource = if (newVersion.value?.needUpdate() == true) R.drawable.ic_new_version else null
            if (it.needUpdate()) {
                VersionUpdateHelper.showUpdateDialog(requireActivity(), it)
            } else {
                ToastUtil.showSuccess(requireContext() ,R.string.toast_already_up_to_date)
            }
        }
    }

    override fun bindPref() {
        // Help
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_HELP_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (firstThrottler.canHandle() is FirstThrottler.CanHandlerResult.Yes) {
                openUrlInChromeCustomTab(requireContext(), null, VectorSettingsUrls.HELP)
            }
            false
        }

        // preference to start the App info screen, to facilitate App permissions access
        findPreference<VectorPreference>(APP_INFO_LINK_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.let { openAppSettingsPage(it) }
            true
        }

        // application version
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_VERSION_PREFERENCE_KEY)!!.let {
            it.summary = BuildConfig.VERSION_NAME
//            it.summary = buildString {
//                append(versionProvider.getVersion(longFormat = false, useBuildNumber = true))
//                if (BuildConfig.DEBUG) {
//                    append(" ")
//                    append(BuildConfig.GIT_BRANCH_NAME)
//                }
//            }
//
//            it.setOnPreferenceClickListener { pref ->
//                copyToClipboard(requireContext(), pref.summary)
//                true
//            }
        }

        // SDK version
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_SDK_VERSION_PREFERENCE_KEY)!!.let {
            it.summary = Matrix.getSdkVersion()

            it.setOnPreferenceClickListener { pref ->
                copyToClipboard(requireContext(), pref.summary)
                true
            }
        }

        // olm version
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_OLM_VERSION_PREFERENCE_KEY)!!
                .summary = session.cryptoService().getCryptoVersion(requireContext(), false)

        //法律信息
        //版权声明
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_COPYRIGHT_NOTICE_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = VectorWebViewActivity.getIntent(requireContext(), "file:///android_asset/copyright-notice.html", getString(R.string.yiqia_copyright_notice))
            requireActivity().startActivity(intent)
            false
        }

        //用户协议
        findPreference<VectorPreference>(VectorPreferences.SETTINGS_USER_POLICY_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = VectorWebViewActivity.getIntent(requireContext(), "file:///android_asset/user-agreements.html", getString(R.string.yiqia_user_policy))
            startActivity(intent)
            false
        }

        //隐私政策
        findPreference<VectorPreference>(VectorPreferences.SETTING_PRIVACY_POLICY_PREFERENCE_KEY)!!
                .onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = VectorWebViewActivity.getIntent(requireContext(), "file:///android_asset/privacy-policy.html", getString(R.string.yiqia_privacy_policy))
            startActivity(intent)
            false
        }

        findPreference<VectorPreference>("SETTINGS_VERSION_CHECK_UPDATE_PREFERENCE_KEY")!!.isVisible = BuildConfig.FLAVOR != BaseConstant.FLAVOR_XIAOMI
        findPreference<VectorPreference>("SETTINGS_VERSION_CHECK_UPDATE_PREFERENCE_KEY")!!.drawableResource = if (newVersion.value?.needUpdate() == true) R.drawable.ic_new_version else null
        findPreference<VectorPreference>("SETTINGS_VERSION_CHECK_UPDATE_PREFERENCE_KEY")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            checkNewVersion()
            false
        }
    }

    private val _service = EachChatSettingsService.getInstance(GMS_URL)

    private fun checkNewVersion() {
//        loading.value = true
        lifecycleScope.launch(Dispatchers.IO) {
            val result = kotlin.runCatching {
                return@runCatching _service.checkUpdate()
            }
            result.getOrNull()?.obj?.let {
                VersionUpdateHelper.setLastVersion(it)
                newVersion.postValue(it)
            }
                    ?: VersionUpdateHelper.getLastVersion().run {
                        newVersion.postValue(this)
                    }
            // loading.postValue(false)
        }
    }

    companion object {
        private const val APP_INFO_LINK_PREFERENCE_KEY = "APP_INFO_LINK_PREFERENCE_KEY"
    }
}
