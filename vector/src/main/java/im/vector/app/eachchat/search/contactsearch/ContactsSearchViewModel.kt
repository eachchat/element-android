package im.vector.app.eachchat.search.contactsearch

import ai.workly.eachchat.android.search.adapter.ContactsSearchAdapter
import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.blankj.utilcode.util.RegexUtils.isEmail
import com.blankj.utilcode.util.RegexUtils.isTel
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
import im.vector.app.eachchat.contact.RoomComparator
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.resolveMxc
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.DepartmentStoreHelper
import im.vector.app.eachchat.department.data.IDisplayBean
import im.vector.app.eachchat.search.contactsearch.data.SearchContactsBean
import im.vector.app.eachchat.search.contactsearch.data.SearchGroupBean
import im.vector.app.eachchat.search.contactsearch.data.SearchUserBean
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.utils.getCloseContactTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.MatrixPatterns
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.profile.ProfileService
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams

class ContactsSearchViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
        private val session: Session
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {

    private var gmsJob: Job? = null
    var keyword: String = ""
    val searchUserOnlineLiveData = MutableLiveData<ContactsDisplayBean?>()
    val searchResultsLiveData = MutableLiveData<List<ContactsSearchAdapter.BaseItem>>()
    val searchSeeMoreItems = MutableLiveData<List<ContactsSearchAdapter.BaseItem>>()
    private val closeContactUsers = mutableListOf<IDisplayBean>()
    private val myContactUsers = mutableListOf<IDisplayBean>()
    private val orgUsers = mutableListOf<IDisplayBean>()
    private val departments = mutableListOf<IDisplayBean>()
    private val groupChatRooms = mutableListOf<IDisplayBean>()
    private val contactMatrixUserDao =
            AppDatabase.getInstance(BaseModule.getContext()).contactMatrixUserDao()

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<ContactsSearchViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): ContactsSearchViewModel
    }

    companion object : MavericksViewModelFactory<ContactsSearchViewModel, EmptyViewState> by hiltMavericksViewModelFactory() {
        // private const val PAGE_LIMIT = 100

        override fun initialState(viewModelContext: ViewModelContext): EmptyViewState {
            return EmptyViewState(
            )
        }
    }

    fun search(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 1.最常联系
            val job1 = async {
                searchCloseContact(keyword)
            }
            // 2.我的联系人
            val job2 = if (AppCache.getIsOpenContact()) async {
                searchMyContacts(keyword)
            } else null
            // 3.组织
            val job3 = async {
                searchOrg(keyword)
            }
            // 4.部门
            val job4 = if (AppCache.getIsOpenOrg()) async {
                searchDepartment(keyword)
            } else null
            // 5.群聊
//            val job5 = if (AppCache.getIsOpenGroup()) async {
//                searchGroupChat(keyword)
//            } else null
            parseItem(
                    items,
                    job1.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_CLOSE_CONTACT
            )
            if (job2 != null) {
                parseItem(
                        items,
                        job2.await().toMutableList(),
                        ContactsSearchAdapter.SUB_TYPE_MY_CONTACT
                )
            }
            parseItem(items, job3.await().toMutableList(), ContactsSearchAdapter.SUB_TYPE_ORG)
            if (job4 != null) {
                parseItem(
                        items,
                        job4.await().toMutableList(),
                        ContactsSearchAdapter.SUB_TYPE_DEPARTMENT
                )
            }
//            if (job5 != null) {
//                parseItem(
//                        items,
//                        job5.await().toMutableList(),
//                        ContactsSearchAdapter.SUB_TYPE_GROUP_CHAT
//                )
//            }
            val keywordValid = isEmail(keyword) || isTel(keyword) || MatrixPatterns.isUserId(keyword)
            if (items.size == 0 && keywordValid) {
                items.add(
                        ContactsSearchAdapter.SearchContactOnlineItem(
                                ContactsSearchAdapter.TYPE_SEARCH_CONTACT_ONLINE,
                                keyword
                        )
                )
            }
            searchResultsLiveData.postValue(items)
        }
    }

    fun searchInOrganizationJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 1.组织
            val job1 = async {
                searchOrg(keyword)
            }
            // 2.部门
            val job2 = async {
                searchDepartment(keyword)
            }
            parseItem(items, job1.await().toMutableList(), ContactsSearchAdapter.SUB_TYPE_ORG)
            parseItem(
                    items,
                    job2.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_DEPARTMENT
            )
            searchResultsLiveData.postValue(items)
        }
    }

    fun searchDepartmentJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 部门
            val job = async {
                searchDepartment(keyword)
            }
            parseAllItem(
                    items,
                    job.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_DEPARTMENT
            )
            searchResultsLiveData.postValue(items)
        }
    }

    fun searchRealContactJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            val job = async {
                searchCloseContact(keyword)
            }
            parseAllItem(
                    items,
                    job.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_CLOSE_CONTACT
            )
            searchResultsLiveData.postValue(items)
        }
    }

    fun searchOrgJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 部门
            val job = async {
                searchOrg(keyword)
            }
            parseAllItem(
                    items,
                    job.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_ORG
            )
            searchResultsLiveData.postValue(items)
        }
    }

    fun searchMyContactJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 部门
            val job = async {
                searchMyContacts(keyword)
            }
            parseAllItem(
                    items,
                    job.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_MY_CONTACT
            )
            searchResultsLiveData.postValue(items)
        }
    }

    fun searchContactGroupJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 通讯录里的群聊
            val job = async {
                searchGroupChat(keyword)
            }
            parseAllItem(
                    items,
                    job.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_GROUP_CHAT
            )
            searchResultsLiveData.postValue(items)
        }
    }

    private fun searchDepartment(keyword: String): List<IDisplayBean> {
        departments.clear()
        departments.addAll(DepartmentStoreHelper.search(keyword, 100, null))
        return departments
    }

    private fun searchGroupChat(keyword: String): MutableList<IDisplayBean> {
        val allRoomIds = AppDatabase.getInstance(BaseModule.getContext()).contactRoomDao().getContactRooms()
        val queryParams = roomSummaryQueryParams {
            memberships = listOf(Membership.JOIN)
        }
        session.getRoomSummaries(queryParams).let {
            groupChatRooms.clear()
            val joinRooms = mutableListOf<RoomSummary>()
            it.forEach { roomSummary ->
                if (!roomSummary.isDirect && roomSummary.displayName.contains(keyword, true)) {
                    joinRooms.add(roomSummary)
                }
            }
            joinRooms.sortWith(RoomComparator())
            joinRooms.forEach { roomSummary ->
                val index = allRoomIds.indexOf(roomSummary.roomId)
                if (index > -1) {
                    if (!roomSummary.isDirect) {
                        val searchData = SearchGroupBean(
                                roomSummary.avatarUrl,
                                roomSummary.displayName, roomSummary.roomId
                        )
                        groupChatRooms.add(searchData)
                    }
                }
            }
        }
        return groupChatRooms
    }

    private fun searchOrg(keyword: String): List<IDisplayBean> {
        orgUsers.clear()
        val searchUsers = AppDatabase.getInstance(BaseModule.getContext()).UserDao().search(keyword, 100)
        if (!searchUsers.isNullOrEmpty()) {
            searchUsers.forEach {
                val searchUserBean = SearchUserBean(it, keyword)
                searchUserBean.avatarTUrl =
                        searchUserBean.matrixId?.let { it1 -> session.getUser(it1)?.avatarUrl?.takeIf { avatarUrl -> avatarUrl.isNotEmpty() } }
                                ?: searchUserBean.matrixId?.let { it1 -> contactMatrixUserDao.getContact(it1)?.avatar }
                orgUsers.add(searchUserBean)
            }
        }
        return orgUsers
    }

    private fun searchMyContacts(keyword: String): List<IDisplayBean> {
        myContactUsers.clear()
        val contacts = ContactDaoHelper.getInstance().searchContacts(keyword)
        contacts.forEach {
            val matrixUser = session.getUser(it.matrixId)
            myContactUsers.add(SearchContactsBean(it).apply {
                matrixUserAvatar = matrixUser?.avatarUrl
            })
        }
        return myContactUsers
    }

    fun searchCloseContact(keyword: String): List<IDisplayBean> {
        val queryParams = roomSummaryQueryParams {
            memberships = listOf(Membership.JOIN)
        }
        session.getRoomSummaries(queryParams).let {
            closeContactUsers.clear()
            val joinRooms = mutableListOf<RoomSummary>()
            it.forEach { roomSummary ->
                if (roomSummary.isDirect && roomSummary.otherMemberIds.isNotEmpty()) {
                    joinRooms.add(roomSummary)
                }
            }
            val processedJoinRooms =
                    joinRooms.distinctBy { roomSummary -> roomSummary.otherMemberIds }
                            .sortedWith(RoomComparator())
            processedJoinRooms.forEach { roomSummary ->
                val matrixUser = session.getUser(roomSummary.otherMemberIds[0]) ?: return@forEach
                var user = AppDatabase.getInstance(BaseModule.getContext()).UserDao().getBriefUserByMatrixId(matrixUser.userId)
                val contact = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2()
                        .getContactByMatrixId(matrixUser.userId)
                if (user == null) {
                    user = contact?.toContactsDisplayBean()?.toUser()
                }
                if (user != null && (user.displayName?.contains(keyword, true) == true)) {
                    val searchUserBean = SearchUserBean(user, keyword).apply {
                        avatarTUrl = matrixUser.avatarUrl
                        contactBase64Avatar = contact?.photo
                        contactUrlAvatar = contact?.photoUrl
                        userTitle = user.matrixId?.let { it1 -> getCloseContactTitle(it1) }
                    }
                    closeContactUsers.add(searchUserBean)
                }
            }
        }
        return closeContactUsers
    }

    private fun parseItem(
            items: MutableList<ContactsSearchAdapter.BaseItem>,
            displayBeans: MutableList<IDisplayBean>,
            subType: Int
    ) {
        if (displayBeans.isNotEmpty()) {
            if (displayBeans.size > 3) {
                items.add(
                        ContactsSearchAdapter.HeaderItem(
                                ContactsSearchAdapter.TYPE_HEADER,
                                subType
                        )
                )
                for (i in 0 until 3) {
                    items.add(
                            ContactsSearchAdapter.ContentItem(
                                    ContactsSearchAdapter.TYPE_CONTENT,
                                    subType,
                                    displayBeans[i]
                            )
                    )
                }
                items.add(
                        ContactsSearchAdapter.FooterItem(
                                ContactsSearchAdapter.TYPE_FOOTER,
                                subType
                        )
                )
            } else {
                items.add(
                        ContactsSearchAdapter.HeaderItem(
                                ContactsSearchAdapter.TYPE_HEADER,
                                subType
                        )
                )
                displayBeans.forEach {
                    items.add(
                            ContactsSearchAdapter.ContentItem(
                                    ContactsSearchAdapter.TYPE_CONTENT,
                                    subType,
                                    it
                            )
                    )
                }
            }
            items.add(ContactsSearchAdapter.GapItem(ContactsSearchAdapter.TYPE_GAP, subType))
        }
    }

    private fun parseAllItem(
            items: MutableList<ContactsSearchAdapter.BaseItem>,
            displayBeans: MutableList<IDisplayBean>,
            subType: Int
    ) {
        items.add(ContactsSearchAdapter.HeaderItem(ContactsSearchAdapter.TYPE_HEADER, subType))
        displayBeans.forEach {
            items.add(
                    ContactsSearchAdapter.ContentItem(
                            ContactsSearchAdapter.TYPE_CONTENT,
                            subType,
                            it
                    )
            )
        }
        items.add(ContactsSearchAdapter.GapItem(ContactsSearchAdapter.TYPE_GAP, subType))
    }

    fun buildSearchSeeMoreData(subType: Int) {
        val items = mutableListOf<ContactsSearchAdapter.BaseItem>()
        items.add(ContactsSearchAdapter.HeaderItem(ContactsSearchAdapter.TYPE_HEADER, subType))
        when (subType) {
            ContactsSearchAdapter.SUB_TYPE_CLOSE_CONTACT -> parseSeeMoreItem(
                    items,
                    closeContactUsers,
                    subType
            )
            ContactsSearchAdapter.SUB_TYPE_MY_CONTACT    -> parseSeeMoreItem(
                    items,
                    myContactUsers,
                    subType
            )
            ContactsSearchAdapter.SUB_TYPE_ORG           -> parseSeeMoreItem(items, orgUsers, subType)
            ContactsSearchAdapter.SUB_TYPE_DEPARTMENT    -> parseSeeMoreItem(
                    items,
                    departments,
                    subType
            )
            ContactsSearchAdapter.SUB_TYPE_GROUP_CHAT    -> parseSeeMoreItem(
                    items,
                    groupChatRooms,
                    subType
            )
        }
    }

    private fun parseSeeMoreItem(
            items: MutableList<ContactsSearchAdapter.BaseItem>,
            displayBeans: MutableList<IDisplayBean>,
            subType: Int
    ) {
        if (displayBeans.isNotEmpty()) {
            displayBeans.forEach {
                items.add(
                        ContactsSearchAdapter.ContentItem(
                                ContactsSearchAdapter.TYPE_CONTENT,
                                subType,
                                it
                        )
                )
            }
            searchSeeMoreItems.postValue(items)
        }
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
                user.matrixId = matrixId
                val contact = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().getContactByMatrixId(matrixId)
                if (contact != null && AppCache.getIsOpenContact()) {
                    user.subTitle = contact.title
                    user.displayName = contact.displayName
                }
                val orgMember = AppDatabase.getInstance(BaseModule.getContext()).UserDao().getBriefUserByMatrixId(matrixId)
                if (orgMember != null && AppCache.getIsOpenOrg()) {
                    user.subTitle = orgMember.userTitle
                    user.displayName = orgMember.displayName
                }
                loading.postValue(false)
                searchUserOnlineLiveData.postValue(user)
            }.exceptionOrNull()?.let {
                searchUserOnlineLiveData.postValue(null)
                loading.postValue(false)
                it.printStackTrace()
            }
        }
    }

    fun searchMatrixUserByEmailOrPhone(string: String?) {
        if (string.isNullOrBlank()) return
        loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val threePids = ArrayList<ThreePid>()
                if (isEmail(string)) {
                    val threePid = ThreePid.Email(string)
                    threePids.add(threePid)
                } else {
                    val threePid = ThreePid.Msisdn(string)
                    threePids.add(threePid)
                }
                session.identityService()
                        .lookUp(threePids)
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
