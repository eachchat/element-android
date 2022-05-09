/*
 * Copyright 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package im.vector.app.eachchat.user

import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.mvrx.runCatchingToAsync
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import im.vector.app.features.roommemberprofile.RoomMemberProfileAction
import im.vector.app.features.roommemberprofile.RoomMemberProfileViewEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.query.RoomCategoryFilter
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.accountdata.UserAccountDataTypes
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.profile.ProfileService
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.flow.flow
import org.matrix.android.sdk.flow.unwrap
import timber.log.Timber

class UserInfoViewModel @AssistedInject constructor(
        @Assisted private val initialState: UserInfoViewState,
        // private val stringProvider: StringProvider,
        private val matrixItemColorProvider: MatrixItemColorProvider,
        private val session: Session
) : VectorViewModel<UserInfoViewState, RoomMemberProfileAction, RoomMemberProfileViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<UserInfoViewModel, UserInfoViewState> {
        override fun create(initialState: UserInfoViewState): UserInfoViewModel
    }

    companion object : MavericksViewModelFactory<UserInfoViewModel, UserInfoViewState> by hiltMavericksViewModelFactory()

    init {
//        observeIgnoredState()
        observeAccountData()
        viewModelScope.launch(Dispatchers.IO) {
            val user = AppDatabase.getInstance(BaseModule.getContext()).userDao().getBriefUserById(initialState.departmentUserId)
            setState {
                copy(departmentUser = user)
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            fetchProfileInfo()
        }

        viewModelScope.launch(Dispatchers.IO) {
            val queryParams = roomSummaryQueryParams {
                memberships = listOf(Membership.JOIN)
                roomCategoryFilter = RoomCategoryFilter.ONLY_DM
            }
            var roomSummaries = session.getRoomSummaries(queryParams)
            roomSummaries = roomSummaries.filter { it.otherMemberIds[0] == initialState.userId && it.joinedMembersCount == 2 }
            if (roomSummaries.isNotEmpty()) {
                setState { copy(directRoomId = roomSummaries[0].roomId) }
            }
        }

        if (initialState.userId == BaseModule.getSession().myUserId) {
            setState {
                copy(isMine = true)
            }
        }
    }

    fun getExistingDM() {
        viewModelScope.launch(Dispatchers.IO) {
            val queryParams = roomSummaryQueryParams {
                memberships = listOf(Membership.JOIN)
                roomCategoryFilter = RoomCategoryFilter.ONLY_DM
            }
            var roomSummaries = session.getRoomSummaries(queryParams)
            roomSummaries = roomSummaries.filter { it.otherMemberIds[0] == initialState.userId && it.joinedMembersCount == 2 }
            if (roomSummaries.isNotEmpty()) {
                setState { copy(directRoomId = roomSummaries[0].roomId) }
            }
        }
    }

    // 观察一些补充的信息
    fun observeOtherInfo(lifecycleOwner: LifecycleOwner) {
        if (AppCache.getIsOpenContact())
            observeContact(lifecycleOwner)
        if (AppCache.getIsOpenOrg())
            observeDepartmentUser(lifecycleOwner)
    }

    private fun observeContact(lifecycleOwner: LifecycleOwner) {
        if (!initialState.userId.isNullOrBlank()) {
            AppDatabase
                    .getInstance(BaseModule.getContext()).contactDaoV2()
                    .getContactByMatrixIdLive(initialState.userId).observe(lifecycleOwner) {
                        setState {
                            copy(contact = it)
                        }
                    }
        }
    }

    private fun observeDepartmentUser(lifecycleOwner: LifecycleOwner) {
        if (!initialState.userId.isNullOrBlank()) {
            AppDatabase
                    .getInstance(BaseModule.getContext()).userDao()
                    .getBriefUserByMatrixIdLive(initialState.userId).observe(lifecycleOwner) {
                        setState {
                            copy(departmentUser = it)
                        }
                    }
        }
    }

    private fun observeAccountData() {
        session.flow()
                .liveUserAccountData(UserAccountDataTypes.TYPE_OVERRIDE_COLORS)
                .unwrap()
                .onEach {
                    val newUserColor = it.content.toModel<Map<String, String>>()?.get(initialState.userId)
                    setState {
                        copy(
                                userColorOverride = newUserColor
                        )
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun observeIgnoredState() {
        session.flow().liveIgnoredUsers()
                .map { ignored ->
                    ignored.find {
                        it.userId == initialState.userId
                    } != null
                }
                .execute {
                    copy(isIgnored = it)
                }
    }

    override fun handle(action: RoomMemberProfileAction) {
    }

    private fun handleSetUserColorOverride(action: RoomMemberProfileAction.SetUserColorOverride) {
        val newOverrideColorSpecs = session.accountDataService()
                .getUserAccountDataEvent(UserAccountDataTypes.TYPE_OVERRIDE_COLORS)
                ?.content
                ?.toModel<Map<String, String>>()
                .orEmpty()
                .toMutableMap()
        initialState.userId?.let {
            if (matrixItemColorProvider.setOverrideColor(initialState.userId, action.newColorSpec)) {
                newOverrideColorSpecs[initialState.userId] = action.newColorSpec
            } else {
                newOverrideColorSpecs.remove(initialState.userId)
            }
        }
        viewModelScope.launch {
            try {
                session.accountDataService().updateUserAccountData(
                        type = UserAccountDataTypes.TYPE_OVERRIDE_COLORS,
                        content = newOverrideColorSpecs
                )
            } catch (failure: Throwable) {
                _viewEvents.post(RoomMemberProfileViewEvents.Failure(failure))
            }
        }
    }

    private fun handleRetryFetchProfileInfo() {
        viewModelScope.launch {
            fetchProfileInfo()
        }
    }

    private suspend fun fetchProfileInfo() {
        val result = runCatchingToAsync {
            initialState.userId?.let { id ->
                session.getProfile(id)
                        .let {
                            MatrixItem.UserItem(
                                    id = initialState.userId,
                                    displayName = it[ProfileService.DISPLAY_NAME_KEY] as? String,
                                    avatarUrl = it[ProfileService.AVATAR_URL_KEY] as? String
                            )
                        }
            }
        }

        setState {
            copy(userMatrixItem = result)
        }
    }

    private fun handleIgnoreAction() = withState { state ->
        val isIgnored = state.isIgnored() ?: return@withState
        _viewEvents.post(RoomMemberProfileViewEvents.Loading())
        viewModelScope.launch {
            val event = try {
                if (isIgnored) {
                    session.unIgnoreUserIds(listOfNotNull(state.userId))
                } else {
                    session.ignoreUserIds(listOfNotNull(state.userId))
                }
                RoomMemberProfileViewEvents.OnIgnoreActionSuccess
            } catch (failure: Throwable) {
                RoomMemberProfileViewEvents.Failure(failure)
            }
            _viewEvents.post(event)
        }
    }

    private fun handleShareRoomMemberProfile() {
        initialState.userId?.let {
            session.permalinkService().createPermalink(it)?.let { permalink ->
                _viewEvents.post(RoomMemberProfileViewEvents.ShareRoomMemberProfile(permalink))
            }
        }
    }

    fun addContacts(contact: ContactsDisplayBean) {
        loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val contactV2 = contact.toContactsDisplayBeanV2()
                val response = ContactServiceV2.getInstance().add(contactV2)
                if (!response.obj?.id.isNullOrEmpty()) {
                    contact.contactAdded = true
                    contact.contactId = response.obj?.id.orEmpty()
                    withContext(Dispatchers.IO) {
                        ContactDaoHelper.getInstance().insertContacts(contact)
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

    fun deleteContact(mContact: ContactsDisplayBeanV2, deleteSuccessListener: () -> Unit) {
        loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val response = mContact.id?.let { it1 -> ContactServiceV2.getInstance().delete(it1) }
                if (response?.isSuccess == true) {
                    mContact.del = 1
                    AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().update(mContact)
                    // callBack.invoke(RoomProfileController.END_LOADING)
                    deleteSuccessListener.invoke()
                    loading.postValue(false)
                } else {
                    // callBack.invoke(RoomProfileController.END_LOADING)
                    loading.postValue(false)
                }
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                loading.postValue(false)
                Timber.e("删除联系人错误")
            }
        }
    }
}
