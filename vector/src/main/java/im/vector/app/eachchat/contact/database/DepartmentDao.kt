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

package im.vector.app.eachchat.contact.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.vector.app.eachchat.contact.api.bean.Department
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM DepartmentStoreHelper WHERE parentId is null and del != 1")
    fun getRootDepartments(): List<Department>?

    @Query("SELECT * FROM DepartmentStoreHelper WHERE parentId is null and del != 1")
    fun getRootDepartmentsLiveData(): LiveData<List<Department?>?>?

    @Query("SELECT * FROM DepartmentStoreHelper WHERE parentId = :parentId and del != 1 ORDER BY showOrder")
    fun getDepartmentsByParentId(parentId: String): List<Department>?

    @Query("SELECT * FROM DepartmentStoreHelper WHERE id = :id")
    fun getDepartmentById(id: String): Department?

    @Query("SELECT * FROM DepartmentStoreHelper")
    fun getAllDepartments(): List<Department>?

    @Query("SELECT * FROM DepartmentStoreHelper WHERE displayName LIKE '%'||:keyword||'%' AND del != 1 LIMIT :count")
    fun search(keyword: String, count: Int): List<Department>?

    @Query("SELECT * FROM DepartmentStoreHelper WHERE displayName LIKE '%'||:keyword||'%' AND del != 1  AND parentId = :departmentId LIMIT :count")
    fun search(keyword: String, count: Int, departmentId: String): List<Department>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun bulkInsert(departments: List<Department?>?)
}
