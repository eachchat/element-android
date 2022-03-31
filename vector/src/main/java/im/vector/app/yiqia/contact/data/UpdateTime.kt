package im.vector.app.yiqia.contact.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by chengww on 1/16/21
 * @author chengww
 */
@Entity(tableName = "update_time")
@Parcelize
data class UpdateTime(@PrimaryKey val id: Int,
                      val time: Long) : Parcelable
