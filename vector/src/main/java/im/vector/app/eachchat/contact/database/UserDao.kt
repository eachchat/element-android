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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.vector.app.eachchat.contact.data.User

@Dao
interface UserDao {
    @Query("SELECT * FROM UserInfoStore WHERE id = :id AND del!=1")
    fun getBriefUserByMatrixId(id: String?): User?

    @Query("SELECT * FROM UserInfoStore WHERE departmentId = :departmentId and del != 1")
    fun getSelectUsersByDepartmentId(departmentId: String?): List<User>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun bulkInsert(users: List<User?>?)
}
