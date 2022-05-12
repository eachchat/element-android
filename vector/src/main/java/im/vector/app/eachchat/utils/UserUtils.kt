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

package im.vector.app.eachchat.utils

import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.emptyTake
import im.vector.app.eachchat.contact.data.matrixHost
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.DepartmentStoreHelper

/**
 * 最常联系的职位: 本域:职位; 外域:公司+职位
 */
fun getCloseContactTitle(matrixId: String): String? {
    val contact = if (AppCache.getIsOpenContact()) ContactDaoHelper.getInstance()
            .getContactByMatrixId(matrixId) else null
    val user = if (AppCache.getIsOpenOrg()) AppDatabase.getInstance(BaseModule.getContext()).userDao().getBriefUserByMatrixId(
            matrixId,
    ) else null
    val tempTitle = contact?.userTitle.emptyTake(user?.userTitle)
    var company = contact?.company
    if (company.isNullOrEmpty()) {
        var departmentId = user?.departmentId
        while (!departmentId.isNullOrEmpty()) {
            val department =
                    runCatching { DepartmentStoreHelper.getDepartmentById(departmentId!!) }.getOrNull()
            departmentId = department?.parentId
            if (department == null || departmentId.isNullOrEmpty()) {
                break
            }
            company = department.displayName.orEmpty()
        }
    }
    return when {
        matrixHostEquals(matrixId) -> tempTitle
        else -> {
            when {
                !company.isNullOrEmpty() && !tempTitle.isNullOrEmpty() -> "$company $tempTitle"
                company.isNullOrEmpty() && !tempTitle.isNullOrEmpty() -> tempTitle
                !company.isNullOrEmpty() && tempTitle.isNullOrEmpty() -> company
                else -> ""
            }
        }
    }
}

fun matrixHostEquals(compareMatrixId: String?): Boolean {
    if (compareMatrixId.isNullOrEmpty()) return false
    val myUserId = BaseModule.getSession()?.myUserId
    if (myUserId.isNullOrEmpty()) return false
    return myUserId.matrixHost().equals(compareMatrixId.matrixHost(), true)
}
