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

package im.vector.app.eachchat.department

import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.database.AppDatabase

fun String.getHomeSever(): String {
    return this.substring(this.indexOf(":"))
}

tailrec fun getCompany(company: String?, departmentId: String?): String? {
    if (departmentId == null) return company
    val department = AppDatabase.getInstance(BaseModule.getContext()).departmentDao().getDepartmentById(departmentId)
    if (department == null || department.departmentType == "root") {
        return company
    }
    return getCompany(department.displayName, department.parentId)
}
