package im.vector.app.eachchat.contact.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.vector.app.eachchat.contact.data.RoomInviteDisplay

/**
 * Created by chengww on 1/29/21
 * @author chengww
 */
@Dao
interface RoomInviteDao {
    @Query("SELECT * FROM contacts_rooms_invite ORDER BY `update` DESC")
    fun getRoomsLive(): LiveData<List<RoomInviteDisplay>>

    @Query("SELECT * FROM contacts_rooms_invite ORDER BY `update` DESC")
    fun getRooms(): List<RoomInviteDisplay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg room: RoomInviteDisplay)

    @Query("DELETE FROM contacts_rooms_invite where id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM contacts_rooms_invite")
    fun deleteAll()
}
