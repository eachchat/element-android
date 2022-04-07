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

package im.vector.app.eachchat.contact.addcontact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityContactAddHomeBinding

@AndroidEntryPoint
class ContactAddHomeActivity : VectorBaseActivity<ActivityContactAddHomeBinding>() {
    override fun getBinding(): ActivityContactAddHomeBinding {
        return ActivityContactAddHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initClickListener()
    }

    private fun initClickListener() {
        views.backLayout.onClick {
            onBackPressed()
        }
        views.tvNewContact.setOnClickListener {
            ContactEditAddActivity.start(this)
        }
        views.tvSearch.setOnClickListener {

        }
    }

    companion object {
        fun start(
                context: Context,
        ) {
            val intent = Intent(context, ContactAddHomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}
