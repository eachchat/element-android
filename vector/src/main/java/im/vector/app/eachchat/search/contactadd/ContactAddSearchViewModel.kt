package im.vector.app.eachchat.search.contactadd

import android.text.TextUtils
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
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
import im.vector.app.eachchat.bean.Response
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.resolveMxc
import im.vector.app.eachchat.contact.data.toContact
import im.vector.app.eachchat.contact.data.toContactList
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.contact.event.UpdateMyContactsEvent
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.DepartmentStoreHelper
import im.vector.app.eachchat.search.contactadd.adapter.ContactAddSearchAdapter
import im.vector.app.eachchat.utils.AppCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.matrix.android.sdk.api.extensions.orFalse
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.profile.ProfileService
import org.matrix.android.sdk.api.util.Cancelable

/**
 * Created by chengww on 2020/10/26
 * @author chengww
 */
class ContactAddSearchViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
        private val session: Session
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<ContactAddSearchViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): ContactAddSearchViewModel
    }

    companion object : MavericksViewModelFactory<ContactAddSearchViewModel, EmptyViewState> by hiltMavericksViewModelFactory() {
        // private const val PAGE_LIMIT = 100

        override fun initialState(viewModelContext: ViewModelContext): EmptyViewState {
            return EmptyViewState(
            )
        }
    }

    val searchUserOnlineLiveData = MutableLiveData<ContactsDisplayBean?>()
    val searching = MutableLiveData(false)
    val userList = ObservableArrayList<ContactsDisplayBean>()
    val userListLiveData = MutableLiveData<List<ContactsDisplayBean>>()
    val keyword = MutableLiveData<CharSequence?>()
    val local = ContactDaoHelper.getInstance()
    private val avatarLocal = AppDatabase.getInstance(BaseModule.getContext()).contactMatrixUserDao()
    val modeNormal = MediatorLiveData<Boolean>().also {
        it.addSource(keyword) { keyword ->
            it.postValue(keyword.isNullOrEmpty())
        }
    }
    private val _modeSearch = MediatorLiveData<Boolean>().also {
        it.addSource(modeNormal) { normalMode ->
            it.postValue(!normalMode && !searching.value.orFalse())
        }

        it.addSource(searching) { searching ->
            it.postValue(!modeNormal.value.orFalse() && !searching)
        }
    }
    val showContactList = MediatorLiveData<Boolean>().also {
        it.addSource(_modeSearch) { _modeSearch ->
            it.postValue(_modeSearch && userList.size > 0)
        }
    }
    val showEmptyView = MediatorLiveData<Boolean>().also {
        it.addSource(_modeSearch) { _modeSearch ->
//            val keywordValid =
//                    isEmail(keyword.value?.trim()) || isTel(keyword.value?.trim()) || MatrixPatterns.isUserId(
//                            keyword.value.toString().trim()
//                    )
            it.postValue(_modeSearch && userList.size < 1 ) // && !keywordValid)
        }
    }
    val showOnlineSearchLayout = MediatorLiveData<Boolean>().also {
        it.addSource(_modeSearch) { _modeSearch ->
//            val keywordValid =
//                    isEmail(keyword.value?.trim()) || isTel(keyword.value?.trim()) || MatrixPatterns.isUserId(
//                            keyword.value.toString().trim()
//                    )
            it.postValue(_modeSearch && userList.size < 1 )// && keywordValid)
        }
    }

    //val addResponse = MutableLiveData<Response<ContactsDisplayBean?, Any?>>()
    val addResponse = MutableLiveData<Response<ContactsDisplayBeanV2?, Any?>>()

    private var job: Job? = null
    private var filterJob: Job? = null
    private var lastTask: Cancelable? = null

    fun onKeywordChanged(keyword: CharSequence?) {
        if (TextUtils.isEmpty(keyword)) {
            searching.postValue(false)
            lastTask?.cancel()
            filterJob?.cancel()
            job?.cancel()
            return
        }
        var internalKeyword = keyword?.toString().orEmpty()
        internalKeyword = internalKeyword.trim()
        searching.postValue(true)
        lastTask?.cancel()
        filterJob?.cancel()
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.Main) {
            delay(600)

            runCatching {
                val result = ArrayList<ContactsDisplayBean>()
                withContext(Dispatchers.IO) {

//                    if (AppCache.getIsOpenOrg()) {
                    AppDatabase.getInstance(BaseModule.getContext()).UserDao().search(internalKeyword)?.filter {
                        it.matrixId?.contains(internalKeyword)
                                .orFalse() || it.displayName?.contains(internalKeyword).orFalse()
                    }?.map { user ->
                        user.toContact(
                                user.departmentId?.let { getUserCompany(it) },
                                user.matrixId?.let { local.getContactByMatrixId(it) } != null
                        )
                                .also { contact ->
                                    contact.subTitle = user.matrixId
                                    contactHighlight(contact, internalKeyword)
                                    contact.avatar = session
                                            .getUser(contact.matrixId)?.avatarUrl?.takeIf { it.isNotEmpty() }
                                            ?: avatarLocal.getContact(contact.matrixId)?.avatar
                                }
                    }?.let {
                        result.addAll(it)
                    }
//                    }

                    session.searchUsersDirectory(internalKeyword, 50, emptySet())
                            .toContactList(local) {
                                it.subTitle = it.matrixId
                                contactHighlight(it, internalKeyword)
                            }?.let {
                                result.addAll(it)
                            }

                    val finalResult = result.distinctBy {
                        it.matrixId
                    }

                    withContext(Dispatchers.Main) {
                        userList.clear()
                        userList.addAll(finalResult)
                        userListLiveData.postValue(userList)
                        searching.postValue(false)
                    }
                }
            }.exceptionOrNull()?.let {
                searching.postValue(false)
                error.postValue(it)
            }
        }
    }

    private fun contactHighlight(it: ContactsDisplayBean, internalKeyword: String) {
        internalKeyword.let { key ->
            it.mainTitle = it.mainTitle?.replace(key, "<font color='#00B368'>$key</font>")
            it.subTitle = it.subTitle?.replace(key, "<font color='#00B368'>$key</font>")
        }
    }

    fun addContacts(position: Int, contact: ContactsDisplayBean, adapter: ContactAddSearchAdapter) {
        loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val contactV2 = contact.toContactsDisplayBeanV2()
                val response = ContactServiceV2.getInstance().add(contactV2)
                if (!response.obj?.id.isNullOrEmpty()) {
                    contact.contactAdded = true
                    contact.contactId = response.obj?.id.orEmpty()
                    userList[position] = contact
                    viewModelScope.launch(Dispatchers.Main) {
                        adapter.setData(position, contact)
                        adapter.notifyItemChanged(position)
                    }
                    withContext(Dispatchers.IO) {
                        local.insertContacts(contact)
                        EventBus.getDefault().post(UpdateMyContactsEvent)
                        loading.postValue(false)
                    }
                } else {
                    loading.postValue(false)
                }
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                loading.postValue(false)
            }
        }
    }

    private fun getUserCompany(departmentId: String): String {
        var departmentIdCopy: String? = departmentId
        // Get departments of the user
        var company = ""
        while (!departmentIdCopy.isNullOrEmpty()) {
            val department =
                    runCatching { DepartmentStoreHelper.getDepartmentById(departmentIdCopy!!) }.getOrNull()
            departmentIdCopy = department?.parentId
            // current department is the org level, break the cycle
            if (department == null || departmentIdCopy.isNullOrEmpty()) {
                return company
            }
            company = department.displayName.orEmpty()
        }
        return company
    }

    fun searchMatrixUser(matrixId: String?) {
        if (matrixId.isNullOrBlank()) return
        loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val data = session.getProfile(matrixId)
                val displayName = data[ProfileService.DISPLAY_NAME_KEY] as? String?
                val avatar = data[ProfileService.AVATAR_URL_KEY] as? String?
                val user = ContactsDisplayBean()
                user.displayName = displayName
                user.avatarUrl = avatar.resolveMxc()
                user.avatar = avatar.resolveMxc()
                user.matrixId = matrixId
                user.subTitle = user.matrixId
                val contact = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2()
                        .getContactByMatrixId(matrixId)
                if (contact != null && AppCache.getIsOpenContact()) {
                    user.displayName = contact.displayName
                    user.contactAdded = true
                }
                val orgMember = AppDatabase.getInstance(BaseModule.getContext()).UserDao().getBriefUserByMatrixId(matrixId)
                if (AppCache.getIsOpenOrg()) {
                    user.displayName = orgMember?.displayName
                }
                searchUserOnlineLiveData.postValue(user)
                loading.postValue(false)
            }.exceptionOrNull()?.let {
                loading.postValue(false)
                searchUserOnlineLiveData.postValue(null)
                it.printStackTrace()
            }
        }
    }

    fun searchMatrixUserByEmailOrPhone(string: String?) {
        if (string.isNullOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val threePids = ArrayList<ThreePid>()
                if (string.contains("@")) {
                    val threePid = ThreePid.Email(string)
                    threePids.add(threePid)
                } else {
                    val threePid = ThreePid.Msisdn(string)
                    threePids.add(threePid)
                }
                val data = session.identityService().lookUp(threePids)
                if (data.isNotEmpty()) {
                    searchMatrixUser(data[0].matrixId)
                } else if (!string.contains("@") && !string.startsWith("86")) {
                    searchMatrixUserByEmailOrPhone("86$string")
                } else {
                    loading.postValue(false)
                    searchUserOnlineLiveData.postValue(null)
                }
            }.exceptionOrNull()?.let {
                searchUserOnlineLiveData.postValue(null)
                loading.postValue(false)
                it.printStackTrace()
            }
        }
    }

    override fun handle(action: EmptyAction) {
    }
}
