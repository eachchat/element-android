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
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import im.vector.app.R
import im.vector.app.core.extensions.isEmail
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentWidgetEmailBinding
import im.vector.app.databinding.FragmentWidgetEmailServerBinding
import im.vector.app.features.home.room.list.RoomListParams
import java.util.Locale

import javax.inject.Inject

class WidgetEmailServerFragment @Inject constructor(
) : VectorBaseFragment<FragmentWidgetEmailServerBinding>() {

    private val widgetEmailParams: WidgetEmailParams by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.acceptUserNameField.setText(widgetEmailParams.email)
        views.acceptPasswordField.setText(widgetEmailParams.password)
        views.outboundUserNameField.setText(widgetEmailParams.email)
        views.outboundPasswordField.setText(widgetEmailParams.password)
        views.emailField.setText(widgetEmailParams.email)

        views.widgetEmailTabLayout.addTab(views.widgetEmailTabLayout.newTab().setText(R.string.IMAP))
        views.widgetEmailTabLayout.addTab(views.widgetEmailTabLayout.newTab().setText(R.string.POP))

        views.acceptUserNameField.doAfterTextChanged {
            checkSubmitEnable()
        }
        views.acceptPasswordField.doAfterTextChanged {
            checkSubmitEnable()
        }
        views.acceptHostField.doAfterTextChanged {
            checkSubmitEnable()
        }
        views.acceptPortField.doAfterTextChanged {
            checkSubmitEnable()
        }

        views.loginSubmit.setOnClickListener {
            if (activity is WidgetEmailActivity) {
                val selectedMode = views.widgetEmailTabLayout.getTabAt(views.widgetEmailTabLayout.selectedTabPosition)?.text.toString().lowercase(Locale.ROOT)
                (activity as WidgetEmailActivity).submit("!mail setup " + selectedMode + ", " + views.acceptHostField.text +
                ":" + views.acceptPortField.text + ", " + views.acceptUserNameField.text + ", " + views.acceptPasswordField.text + ", mailbox"
                + ", true")
            }
        }

        views.backLayout.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    fun checkSubmitEnable() {
        views.loginSubmit.isEnabled = views.acceptUserNameField.text?.isEmail() == true
                && views.acceptHostField.text?.isNotBlank() == true
                && views.acceptPortField.text?.isNotBlank() == true
                && views.acceptPasswordField.text?.isNotBlank() == true
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWidgetEmailServerBinding {
        return FragmentWidgetEmailServerBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
