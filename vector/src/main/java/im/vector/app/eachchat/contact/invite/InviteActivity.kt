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

package im.vector.app.eachchat.contact.invite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.airbnb.mvrx.Mavericks
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.extensions.replaceFragment
import im.vector.app.core.extensions.toMvRxBundle
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityHomeBinding
import im.vector.app.databinding.ActivityInviteBinding
import im.vector.app.features.home.HomeActivity
import im.vector.app.features.home.HomeActivityArgs
import im.vector.app.features.home.HomeDetailFragment
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.home.room.list.RoomListParams

@AndroidEntryPoint
class InviteActivity: VectorBaseActivity<ActivityInviteBinding>() {
    override fun getBinding(): ActivityInviteBinding {
        return ActivityInviteBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = RoomListParams(RoomListDisplayMode.INVITE)
        replaceFragment(views.homeDetailFragmentContainer, InviteFragment::class.java, params)
    }

    companion object {
        fun start(context: Context,
        ) {
            val intent = Intent(context, InviteActivity::class.java)
            context.startActivity(intent)
        }
    }
}
