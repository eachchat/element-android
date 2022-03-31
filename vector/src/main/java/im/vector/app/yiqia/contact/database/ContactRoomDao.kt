package im.vector.app.yiqia.contact.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.vector.app.yiqia.contact.data.ContactsRoom

/**
 * Created by chengww on 1/15/21
 * @author chengww
 */
@Dao
interface ContactRoomDao {

    @Query("SELECT roomId FROM contacts_rooms ORDER BY updateTimestamp")
    fun getContactRoomsLive(): LiveData<List<String>>

    @Query("SELECT roomId FROM contacts_rooms ORDER BY updateTimestamp")
    fun getContactRooms(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRooms(vararg room: ContactsRoom)

    @Query("DELETE FROM contacts_rooms where roomId = :roomId")
    fun delete(roomId: String)

    @Query("SELECT * FROM contacts_rooms WHERE roomId = :roomId")
    fun getContactRoomByRoomId(roomId: String): LiveData<ContactsRoom?>

    @Query("DELETE FROM contacts_rooms")
    fun deleteAll()
}
