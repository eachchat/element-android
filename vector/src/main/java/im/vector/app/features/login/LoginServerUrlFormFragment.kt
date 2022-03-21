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

package im.vector.app.features.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import im.vector.app.R
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.core.utils.openUrlInChromeCustomTab
import im.vector.app.databinding.FragmentLoginServerUrlFormBinding
import im.vector.app.yiqia.utils.ToastUtil
import im.vector.app.yiqia.utils.string.StringUtils.highlightKeyword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.failure.Failure
import reactivecircus.flowbinding.android.widget.textChanges
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * In this screen, the user is prompted to enter a homeserver url
 */
class LoginServerUrlFormFragment @Inject constructor() :
    AbstractLoginFragment<FragmentLoginServerUrlFormBinding>() {

    var isSelectOrg = false

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginServerUrlFormBinding {
        return FragmentLoginServerUrlFormBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupHomeServerField()
    }

    private fun setupViews() {
        views.loginServerUrlFormLearnMore.debouncedClicks { learnMore() }
        views.loginServerUrlFormClearHistory.debouncedClicks { clearHistory() }
        views.loginServerUrlFormSubmit.debouncedClicks { submit() }
        //选择组织名后不会再次弹出选择框
        views.loginServerUrlFormHomeServerUrl.setOnItemClickListener { _, _, _, _ ->
            isSelectOrg = true
        }
        views.loginServerUrlFormHomeServerUrl.doAfterTextChanged { editable ->
            if (editable.isNullOrBlank()) {
                views.loginServerUrlFormHomeServerUrl.dismissDropDown()
                return@doAfterTextChanged
            }
            loginViewModel.getOrgNames(editable.toString().trim()) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val orgNamesList: List<String> = it?.toMutableList() ?: listOf()
                    views.loginServerUrlFormHomeServerUrl.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            R.layout.item_completion_homeserver,
                            orgNamesList.highlightKeyword(editable.toString())
                        )
                    )
                    if (orgNamesList.isNotEmpty() && !editable.isNullOrBlank() && !isSelectOrg) {
                        isSelectOrg = false
                        views.loginServerUrlFormHomeServerUrl.showDropDown()
                    }
                }
            }
        }
    }

    private fun setupHomeServerField() {
        views.loginServerUrlFormHomeServerUrl.textChanges()
            .onEach {
                //views.loginServerUrlFormHomeServerUrlTil.error = null
                views.loginServerUrlFormSubmit.isEnabled = it.isNotBlank()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        views.loginServerUrlFormHomeServerUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                views.loginServerUrlFormHomeServerUrl.dismissDropDown()
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun setupUi(state: LoginViewState) {
        when (state.serverType) {
            ServerType.EMS -> {
                views.loginServerUrlFormIcon.isVisible = true
                //views.loginServerUrlFormTitle.text = getString(R.string.login_connect_to_modular)
                views.loginServerUrlFormText.text =
                    getString(R.string.login_server_url_form_modular_text)
                views.loginServerUrlFormLearnMore.isVisible = true
                views.loginServerUrlFormHomeServerUrlTil.hint =
                    getText(R.string.login_server_url_form_modular_hint)
                views.loginServerUrlFormNotice.text =
                    getString(R.string.login_server_url_form_modular_notice)
            }
            else -> {
                views.loginServerUrlFormIcon.isVisible = false
                //views.loginServerUrlFormTitle.text = getString(R.string.login_server_other_title)
                views.loginServerUrlFormText.text = getString(R.string.organization_name)
                views.loginServerUrlFormLearnMore.isVisible = false
                views.loginServerUrlFormHomeServerUrlTil.hint =
                    getText(R.string.please_enter_organization_name)
                views.loginServerUrlFormNotice.text =
                    getString(R.string.login_server_url_form_common_notice)
            }
        }
//        val completions = state.knownCustomHomeServersUrls + if (BuildConfig.DEBUG) listOf("http://10.0.2.2:8080") else emptyList()
//        views.loginServerUrlFormHomeServerUrl.setAdapter(ArrayAdapter(
//                requireContext(),
//                R.layout.item_completion_homeserver,
//                completions
//        ))
//        views.loginServerUrlFormHomeServerUrlTil.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
//                .takeIf { completions.isNotEmpty() }
//                ?: TextInputLayout.END_ICON_NONE
    }

    private fun learnMore() {
        openUrlInChromeCustomTab(requireActivity(), null, EMS_LINK)
    }

    private fun clearHistory() {
        loginViewModel.handle(LoginAction.ClearHomeServerHistory)
    }

    override fun resetViewModel() {
        loginViewModel.handle(LoginAction.ResetHomeServerUrl)
    }

    @SuppressLint("SetTextI18n")
    private fun submit() {
        cleanupUi()

        // Static check of homeserver url, empty, malformed, etc.
        val serverUrl =
            views.loginServerUrlFormHomeServerUrl.text.toString().trim() // .ensureProtocol()

        when {
            serverUrl.isBlank() -> {
                //views.loginServerUrlFormHomeServerUrlTil.error =
                ToastUtil.showError(context, getString(R.string.login_error_invalid_home_server))
            }
            else -> {
//                views.loginServerUrlFormHomeServerUrl.setText(serverUrl, false /* to avoid completion dialog flicker*/)
                loginViewModel.handle(LoginAction.UpdateHomeServer(serverUrl))
            }
        }
    }

    private fun cleanupUi() {
        views.loginServerUrlFormSubmit.hideKeyboard()
        //views.loginServerUrlFormHomeServerUrlTil.error = null
    }

    override fun onError(throwable: Throwable) {
        //views.loginServerUrlFormHomeServerUrlTil.error =
            if (throwable is Failure.NetworkConnection &&
                throwable.ioException is UnknownHostException
            ) {
                // Invalid homeserver?
                ToastUtil.showError(context, getString(R.string.login_error_homeserver_not_found))
            } else {
                ToastUtil.showError(context, errorFormatter.toHumanReadable(throwable))
            }
    }

    override fun updateWithState(state: LoginViewState) {
        setupUi(state)

//        views.loginServerUrlFormClearHistory.isInvisible = state.knownCustomHomeServersUrls.isEmpty()
    }
}
