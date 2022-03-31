package im.vector.app.yiqia.contact.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Created by chengww on 1/15/21
 * @author chengww
 */
@Entity(tableName = "contacts_rooms")
@Parcelize
class ContactsRoom(
        @PrimaryKey
        val id: String,
        val roomId: String,
        val del: Int?,
        val updateTimestamp: String?
) : Parcelable {
    @IgnoredOnParcel
    val roomDel: Boolean
        get() = del != null && del > 0
}
