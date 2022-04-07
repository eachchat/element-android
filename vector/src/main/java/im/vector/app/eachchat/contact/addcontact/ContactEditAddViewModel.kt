package im.vector.app.eachchat.contact.addcontact

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
import im.vector.app.eachchat.contact.addcontact.ContactEditAddActivity.Companion.MODE_ADD
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.contact.mycontacts.MyContactViewModel
import im.vector.app.eachchat.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.MatrixPatterns
import org.matrix.android.sdk.api.session.user.model.User

/**
 * Created by chengww on 2020/10/30
 * @author chengww
 */

class ContactEditAddViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState) {
    val local = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2()
    val contactEditAddStatues = MutableLiveData<Int>()
    val contactLiveData = MutableLiveData<ContactsDisplayBeanV2?>()
    val session = BaseModule.getSession()

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<ContactEditAddViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): ContactEditAddViewModel
    }

    companion object : MavericksViewModelFactory<ContactEditAddViewModel, EmptyViewState> by hiltMavericksViewModelFactory() {
        const val ADD_SUCCESS = 1
        const val ADD_FAIL = 2
        const val EDIT_SUCCESS = 3
        const val EDIT_FAIL = 4
        const val REPEAT_MATRIX_ID = 5
        const val INVALID_MATRIX_ID = 6
        const val UNKNOWN_HOST_EXCEPTION = 7

        override fun initialState(viewModelContext: ViewModelContext): EmptyViewState {
            return EmptyViewState(
            )
        }
    }

    fun getContact(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            contactLiveData.postValue(local.getContactByContactId(id))
        }
    }

    fun addContact(contact: ContactsDisplayBeanV2) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                contact.matrixId?.let { matrixId ->
                    if (matrixId.isEmpty()) return@let
                    //check has same matrixId in local database
                    val contactFound = ContactDaoHelper.getInstance().getContactByMatrixId(matrixId)
                    if (contactFound != null) {
                        contactEditAddStatues.postValue(REPEAT_MATRIX_ID)
                        return@runCatching
                    }
                    //is matrix Id formatted
                    if (!MatrixPatterns.isUserId(matrixId)) {
                        contactEditAddStatues.postValue(INVALID_MATRIX_ID)
                        return@runCatching
                    }
                    //get matrix user
                    getMatrixUser(matrixId)?.let {
                        contact.photoUrl = it.avatarUrl
                        if (contact.nickName.isNullOrEmpty()) {
                            contact.nickName = it.displayName
                        }
                        if (contact.family.isNullOrEmpty()) {
                            contact.family = it.displayName
                        }
                    }
                }
                val response = ContactServiceV2.getInstance().add(contact)
                if (response.code == 200) {
                    local.insertContact(response.obj)
//                    Contact.contactInfoActivityV2(response.obj?.id)
                    contactEditAddStatues.postValue(ADD_SUCCESS)
                } else
                    contactEditAddStatues.postValue(ADD_FAIL)
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                contactEditAddStatues.postValue(ADD_FAIL)
            }
        }
    }

    fun updateContact(contact: ContactsDisplayBeanV2, needCheckRepeatId: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                contact.matrixId?.let { matrixId ->
                    if (matrixId.isEmpty()) return@let
                    //check has same matrixId in local database
                    if (needCheckRepeatId) {
                        val contactFound =
                                ContactDaoHelper.getInstance().getContactByMatrixId(matrixId)
                        if (contactFound != null) {
                            contactEditAddStatues.postValue(REPEAT_MATRIX_ID)
                            return@runCatching
                        }
                        if (!MatrixPatterns.isUserId(matrixId)) {
                            contactEditAddStatues.postValue(INVALID_MATRIX_ID)
                            return@runCatching
                        }
                        //check is the matrixId valid
                        getMatrixUser(matrixId)?.let {
                            contact.photoUrl = it.avatarUrl
                            if (contact.nickName.isNullOrEmpty()) {
                                contact.nickName = it.displayName
                            }
                            if (contact.family.isNullOrEmpty()) {
                                contact.family = it.displayName
                            }
                        }
                    }
                }
                val response = ContactServiceV2.getInstance().edit(contact)
                if (response.code == 200) {
                    response.obj?.let {
                        local.insertContact(contact)
                        contactEditAddStatues.postValue(EDIT_SUCCESS)
                    }
                } else
                    contactEditAddStatues.postValue(EDIT_FAIL)
            }.exceptionOrNull()?.let {
                it.printStackTrace()
                contactEditAddStatues.postValue(EDIT_FAIL)
            }
        }
    }

    private fun getMatrixUser(matrixId: String): User? {
        kotlin.runCatching {
            session?.let { session ->
                return session.getUser(matrixId)
            }
        }
        return null
    }

    val mode = MutableLiveData(MODE_ADD)



    override fun handle(action: EmptyAction) {

    }
}
