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

import com.airbnb.epoxy.TypedEpoxyController
import im.vector.app.core.resources.StringProvider
import im.vector.app.features.autocomplete.AutocompleteClickListener
import javax.inject.Inject

class AutocompleteEmailCommandController @Inject constructor(private val stringProvider: StringProvider) : TypedEpoxyController<List<EmailCommand>>() {

    var listener: AutocompleteClickListener<EmailCommand>? = null
    var mKeyword: String? = null

    override fun buildModels(data: List<EmailCommand>?) {
        if (data.isNullOrEmpty()) {
            return
        }
        val host = this
        data.forEach { command ->
            autocompleteEmailCommandItem {
                id(command.command)
                name(command.command)
                parameters(command.parameters)
                description(host.stringProvider.getString(command.description))
                clickListener { host.listener?.onItemClick(command) }
                keyword(command.keyword)
            }
        }
    }
}
