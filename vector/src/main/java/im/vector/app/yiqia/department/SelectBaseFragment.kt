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

package im.vector.app.yiqia.department

import androidx.viewbinding.ViewBinding
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.yiqia.department.data.IDisplayBean

abstract class SelectBaseFragment<VB : ViewBinding>: VectorBaseFragment<VB>() {
//    open fun getSelectHomeActivity():    SelectHomeActivity? {
//        return if (activity == null) {
//            null
//        } else activity as SelectHomeActivity?
//    }
//
//    open fun getSelectedIds(): List<String?>? {
//        return if (getSelectHomeActivity() == null) {
//            null
//        } else getSelectHomeActivity().getParam().getUserIds()
//    }
//
//    open fun getCurrentUsers(): List<IDisplayBean?>? {
//        return if (getSelectHomeActivity() == null) {
//            null
//        } else getSelectHomeActivity().getCurrentUsers()
//    }
//
//    open fun getParam(): SelectParam? {
//        return if (getSelectHomeActivity() == null) {
//            null
//        } else getSelectHomeActivity().getParam()
//    }

    //刷新子fragment
    abstract fun refresh()
}
