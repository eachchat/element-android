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

package im.vector.app.eachchat.complain

import com.airbnb.epoxy.TypedEpoxyController
import im.vector.app.R
import im.vector.app.core.epoxy.checkBoxItem
import im.vector.app.core.epoxy.dividerItem
import im.vector.app.core.resources.StringProvider
import javax.inject.Inject

class RoomComplainController @Inject constructor(
        private val stringProvider: StringProvider
) : TypedEpoxyController<RoomComplainViewState>() {

    var selectedComplainList: ArrayList<String> = ArrayList()
    var selectComplainCallback: (() -> Unit)? = null

    override fun buildModels(data: RoomComplainViewState?) {
        buildComplainTitleItem(stringProvider.getString(R.string.select_reason_to_complain))

        data?.complainList?.forEach {
            buildComplainItem(
                    it, selectedComplainList, selectComplainCallback
            )
        }
    }

    private fun buildComplainItem(title: String, selectedComplainList: ArrayList<String>, callback: (()->Unit)?): String {
        val checkBoxId = "checkbox_$title"
        checkBoxItem {
            id(checkBoxId)
            title(title)
            checkChangeListener { _, _, _, isChecked, _ ->
                if (isChecked) {
                    selectedComplainList.add(title)
                } else {
                    selectedComplainList.remove(title)
                }
                callback?.invoke()
            }
        }
        dividerItem {
            id("divider_$title")
        }
        return checkBoxId
    }
}
