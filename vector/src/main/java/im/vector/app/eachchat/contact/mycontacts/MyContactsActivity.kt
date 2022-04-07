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

package im.vector.app.eachchat.contact.mycontacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.extensions.replaceFragment
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFragmentContainerBinding
import im.vector.app.eachchat.contact.invite.InviteActivity
import im.vector.app.eachchat.contact.invite.InviteFragment
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.home.room.list.RoomListParams
import im.vector.app.features.rageshake.ReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyContactsActivity: VectorBaseActivity<ActivityFragmentContainerBinding>() {
    override fun getBinding(): ActivityFragmentContainerBinding {
        return ActivityFragmentContainerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = RoomListParams(RoomListDisplayMode.INVITE)
        replaceFragment(views.fragmentContainer, MyContactsFragment::class.java, params)
    }


    // menu
    override fun getMenuRes() = R.menu.my_contact

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_contact_add_contact -> {
                bugReporter.openBugReportScreen(this, ReportType.SUGGESTION)
                return true
            }
            R.id.menu_contact_manage_contact      -> {
                navigator.openRoomsFiltering(this)
                return true
            }
            R.id.menu_my_contact_filter      -> {
                navigator.openRoomsFiltering(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(context: Context,
        ) {
            val intent = Intent(context, MyContactsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
