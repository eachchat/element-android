package im.vector.app.yiqia.contact.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Created by chengww on 1/29/21
 * @author chengww
 */
@Entity(tableName = "contacts_rooms_invite")
class RoomInviteDisplay(
        var roomId: String = "",
        var title: String = "",
        var avatarUrl: String? = null,
        var isEncrypted: Boolean = false,
        var isDirect: Boolean = false,
        var inviterId: String? = null,
        var accepted: Boolean = false,
        var update: Long = 0,
        var canonicalAlias: String? = null,
        var topic: String? = null,
        @Ignore
        var fromDatabase: Boolean = true,
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        @Ignore
        var inviterName: String? = null
)
