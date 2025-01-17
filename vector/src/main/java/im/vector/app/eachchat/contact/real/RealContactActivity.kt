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

package im.vector.app.eachchat.contact.real

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.extensions.replaceFragment
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFragmentContainerBinding
import im.vector.app.eachchat.contact.addcontact.ContactAddHomeActivity
import im.vector.app.eachchat.search.contactsearch.ContactsSearchActivity
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.home.room.list.RoomListParams
import im.vector.app.features.rageshake.ReportType

@AndroidEntryPoint
class RealContactActivity: VectorBaseActivity<ActivityFragmentContainerBinding>() {
    override fun getBinding(): ActivityFragmentContainerBinding {
        return ActivityFragmentContainerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = RoomListParams(RoomListDisplayMode.INVITE)
        replaceFragment(views.fragmentContainer, RealContactsFragment::class.java, params)
    }

    // menu
    override fun getMenuRes() = R.menu.real_contact

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_real_add_contact          -> {
                ContactAddHomeActivity.start(this)
                return true
            }
            R.id.menu_home_filter              -> {
                ContactsSearchActivity.start(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(context: Context,
        ) {
            val intent = Intent(context, RealContactActivity::class.java)
            context.startActivity(intent)
        }
    }
}
