package im.vector.app.eachchat.contact.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.vector.app.eachchat.contact.data.UpdateTime
import im.vector.app.eachchat.contact.data.UpdateTimeIDs

/**
 * Created by chengww on 1/16/21
 * @author chengww
 */
@Dao
interface UpdateTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contactUpdate: UpdateTime)

    @Query("SELECT time FROM update_time WHERE id=:id")
    fun getTimeWithId(id: Int): Long

    fun insertTime(id: Int, time: Long) = insert(UpdateTime(id, time))

    fun getContactsTime() = getTimeWithId(UpdateTimeIDs.ContactsRoomsTimeV2)

    fun getContactsRoomsTime() = getTimeWithId(UpdateTimeIDs.ContactsRoomsTime)

    fun updateContactsTime(time: Long) {
        insertTime(UpdateTimeIDs.ContactsTime, time)
    }

    fun updateContactsTimeV2(time: Long) {
        insertTime(UpdateTimeIDs.ContactsRoomsTimeV2, time)
    }

    fun updateContactsRoomsTime(time: Long) {
        insertTime(UpdateTimeIDs.ContactsRoomsTime, time)
    }

    @Query("DELETE FROM update_time")
    fun deleteAll()
}
