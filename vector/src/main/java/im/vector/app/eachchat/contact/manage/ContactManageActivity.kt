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

package im.vector.app.eachchat.contact.manage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.viewModel
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.extensions.replaceFragment
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFragmentContainerBinding
import im.vector.app.eachchat.contact.invite.InviteFragment
import im.vector.app.eachchat.contact.mycontacts.MyContactsActivity
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.home.room.list.RoomListParams
import im.vector.lib.multipicker.FilePicker

@AndroidEntryPoint
class ContactManageActivity: VectorBaseActivity<ActivityFragmentContainerBinding>() {

    override fun getBinding(): ActivityFragmentContainerBinding {
        return ActivityFragmentContainerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = RoomListParams(RoomListDisplayMode.INVITE)
        waitingView = views.waitingView.waitingView
        replaceFragment(views.fragmentContainer, ContactManageFragment::class.java, params)
    }

    companion object {
        fun start(context: Context,
        ) {
            val intent = Intent(context, ContactManageActivity::class.java)
            context.startActivity(intent)
        }
    }
}
