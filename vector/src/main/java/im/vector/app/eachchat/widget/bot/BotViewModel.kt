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

package im.vector.app.eachchat.widget.bot

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
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.widget.bot.data.Bot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BotViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {

    var contactData = MutableLiveData<List<User>>()
    val indexChars = MutableLiveData<List<Char>>()
    var offset = 0 // 将offset设为全局变量，只在第一次进入时分页读取
    var directlyUpdateContactsLiveData = MutableLiveData<List<User?>?>()

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<BotViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): BotViewModel
    }

    companion object : MavericksViewModelFactory<BotViewModel, EmptyViewState> by hiltMavericksViewModelFactory() {
        private const val PAGE_LIMIT = 100

        override fun initialState(viewModelContext: ViewModelContext): EmptyViewState {
            return EmptyViewState(
            )
        }
    }

    fun loadMyContactsOffset() {
        loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val count =
                    AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().getContactsCount()
            val users = mutableListOf<User>()
            while (count - offset >= 0) {
                //分页查询联系人数据库
                val contacts = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2()
                        .getContactsLimitOffset(offset, PAGE_LIMIT)
                offset += PAGE_LIMIT

                for (it in contacts) {
                    if (it.del != 1) {
                        val user = it.toContactsDisplayBean().toUser()
                        user.displayName = it.displayName
                        user.userTitle = it.displayTitle
                        users.add(user)
                    }
                }
                users.sort()

                val chars = mutableListOf<Char>()
                users.forEach {
                    if (chars.size > 27) return@forEach
                    val character: Char = it.firstChar.uppercaseChar()
                    if (!chars.contains(character)) {
                        chars.add(character)
                    }
                }
                //stop load when completely loaded
                if (contactData.value != null) {
                    if (contactData.value!!.size > users.size) {
                        return@launch
                    }
                }
                indexChars.postValue(chars)
                contactData.postValue(users.toList())
            }
            loading.postValue(false)
        }
    }

    fun loadBot(bots: List<Bot?>?) {
        if (bots == null) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            //用于清理垃圾数据
//            contacts.forEach {
//                it.id?.let { it1 -> ContactServiceV2.getInstance().delete(it1) }
//            }
            val users = mutableListOf<User>()
            for (it in bots) {
                if (it?.appMatrixId == null) continue
                val matrixUser = BaseModule.getSession().getUser(it.appMatrixId!!) ?: continue
                val user = User(displayName = matrixUser.displayName, matrixId = matrixUser.userId, avatarOUrl = matrixUser.avatarUrl, avatarTUrl = matrixUser.avatarUrl, userTitle = it.description)
                users.add(user)
            }
            users.sort()

            loadIndex(users)
            contactData.postValue(users.toList())
            loading.postValue(false)
        }
    }

    fun loadIndex(users: MutableList<User>) {
        val chars = mutableListOf<Char>()
        users.forEach {
            if (chars.size > 27) return@forEach
            val character: Char = it.firstChar.uppercaseChar()
            if (!chars.contains(character)) {
                chars.add(character)
            }
        }
        indexChars.postValue(chars)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun directlyLoadContact(contactId: String, users: MutableList<User?>) {
        GlobalScope.launch(Dispatchers.IO) {
            val contact = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2()
                    .getContactByContactId(contactId) ?: return@launch
            var index = 0
            val data = ArrayList<User>()
            data.addAll(users.filterNotNull())
            while (index < data.size - 1) {
                val user = data[index]
                index++
                if (user.contactId == contactId) {
                    val contactToUser = contact.toContactsDisplayBean().toUser()
                    user.contactBase64Avatar = contactToUser.contactBase64Avatar
                    user.contactUrlAvatar = contactToUser.contactUrlAvatar
                    user.displayName = contactToUser.displayName
                    user.userTitle = contactToUser.userTitle
                    user.displayNamePy = contactToUser.displayNamePy
                    break
                }
            }
            data.sort()
            loadIndex(data)
            directlyUpdateContactsLiveData.postValue(data)
        }
    }

    override fun handle(action: EmptyAction) {
    }
}
