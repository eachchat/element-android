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

package im.vector.app.eachchat.search.contactsearch.data

import android.content.Intent
import im.vector.app.eachchat.department.data.IDisplayBean

data class SearchData(val mainTitle: String,
                      val minor: String?,
                      val mAvatar: String?,
                      val mType: Int,
                      val mId: String?,
                      var hasMore: Boolean,
                      val mCount: Int = 0,
                      val targetId: String? = null) : IDisplayBean{
    var time: Long? = 0
    var isDirect: Boolean = true
    var isEncrypted : Boolean = false

    override fun getItemType(): Int {
        return type
    }

    override fun getAvatar(): String? {
        return mAvatar
    }

    override fun getMainContent(): String {
        return mainTitle
    }

    override fun getMinorContent(): String? {
        return minor
    }

    override fun getId(): String? {
        return mId
    }

    override fun getCount(): Int {
        return mCount
    }

    override fun getType(): Int {
        return mType
    }

    override fun getExtra(): Intent? {
        return null
    }
}
