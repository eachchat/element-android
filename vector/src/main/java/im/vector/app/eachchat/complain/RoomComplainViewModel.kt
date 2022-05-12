/*
 * Copyright 2019 New Vector Ltd
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
 */

package im.vector.app.eachchat.complain

import com.airbnb.mvrx.MavericksViewModelFactory
import com.blankj.utilcode.util.SPStaticUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.platform.EmptyViewEvents
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.eachchat.service.LoginApi
import im.vector.app.eachchat.complain.api.ComplainService
import im.vector.app.eachchat.complain.data.ComplainInput
import im.vector.app.eachchat.complain.data.ReportBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.flow.flow
import org.matrix.android.sdk.flow.unwrap

class RoomComplainViewModel @AssistedInject constructor(@Assisted initialState: RoomComplainViewState,
                                                        private val session: Session) :
        VectorViewModel<RoomComplainViewState, RoomComplainAction, EmptyViewEvents>(initialState) {

    var complainSuccessCallback: ((Boolean) -> Unit)? = null

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<RoomComplainViewModel, RoomComplainViewState> {
        override fun create(initialState: RoomComplainViewState): RoomComplainViewModel
    }

    companion object : MavericksViewModelFactory<RoomComplainViewModel, RoomComplainViewState> by hiltMavericksViewModelFactory()

    private val room = session.getRoom(initialState.roomId)!!

    init {
        observeRoomSummary()
        getComplainFromLocal()
        getComplainFromServer()
    }

    private fun observeRoomSummary() {
        room.flow().liveRoomSummary()
                .unwrap()
                .execute { async ->
                    copy(roomSummary = async)
                }
    }

    private fun getComplainFromLocal() {
        val strings = SPStaticUtils.getString("complains", "")
        val list = strings.split(",")
        val complains = ArrayList<String>()
        list.forEach {
            complains.add(it)
        }
        setState {
            copy(complainList = complains)
        }
    }

    private fun getComplainFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val complains = ArrayList<ReportBean>()
                val response = withContext(Dispatchers.IO) {
                    ComplainService.getInstance(LoginApi.GMS_URL)?.getComplains("complainAnswer")
                }
                if (response == null || !response.isSuccess) {
                    return@launch
                }
                val list = response.results
                list?.forEach {
                    complains.add(ReportBean(it, false))
                }
                if (list != null) {
                    SPStaticUtils.put("complains", list.toArray().joinToString(","))
                    setState {
                        copy(complainList = list)
                    }
                }
            }
        }
    }

    fun complain(complainStrs: List<String>) {
        setState { copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.Main) {
            if (complainStrs.isNullOrEmpty()) {
                return@launch
            }
            kotlin.runCatching {
                val response = withContext(Dispatchers.IO) {
                    val input = ComplainInput(complainStrs, session.myUserId, room.roomId)
                    ComplainService.getInstance(LoginApi.GMS_URL)?.complain(input)
                }
                if (response == null || !response.isSuccess) {
                    setState { copy(isLoading = false) }
                    complainSuccessCallback?.invoke(false)
                    return@launch
                }
                setState { copy(isLoading = false) }
                complainSuccessCallback?.invoke(true)
            }.exceptionOrNull()?.let {
                setState { copy(isLoading = false) }
                complainSuccessCallback?.invoke(false)
            }
        }
    }

    override fun handle(action: RoomComplainAction) {
    }
}
