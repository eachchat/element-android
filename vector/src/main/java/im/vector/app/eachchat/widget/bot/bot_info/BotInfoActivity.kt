/*
 * Copyright 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package im.vector.app.eachchat.widget.bot.bot_info

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.airbnb.mvrx.Mavericks
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.extensions.addFragment
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivitySimpleBinding
import im.vector.app.features.room.RequireActiveMembershipViewEvents

// 用来处理没有matrix信息的联系人和组织用户
@AndroidEntryPoint
class BotInfoActivity : VectorBaseActivity<ActivitySimpleBinding>() {

    companion object {
        fun start(context: Context, args: BotInfoArg) {
            val intent = Intent(context, BotInfoActivity::class.java).apply {
                putExtra(Mavericks.KEY_ARG, args)
            }
            context.startActivity(intent)
        }
    }

    // private val requireActiveMembershipViewModel: RequireActiveMembershipViewModel by viewModel()

    override fun getBinding(): ActivitySimpleBinding {
        return ActivitySimpleBinding.inflate(layoutInflater)
    }

    override fun initUiAndData() {
        if (isFirstCreation()) {
            val fragmentArgs: BotInfoArg = intent?.extras?.getParcelable(Mavericks.KEY_ARG) ?: return
            addFragment(views.simpleFragmentContainer, BotInfoFragment::class.java, fragmentArgs)
        }

//        requireActiveMembershipViewModel.observeViewEvents {
//            when (it) {
//                is RequireActiveMembershipViewEvents.RoomLeft -> handleRoomLeft(it)
//            }
//        }
    }

    private fun handleRoomLeft(roomLeft: RequireActiveMembershipViewEvents.RoomLeft) {
        if (roomLeft.leftMessage != null) {
            Toast.makeText(this, roomLeft.leftMessage, Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
