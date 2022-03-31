package im.vector.app.yiqia.contact.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by chengww on 1/26/21
 * @author chengww
 */
@Entity(tableName = "contacts_matrix_user")
@Parcelize
class ContactsMatrixUser(
        @PrimaryKey
        val matrixId: String,
        val avatar: String,
        val displayName: String,
        val lastUpdate: Long
) : Parcelable
