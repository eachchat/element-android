package im.vector.app.eachchat.department

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.CONTACTS_DEPARTMENT_ID
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.contact.data.getNamePy
import im.vector.app.eachchat.contact.data.resolveMxc
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.data.DepartmentUserBean
import org.matrix.android.sdk.api.util.Optional
import java.util.*

/**
 * Created by chengww on 2020/11/10
 * @author chengww
 */
class ContactAvatarObserver {
    private val avatarList: MutableList<LiveData<org.matrix.android.sdk.api.session.user.model.User>> = ArrayList()
    private val session = BaseModule.getSession()
    private val local = AppDatabase.getInstance(BaseModule.getContext()).contactMatrixUserDao()

    fun clearObserver(owner: LifecycleOwner) {
        for (avatar in avatarList) {
            avatar.removeObservers(owner)
        }
        avatarList.clear()
    }

    fun observer(owner: LifecycleOwner, mAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>, results: List<Any>?) {
        observer(owner, mAdapter, results, false)
    }

    fun observer(owner: LifecycleOwner, mAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>, results: List<Any>?, withDisplayName: Boolean) {
        if (results == null || session == null) return
        for (i in results.indices) {
            val user = results[i]
            val matrixId = when (user) {
                is DepartmentUserBean -> user.matrixId
//                is SearchContactsBean  -> user.contact.matrixId
                is ContactsDisplayBean -> user.matrixId
                else                   -> null
            }
            if (matrixId.isNullOrEmpty()) continue
            val userLive = userLive(matrixId) ?: continue
            userLive.observe(owner) { _user ->
                val avatar = _user.avatarUrl.resolveMxc()
                when (user) {
                    is DepartmentUserBean  -> {
                        user.profileUrl = avatar
                        user.avatarOUrl = avatar
                        user.avatarTUrl = avatar
                        if (withDisplayName && user.remarkName.isNullOrEmpty() && TextUtils.equals(user.departmentId, CONTACTS_DEPARTMENT_ID)) {
                            user.displayName = _user.displayName
                            user.displayNamePy = getNamePy(user.displayName)
                        }
                    }
//                    is SearchContactsBean -> {
//                        user.contact.avatar = avatar
//                        if (withDisplayName && user.contact.remarkName.isNullOrEmpty()) {
//                            user.contact.displayName = _user.getBestName()
//                        }
//                    }
                    is ContactsDisplayBean -> {
                        user.avatar = avatar
                        if (withDisplayName && user.remarkName.isNullOrEmpty()) {
                            user.displayName = _user.displayName
                        }
                    }
                }

                if (mAdapter.itemCount == results.size + 1) mAdapter.notifyItemChanged(i + 1, user)
                else mAdapter.notifyItemChanged(i, user)
            }
            avatarList.add(userLive)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun observerContacts(owner: LifecycleOwner, mAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>, results: List<User>?) {
        if (results == null || session == null) return
        for (i in results.indices) {
            val user: User = results[i]
            val matrixId = user.matrixId
            if (TextUtils.isEmpty(matrixId)) continue
            val userLive = matrixId?.let { userLive(it) } ?: continue
            userLive.observe(owner) { _user ->
                user.avatarOUrl = _user.avatarUrl.resolveMxc()
                user.avatarTUrl = _user.avatarUrl.resolveMxc()
                mAdapter.notifyDataSetChanged()
            }
            avatarList.add(userLive)
        }
    }

    fun avatarLive(matrixId: String): LiveData<String>? {
        if (session == null) return null
        val contact = local.getContactLive(matrixId)
        val userLive: LiveData<Optional<org.matrix.android.sdk.api.session.user.model.User>> = session.getUserLive(matrixId)
        return MediatorLiveData<String>().run {
            addSource(userLive) { optional ->
                optional.getOrNull()?.let {
                    if (!it.avatarUrl.isNullOrEmpty() || it.userId != session.myUserId) {
                        this.postValue(it.avatarUrl.resolveMxc())
                        removeSource(contact)
                    }
                }
            }

            addSource(contact) {
                if (it != null) {
                    postValue(it.avatar.resolveMxc())
                }
            }
            this
        }
    }

    fun userLive(matrixId: String): LiveData<org.matrix.android.sdk.api.session.user.model.User>? {
        if (session == null) return null
        val contact = local.getContactLive(matrixId)
        val userLive: LiveData<Optional<org.matrix.android.sdk.api.session.user.model.User>> = session.getUserLive(matrixId)
        return MediatorLiveData<org.matrix.android.sdk.api.session.user.model.User>().run {
            addSource(contact) {
                if (it != null && userLive.value?.getOrNull()?.avatarUrl.isNullOrEmpty()) {
                    postValue(org.matrix.android.sdk.api.session.user.model.User(matrixId, it.displayName, it.avatar))
                }
            }

            addSource(userLive) { optional ->
                optional.getOrNull()?.let {
                    if (!it.avatarUrl.isNullOrEmpty() || it.userId != session.myUserId) {
                        this.postValue(it)
                        removeSource(contact)
                    }
                }
            }

            this
        }
    }

}
