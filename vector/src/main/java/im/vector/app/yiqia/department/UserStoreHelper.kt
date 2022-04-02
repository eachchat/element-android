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

import im.vector.app.eachchat.BaseModule
import im.vector.app.yiqia.contact.api.bean.Department
import im.vector.app.yiqia.contact.data.User
import im.vector.app.yiqia.database.AppDatabase
import im.vector.app.yiqia.department.data.DepartmentBean
import im.vector.app.yiqia.department.data.DepartmentUserBean
import im.vector.app.yiqia.department.data.IDisplayBean

object UserStoreHelper {
    @JvmStatic
    fun getSelectUsersByDepartmentId(departmentId: String?): List<IDisplayBean> {
        return AppDatabase.getInstance(BaseModule.getContext()).UserDao().getSelectUsersByDepartmentId(departmentId).toIDisplayBeanList()
    }

    fun List<User>?.toIDisplayBeanList(): List<IDisplayBean> {
        val iDisplayBeanList: MutableList<IDisplayBean> = ArrayList()
        this?.forEach {
            iDisplayBeanList.add(DepartmentUserBean(it))
        }
        return iDisplayBeanList
    }
}
