/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.eachchat.contact.real

import androidx.lifecycle.LifecycleOwner
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
import im.vector.app.eachchat.bean.OrgSearchInput
import im.vector.app.eachchat.service.LoginApi
import im.vector.app.eachchat.base.EmptyAction
import im.vector.app.eachchat.base.EmptyViewState
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.contact.RoomComparator
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.getCompany
import im.vector.app.eachchat.department.getHomeSever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.query.RoomCategoryFilter
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import timber.log.Timber

class RealContactsViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
        private val session: Session) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {

    // 最常联系
    val closeContactData = MutableLiveData<List<User>>()
    val isOpenContactLiveData = MutableLiveData<Boolean?>()
    val isOpenOrgLiveData = MutableLiveData<Boolean?>()
    val isOpenGroupLiveData = MutableLiveData<Boolean?>()
    private var gmsJob: Job? = null

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<RealContactsViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): RealContactsViewModel
    }

    companion object : MavericksViewModelFactory<RealContactsViewModel, EmptyViewState> by hiltMavericksViewModelFactory() {

        override fun initialState(viewModelContext: ViewModelContext): EmptyViewState {
            return EmptyViewState(
            )
        }
    }

    fun loadCloseContacts(owner: LifecycleOwner) {
        val queryParams = roomSummaryQueryParams {
            memberships = listOf(Membership.JOIN)
            roomCategoryFilter = RoomCategoryFilter.ONLY_DM
        }
        session.getRoomSummariesLive(queryParams).observe(owner) {
            if (it.isNullOrEmpty()) return@observe
            var users = mutableListOf<User>()
            val joinRooms = mutableListOf<RoomSummary>()
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    it.forEach { roomSummary ->
                        if (roomSummary.isDirect && roomSummary.otherMemberIds.isNotEmpty()) {
                            joinRooms.add(roomSummary)
                        }
                    }
                    val processedJoinRooms =
                            joinRooms // .distinctBy { roomSummary -> roomSummary.otherMemberIds }
                                    .sortedWith(RoomComparator())
                    processedJoinRooms.forEach { roomSummary2 ->
                        var user: User?
                        val matrixUser = session.getUser(roomSummary2.otherMemberIds[0]) ?: return@forEach
                        user = if (AppCache.getIsOpenOrg()) AppDatabase.getInstance(BaseModule.getContext()).userDao().getBriefUserByMatrixId(matrixUser.userId) else null
                        var contact: ContactsDisplayBean? = null
                        if (user?.matrixId?.getHomeSever() != BaseModule.getSession().myUserId.getHomeSever()) {
                            user?.userTitle = getCompany(null, user?.departmentId) + " "  + user?.userTitle
                        }
                        if (matrixUser.userId.isNotEmpty() && AppCache.getIsOpenContact()) {
                            contact = ContactDaoHelper.getInstance().getContactByMatrixId(matrixUser.userId)
                        }
                        if (contact != null) {
                            user = contact.toUser()
                        }
                        if (user == null) {
                            user = User()
                            user.displayName = matrixUser.displayName
                            user.avatarTUrl = matrixUser.avatarUrl
                            user.avatarOUrl = matrixUser.avatarUrl
                            user.matrixId = matrixUser.userId
                        }
                        user.roomId = roomSummary2.roomId
                        user.avatarTUrl = roomSummary2.avatarUrl
                        user.avatarOUrl = roomSummary2.avatarUrl
                        if (user.displayName.isNullOrBlank()) {
                            user.displayName = user.matrixId
                        }
                        users.add(user)
                    }
                }.exceptionOrNull()?.let {
                    it.printStackTrace()
                    Timber.e("通讯录异常")
                }
                users = users.distinctBy { user -> user.matrixId  }.toMutableList()
                closeContactData.postValue(users)
            }
        }
    }

    fun startDirectChat(matrixId: String?) {
        if (matrixId == null) return
//        val callback = object : MatrixCallback<String> {
//            override fun onSuccess(data: String) {
//                super.onSuccess(data)
////                ARouter.getInstance().build(RoutePath.CHAT_DETAIL)
////                    .withString(BaseConstant.KEY_ROOM_ID, data)
////                    .navigation()
//            }
//
//            override fun onFailure(failure: Throwable) {
//                super.onFailure(failure)
//            }
//        }
    }

    fun getGMSConfig() {
        if (gmsJob?.isActive == true) return
        gmsJob = viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val homeServerUrl = AppCache.getTenantName()
                val response = LoginApi.getInstance(LoginApi.GMS_URL)
                        ?.gms(OrgSearchInput(homeServerUrl))
                if (response?.isSuccess == true) {
                    val isOpenContact = response.obj?.book?.contactSwitch ?: false
                    isOpenContactLiveData.postValue(isOpenContact)
                    AppCache.setIsOpenContact(isOpenContact)
                    val isOpenGroup = response.obj?.book?.groupSwitch ?: false
                    isOpenGroupLiveData.postValue(isOpenGroup)
                    AppCache.setIsOpenGroup(isOpenGroup)
                    val isOpenOrg = response.obj?.book?.orgSwitch ?: false
                    AppCache.setIsOpenOrg(isOpenOrg)
                    isOpenOrgLiveData.postValue(isOpenOrg)
                    val isOpenVideoCall = response.obj?.im?.videoSwitch ?: false
                    AppCache.setIsOpenVideoCall(isOpenVideoCall)
                    val isOpenVoiceCall = response.obj?.im?.audioSwitch ?: false
                    AppCache.setIsOpenVoiceCall(isOpenVoiceCall)
                }
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    fun getGMSConfigCache() {
        isOpenContactLiveData.postValue(AppCache.getIsOpenContact())
        isOpenGroupLiveData.postValue(AppCache.getIsOpenGroup())
        isOpenOrgLiveData.postValue(AppCache.getIsOpenOrg())
    }

    override fun handle(action: EmptyAction) {
    }
}
