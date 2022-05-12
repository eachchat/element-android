package ai.workly.eachchat.android.contact.relationship

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.platform.EmptyViewEvents
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.base.EmptyAction
import im.vector.app.eachchat.base.EmptyViewState
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.ContactAvatarObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by chengww on 2020/11/12
 * @author chengww
 */
class ReportingRelationshipViewModel@AssistedInject constructor(
        @Assisted private val initialState: EmptyViewState
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<ReportingRelationshipViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): ReportingRelationshipViewModel
    }

    companion object : MavericksViewModelFactory<ReportingRelationshipViewModel, EmptyViewState> by hiltMavericksViewModelFactory()

    val userDao = AppDatabase.getInstance(BaseModule.getContext()).userDao()

    val managers = MutableLiveData<List<User>?>()

    fun getManagers(departmentUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getBriefUserById(departmentUserId)
            managers.postValue(getManagers(user))
        }
    }

    private val avatarObserver = ContactAvatarObserver()
    val managerAvatars = MediatorLiveData<Map<String, LiveData<String>?>>().also { _managerAvatars ->
        _managerAvatars.addSource(managers) { _managers ->
            val result = HashMap<String, LiveData<String>?>()
            _managers?.forEach {
                it.matrixId?.let { matrixId ->
                    result[matrixId] = avatarObserver.avatarLive(matrixId)
                }
            }
            _managerAvatars.value = result
        }
    }

    fun getManagers(user: User?): List<User> {
        var mUser = user
        val managers: ArrayList<User> = ArrayList()
        if (mUser != null) {
            managers.add(mUser)
        }
        while (mUser != null && !TextUtils.isEmpty(mUser.managerId)) {
            val currentUserId: String? = mUser.id
            mUser = userDao.getBriefUserById(mUser.managerId)
            if (mUser != null) {
                if (TextUtils.equals(mUser.id, currentUserId)) break
                managers.add(mUser)
            }
        }
        return managers.reversed()
    }

    override fun handle(action: EmptyAction) {
        TODO("Not yet implemented")
    }
}
