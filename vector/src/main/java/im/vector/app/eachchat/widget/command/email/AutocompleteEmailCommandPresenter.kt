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

package im.vector.app.eachchat.widget.command.email

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.features.autocomplete.AutocompleteClickListener
import im.vector.app.features.autocomplete.RecyclerViewPresenter
import im.vector.app.features.settings.VectorPreferences

class AutocompleteEmailCommandPresenter @AssistedInject constructor(
        @Assisted val isInThreadTimeline: Boolean,
        context: Context,
        private val controller: AutocompleteEmailCommandController,
        private val vectorPreferences: VectorPreferences) :
        RecyclerViewPresenter<EmailCommand>(context), AutocompleteClickListener<EmailCommand> {

    @AssistedFactory
    interface Factory {
        fun create(isFromThreadTimeline: Boolean): AutocompleteEmailCommandPresenter
    }

    init {
        controller.listener = this
    }

    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        return controller.adapter
    }

    override fun onItemClick(t: EmailCommand) {
        dispatchClick(t)
    }

    override fun onQuery(query: CharSequence?) {
        val data = EmailCommand.values()
                .filter {
                    !it.isDevCommand || vectorPreferences.developerMode()
                }
                .filter {
                    if (vectorPreferences.areThreadMessagesEnabled() && isInThreadTimeline) {
                        it.isThreadCommand
                    } else {
                        true
                    }
                }
                .filter {
                    if (query.isNullOrEmpty()) {
                        true
                    } else {
                        it.command.contains(query)
                    }
                }
                data.forEach {
                    it.keyword = query.toString()
                }
        controller.setData(data)
    }

    fun clear() {
        controller.listener = null
    }
}
