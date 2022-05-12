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

package im.vector.app.features.roomprofile.contact

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
import im.vector.app.eachchat.complain.RoomComplainAction
import im.vector.app.eachchat.complain.RoomComplainViewModel
import im.vector.app.eachchat.complain.RoomComplainViewState
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.flow.flow
import org.matrix.android.sdk.flow.unwrap
import timber.log.Timber

class RoomContactViewModel @AssistedInject constructor(@Assisted initialState: RoomComplainViewState,
                                                       private val session: Session) :
        VectorViewModel<RoomComplainViewState, RoomComplainAction, EmptyViewEvents>(initialState) {

    var contactAddSuccessCallback: ((Boolean) -> Unit)? = null

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<RoomContactViewModel, RoomComplainViewState> {
        override fun create(initialState: RoomComplainViewState): RoomContactViewModel
    }

    companion object : MavericksViewModelFactory<RoomContactViewModel, RoomComplainViewState> by hiltMavericksViewModelFactory()

    private val room = session.getRoom(initialState.roomId)!!

    init {
        observeRoomSummary()
    }

    private fun observeRoomSummary() {
        room.flow().liveRoomSummary()
                .unwrap()
                .execute { async ->
                    copy(roomSummary = async)
                }
    }

    fun addContact() {
        loading.postValue(true)
        room.roomSummary()?.otherMemberIds?.get(0)?.let {
            viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val contact = ContactsDisplayBeanV2(matrixId = it, nickName = room.roomSummary()?.displayName, photoUrl = room.roomSummary()?.avatarUrl)
                    val response = ContactServiceV2.getInstance().add(contact)
                    if (response.obj != null) {
                        contact.id = response.obj?.id
                        AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().insertContact(contact)
                        // callBack.invoke(RoomProfileController.END_LOADING)
                        loading.postValue(false)
                    } else {
                        loading.postValue(false)
                        // callBack.invoke(RoomProfileController.END_LOADING)
                    }
                }.exceptionOrNull()?.let {
                    it.printStackTrace()
                    loading.postValue(false)
                    Timber.e("添加联系人错误")
                }
            }
        }
    }

    fun deleteContact() {
        loading.postValue(true)
        room.roomSummary()?.otherMemberIds?.get(0)?.let {
            viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val mContact = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().getContactByMatrixId(it)
                    val response = mContact?.id?.let { it1 -> ContactServiceV2.getInstance().delete(it1) }
                    if (response?.isSuccess == true) {
                        mContact.del = 1
                        AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().update(mContact)
                        // callBack.invoke(RoomProfileController.END_LOADING)
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

    override fun handle(action: RoomComplainAction) {
    }
}
