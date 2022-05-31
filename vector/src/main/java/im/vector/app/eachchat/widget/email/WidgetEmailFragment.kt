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

package im.vector.app.eachchat.widget.email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.extensions.isEmail
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentWidgetEmailBinding
import im.vector.app.databinding.FragmentWidgetEmailServerBinding

import javax.inject.Inject

class WidgetEmailFragment @Inject constructor(
) : VectorBaseFragment<FragmentWidgetEmailBinding>() {


    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWidgetEmailBinding {
        return FragmentWidgetEmailBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.loginField.doAfterTextChanged {
            views.loginSubmit.isEnabled = views.loginField.text?.isEmail() == true && views.passwordField.text?.isNotBlank() == true
        }
        views.passwordField.doAfterTextChanged {
            views.loginSubmit.isEnabled = views.loginField.text?.isEmail() == true && views.passwordField.text?.isNotBlank() == true
        }

        views.loginSubmit.setOnClickListener {
            if (activity is WidgetEmailActivity) {
                (activity as WidgetEmailActivity).openWidgetEmailFragment(views.loginField.text.toString(), views.passwordField.text.toString())
            }
        }

        views.backLayout.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
