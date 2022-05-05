/*
 * Copyright 2019 New Vector Ltd
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
 */
package im.vector.app.core.epoxy.profiles

import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.app.R
import im.vector.app.core.epoxy.VectorEpoxyHolder
import im.vector.app.core.epoxy.VectorEpoxyModel
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.utils.ScreenUtils

// 用来显示聊天工具、重要日期之类
@EpoxyModelClass(layout = R.layout.item_multi_user_profile_info)
abstract class UserProfileMultiInfoTitleItem : VectorEpoxyModel<UserProfileMultiInfoTitleItem.Holder>() {
    @EpoxyAttribute
    var key: String? = ""

    @EpoxyAttribute
    var values: ArrayList<String>? = null

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.title.text = key
    }

    class Holder : VectorEpoxyHolder() {
        val title by bind<TextView>(R.id.tv_item_user_info_title)
    }
}