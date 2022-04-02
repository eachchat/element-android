package im.vector.app.eachchat.contact.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.ContactSyncUtils
import im.vector.app.eachchat.contact.data.ContactsMatrixUser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by chengww on 1/26/21
 * @author chengww
 */
@Dao
interface ContactMatrixUserDao {
    @Query("DELETE FROM contacts_matrix_user")
    fun deleteAll()

    @Query("SELECT * FROM contacts_matrix_user WHERE matrixId = :matrixId")
    fun getContactByMatrixId(matrixId: String): LiveData<ContactsMatrixUser>

    @Query("SELECT * FROM contacts_matrix_user WHERE matrixId = :matrixId")
    fun getContact(matrixId: String): ContactsMatrixUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg contact: ContactsMatrixUser)

    @OptIn(DelicateCoroutinesApi::class)
    fun getContactLive(matrixId: String): LiveData<ContactsMatrixUser> {
        val user = BaseModule.getSession()?.getUser(matrixId)
        user?.avatarUrl?.let {
            if (it.isNotEmpty()) {
                return MutableLiveData(ContactsMatrixUser(matrixId, it, user.displayName ?: "", 0))
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            val contact = getContact(matrixId)
            if (contact == null || (contact.avatar.isEmpty() && contact.lastUpdate + 5 * 60000 < System.currentTimeMillis())) {
                ContactSyncUtils.getInstance().syncContactMatrixUser(matrixId)
            }
        }
        return getContactByMatrixId(matrixId)
    }
}
