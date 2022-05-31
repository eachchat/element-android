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

package im.vector.app.eachchat.widget.bot.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BotDao {
    @Query("SELECT * FROM widget_bot")
    fun getBots(): List<Bot>?

    @Query("SELECT * FROM widget_bot where appMatrixId = :matrixId")
    fun getBot(matrixId: String): Bot?

    @Query("SELECT * FROM widget_bot")
    fun getBotsLive(): LiveData<List<Bot>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun bulkInsert(bots: List<Bot?>?)
}
