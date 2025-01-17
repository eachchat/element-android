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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.extensions.replaceFragment
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFragmentContainerBinding
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.home.room.list.RoomListParams

@AndroidEntryPoint
class WidgetEmailActivity: VectorBaseActivity<ActivityFragmentContainerBinding>() {
    override fun getBinding(): ActivityFragmentContainerBinding {
        return ActivityFragmentContainerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val params = RoomListParams(RoomListDisplayMode.INVITE)
        replaceFragment(views.fragmentContainer, WidgetEmailFragment::class.java, params)
    }

    fun submit() {
        val resultIntent = Intent().apply{ putExtra(KEY_WIDGET_EMAIL, "xxxxxxx") }
        setResult(Activity.RESULT_OK, resultIntent)
    }

    override fun onDestroy() {
        submit()
        super.onDestroy()
    }




    companion object {
        fun start(context: Context,
        ) {
            val intent = Intent(context, WidgetEmailActivity::class.java)
            context.startActivity(intent)
        }

        const val KEY_WIDGET_EMAIL = "KEY_WIDGET_EMAIL"
    }
}
