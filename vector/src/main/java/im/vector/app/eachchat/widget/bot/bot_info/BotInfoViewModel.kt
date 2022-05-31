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

package im.vector.app.eachchat.widget.bot.bot_info

import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.args
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

class BotInfoViewModel @AssistedInject constructor(
        @Assisted private val initialState: BotInfoViewState,
        // private val stringProvider: StringProvider,
        private val session: Session
) : VectorViewModel<BotInfoViewState, RoomMemberProfileAction, RoomMemberProfileViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<BotInfoViewModel, BotInfoViewState> {
        override fun create(initialState: BotInfoViewState): BotInfoViewModel
    }

    companion object : MavericksViewModelFactory<BotInfoViewModel, BotInfoViewState> by hiltMavericksViewModelFactory()

    init {
//        observeIgnoredState()
        observeAccountData()
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

        viewModelScope.launch (Dispatchers.IO) {
            initialState.userId?.let {
                val bot = AppDatabase.getInstance(BaseModule.getContext()).botDao().getBot(it)
                setState {
                    copy(botDescription = bot?.description, botDeveloper = bot?.developer, botHelp = bot?.help)
                }
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

    override fun handle(action: RoomMemberProfileAction) {
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
}
