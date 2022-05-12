package im.vector.app.eachchat.search.contactsearch

import ai.workly.eachchat.android.search.adapter.ContactsSearchAdapter
import ai.workly.eachchat.android.search.adapter.ContactsSearchAdapter.Companion.BODY
import ai.workly.eachchat.android.search.adapter.ContactsSearchAdapter.Companion.SUB_TYPE_CHAT_RECORD
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.blankj.utilcode.util.RegexUtils.isEmail
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.R
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.platform.EmptyViewEvents
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.base.EmptyAction
import im.vector.app.eachchat.base.EmptyViewState
import im.vector.app.eachchat.bean.Filter
import im.vector.app.eachchat.bean.GroupFilter
import im.vector.app.eachchat.bean.SearchGroupCountInput
import im.vector.app.eachchat.bean.SearchInput
import im.vector.app.eachchat.contact.RoomComparator
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.resolveMxc
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.DepartmentStoreHelper
import im.vector.app.eachchat.department.data.IDisplayBean
import im.vector.app.eachchat.search.chatRecord.SearchAction
import im.vector.app.eachchat.search.chatRecord.SearchType
import im.vector.app.eachchat.search.contactsearch.data.SearchContactsBean
import im.vector.app.eachchat.search.contactsearch.data.SearchData
import im.vector.app.eachchat.search.contactsearch.data.SearchGroupBean
import im.vector.app.eachchat.search.contactsearch.data.SearchUserBean
import im.vector.app.eachchat.service.SearchService
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.utils.getCloseContactTitle
import im.vector.app.eachchat.utils.string.StringUtils
import im.vector.app.eachchat.utils.string.StringUtils.getKeywordStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.MatrixPatterns
import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.events.model.Content
import org.matrix.android.sdk.api.session.events.model.Event
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.profile.ProfileService
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import timber.log.Timber

class ContactsSearchViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
        private val session: Session
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {

    // private var gmsJob: Job? = null
    var keyword: String = ""
    val searchUserOnlineLiveData = MutableLiveData<ContactsDisplayBean?>()
    val searchResultsLiveData = MutableLiveData<List<ContactsSearchAdapter.BaseItem>>()
    val searchSeeMoreItems = MutableLiveData<List<ContactsSearchAdapter.BaseItem>>()
    private val closeContactUsers = mutableListOf<IDisplayBean>()
    private val myContactUsers = mutableListOf<IDisplayBean>()
    private val orgUsers = mutableListOf<IDisplayBean>()
    private val departments = mutableListOf<IDisplayBean>()
    private val chatRecords = mutableListOf<IDisplayBean>()
    private val groupChatRooms = mutableListOf<IDisplayBean>()
    val action = MutableLiveData<SearchAction>()
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
            val job5 = async {
                searchGroupChat(keyword)
            }
            // 6. 聊天记录
            val job6 = async {
                searchChatRecord(keyword)
            }
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
            parseItem(
                    items,
                    job5.await().toMutableList(),
                    ContactsSearchAdapter.SUB_TYPE_GROUP_CHAT
            )
            if (job6.await() != null) {
                parseItem(items, job6.await()!!, SUB_TYPE_CHAT_RECORD)
            }

            val keywordValid = isEmail(keyword) || StringUtils.isPhoneNumber(keyword) || MatrixPatterns.isUserId(keyword)
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
            val job2 = if (AppCache.getIsOpenOrg()) async {
                searchDepartment(keyword)
            } else null
            parseItem(items, job1.await().toMutableList(), ContactsSearchAdapter.SUB_TYPE_ORG)
            job2?.await()?.toMutableList()?.let {
                parseItem(
                        items,
                        it,
                        ContactsSearchAdapter.SUB_TYPE_DEPARTMENT
                )
            }
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

    fun searchGroupChatJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 部门
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

    fun searchChatRecordJob(keyword: String): Job {
        this.keyword = keyword
        return viewModelScope.launch(Dispatchers.IO) {
            val items = mutableListOf<ContactsSearchAdapter.BaseItem>()

            // 部门
            val job = async {
                searchChatRecord(keyword)
            }
            if (job.await() == null) return@launch
            parseAllItem(
                    items,
                    job.await()!!.toMutableList(),
                    SUB_TYPE_CHAT_RECORD
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
                if (!roomSummary.isDirect) {
                    val searchData = SearchGroupBean(
                            roomSummary.avatarUrl,
                            roomSummary.displayName, roomSummary.roomId
                    )
                    groupChatRooms.add(searchData)
                }
            }
        }
        return groupChatRooms
    }

    private fun searchOrg(keyword: String): List<IDisplayBean> {
        orgUsers.clear()
        val searchUsers = AppDatabase.getInstance(BaseModule.getContext()).userDao().search(keyword, 100)?.filter { it.del != 1 }
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

    private fun searchCloseContact(keyword: String): List<IDisplayBean> {
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
                var user = AppDatabase.getInstance(BaseModule.getContext()).userDao().getBriefUserByMatrixId(matrixUser.userId)
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
                session.identityService().setUserConsent(true)
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
                val orgMember = AppDatabase.getInstance(BaseModule.getContext()).userDao().getBriefUserByMatrixId(matrixId)
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
                session.identityService().setUserConsent(true)
                val threePids = ArrayList<ThreePid>()
                if (isEmail(string)) {
                    val threePid = ThreePid.Email(string)
                    threePids.add(threePid)
                } else {
                    val threePid = ThreePid.Msisdn(string)
                    threePids.add(threePid)
                }
                val user = BaseModule.getSession().identityService()
                        .lookUp(threePids)
                if (user.isNotEmpty()) {
                    searchMatrixUser(user[0].matrixId)
                } else if (StringUtils.isPhoneNumber(string) && !string.startsWith("86")) {
                    searchMatrixUserByEmailOrPhone("86$string")
                } else {
                    searchUserOnlineLiveData.postValue(null)
                }
            }.exceptionOrNull()?.let {
                searchUserOnlineLiveData.postValue(null)
                loading.postValue(false)
                it.printStackTrace()
            }
        }
    }

    private suspend fun searchChatRecord(keyword: String): MutableList<IDisplayBean>? {
        chatRecords.clear()
        val filters = mutableListOf<Filter>()
        val filter = Filter(field = "body", value = keyword)
        val indexFilter = Filter(field = "type", value = "CHAT")
        filters.add(filter)
        filters.add(indexFilter)
        val groupFilter = listOf(GroupFilter(filters))
        val input = SearchGroupCountInput(groups = groupFilter, limit = 0)

        runCatching {
            val response = SearchService.getInstance().searchGroupMessageCount(input)
            if (!response.isSuccess || response.obj == null || response.obj!!.rooms == null) {
                return null
            }
            response.obj!!.rooms!!.results?.forEach {
                val minor: String?
                var mainTitle: String? = ""
                var avatarUrl: String?
                if (it.room_id.isNullOrEmpty()) {
                    return@forEach
                }

                val room = session.getRoom(it.room_id) ?: return@forEach
                val roomSummary = room.roomSummary() ?: return@forEach
                avatarUrl = roomSummary.avatarUrl
                var targetId: String? = null
                if (roomSummary.isDirect && roomSummary.otherMemberIds.isNotEmpty()) {
                    val user = session.getUser(roomSummary.otherMemberIds[0])
                    if (user != null) {
                        mainTitle = getUserDisplayName(user.displayName, user.userId)
                        avatarUrl = user.avatarUrl
                    }
                } else {
                    mainTitle = roomSummary.displayName
                }
                if (it.keywordCount == 1 && it.firstChat != null && it.firstChat.event != null) {
                    minor = it.firstChat.body
                    targetId = it.firstChat.event.event_id
                } else {
                    minor = String.format(
                            BaseModule.getContext()
                                    .getString(R.string.search_message_record), it.keywordCount
                    )
                }
                if (mainTitle.isNullOrEmpty()) {
                    mainTitle = ""
                }

                var more = false
                if (response.obj!!.rooms!!.more != null) {
                    more = response.obj!!.rooms!!.more!!
                }
                val searchData = SearchData(
                        mainTitle, minor, avatarUrl,
                        SUB_TYPE_CHAT_RECORD, it.room_id,
                        more, it.keywordCount, targetId
                )
                searchData.isDirect = roomSummary.isDirect
                chatRecords.add(searchData)
            }
            return chatRecords
        }.exceptionOrNull()?.let {
            Timber.v("聊天记录搜索异常")
            it.printStackTrace()
            error.postValue(it)
        }
        return null
    }

    fun getText(context: Context, event: Event): String {
        val content: Content? = event.getClearContent()
        var str: String? = context.getString(R.string.unknown_message)
        if (content != null) {
            str = content[BODY] as String?
        }
        if (str == null) return ""
        return str
    }

    override fun handle(action: EmptyAction) {
    }

    fun getUserDisplayName(matrixDisplayName: String?, matrixId: String): String? {
        if (TextUtils.isEmpty(matrixId)) {
            return matrixDisplayName
        }
        // 先获取联系人的备注名
        if (AppCache.getIsOpenContact()) {
            val contact = ContactDaoHelper.getInstance().getContactByMatrixId(matrixId)
            if (contact != null && contact.remarkName?.isNotEmpty() == true) {
                return contact.remarkName
            }
        }

        // 如果没有备注名, 再获取组织架构中的名字
        val user = if (AppCache.getIsOpenOrg()) AppDatabase.getInstance(BaseModule.getContext()).userDao().getBriefUserByMatrixId(matrixId) else null

        // 如果组织架构中没有名字, 就使用 Matrix 中的名字
        if (user == null || TextUtils.isEmpty(user.displayName)) {
            return matrixDisplayName
        }
        // if don't have Matrix name，use MatrixId as name
        if (TextUtils.isEmpty(user.displayName)) {
            return matrixId
        }
        return user.displayName
    }

    val pageCount = 20
    private var currentKeyword: String? = null
    val total = MutableLiveData(0)

    fun searchGroupMessage(keyword: String, roomId: String?, count: Int) {
        this.currentKeyword = keyword
        val filters = mutableListOf<Filter>()
        val filter = Filter(field = "body", value = keyword)
        val indexFilter = Filter(field = "type", value = "CHAT")
        filters.add(filter)
        filters.add(indexFilter)
        if (!roomId.isNullOrEmpty()) {
            val roomFilter = Filter(field = "room_id", value = roomId)
            filters.add(roomFilter)
        }
        val input = SearchInput(filters = filters, sequenceId = count, perPage = pageCount)
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val response = SearchService.getInstance().searchGroupMessage(input)
                total.postValue(response.total)
                if (!response.isSuccess) {
                    loading.postValue(false)
                    error.postValue(Failure.NetworkConnection())
                    return@launch
                }
                val searchDatas = mutableListOf<SearchData>()
                response.results?.forEach {
                    if (it?.event == null) return@forEach
                    var displayName: String? = ""
                    var minor: String?
                    var avatarUrl: String? = ""
                    val id = it.event.sender
                    if (!id.isNullOrEmpty()) {
                        val user = BaseModule.getSession()?.getUser(id)
                        displayName = getUserDisplayName(user?.displayName, id)
                        avatarUrl = user?.avatarUrl
                    }
                    displayName = getKeywordStr(displayName, keyword)
                    minor = it.body
                    minor = getKeywordStr(minor, keyword)
                    val data = SearchData(displayName, minor, avatarUrl,
                            SearchType.GroupMessage, it.event.event_id, false, targetId = roomId)
                    data.time = it.event.origin_server_ts
                    searchDatas.add(data)
                }
                if (count == 0) {
                    action.postValue(SearchAction.SearchResult(searchDatas))
                } else {
                    action.postValue(SearchAction.SearchMoreResult(searchDatas))
                }
            }.exceptionOrNull()?.let {
                action.postValue(SearchAction.SearchResult(null))
            }
        }
    }
}


