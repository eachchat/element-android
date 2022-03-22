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

package im.vector.app.yiqia.cache

import com.blankj.utilcode.util.SPUtils

object AppCache {
    private const val SP_NAME_CACHE_NOT_CLEAR = "cache_not_clear"
    private const val KEY_IS_SHOW_PRIVACY_POLICY = "key_is_privacy_policy_show"

    fun isShowPrivacyPolicy(): Boolean {
        return SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_SHOW_PRIVACY_POLICY, true)
    }

    fun setShowPrivacyPolicy(isShow: Boolean) {
        SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_SHOW_PRIVACY_POLICY, isShow)
    }
}
