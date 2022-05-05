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

package im.vector.app.eachchat.moreinfo

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
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.user.UserInfoViewState
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import im.vector.app.features.roommemberprofile.RoomMemberProfileAction
import im.vector.app.features.roommemberprofile.RoomMemberProfileViewEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.accountdata.UserAccountDataTypes
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.profile.ProfileService
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.flow.flow
import org.matrix.android.sdk.flow.unwrap

class MoreInfoViewModel @AssistedInject constructor(
        @Assisted private val initialState: UserInfoViewState,
        // private val stringProvider: StringProvider,
        private val matrixItemColorProvider: MatrixItemColorProvider,
        private val session: Session
) : VectorViewModel<UserInfoViewState, RoomMemberProfileAction, RoomMemberProfileViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<MoreInfoViewModel, UserInfoViewState> {
        override fun create(initialState: UserInfoViewState): MoreInfoViewModel
    }

    companion object : MavericksViewModelFactory<MoreInfoViewModel, UserInfoViewState> by hiltMavericksViewModelFactory()

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

        setState { copy(directRoomId = initialState.userId?.let { session.getExistingDirectRoomWithUser(it) }) }
    }

    // 观察一些补充的信息
    fun observeOtherInfo(lifecycleOwner: LifecycleOwner) {
        if (AppCache.getIsOpenContact())
            observeContact(lifecycleOwner)
        if (AppCache.getIsOpenOrg())
            observeDepartmentUser(lifecycleOwner)
    }

    private fun observeContact(lifecycleOwner: LifecycleOwner) {
        initialState.userId?.let {
            AppDatabase
                    .getInstance(BaseModule.getContext()).contactDaoV2()
                    .getContactByMatrixIdLive(it).observe(lifecycleOwner) {
                        setState {
                            copy(contact = it)
                        }
                    }
        }
    }

    private fun observeDepartmentUser(lifecycleOwner: LifecycleOwner) {
        AppDatabase
                .getInstance(BaseModule.getContext()).userDao()
                .getBriefUserByMatrixIdLive(initialState.userId).observe(lifecycleOwner) {
                    setState {
                        copy(departmentUser = it)
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
}