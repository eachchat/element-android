package im.vector.app.eachchat.contact.manage


import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.blankj.utilcode.util.LogUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.dialogs.GalleryOrCameraDialogHelper
import im.vector.app.core.platform.EmptyViewEvents
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.eachchat.base.EmptyAction
import im.vector.app.eachchat.base.EmptyViewState
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.mycontacts.MyContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.matrix.android.sdk.internal.util.writeToFile
import retrofit2.HttpException
import java.io.File
import java.net.SocketTimeoutException

class ContactManageViewModel @AssistedInject constructor(
        @Assisted initialState: EmptyViewState,
) : VectorViewModel<EmptyViewState, EmptyAction, EmptyViewEvents>(initialState)  {
    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<ContactManageViewModel, EmptyViewState> {
        override fun create(initialState: EmptyViewState): ContactManageViewModel
    }

    companion object : MavericksViewModelFactory<ContactManageViewModel, EmptyViewState> by hiltMavericksViewModelFactory() {
        const val UPLOAD_TIME_OUT = 100

        override fun initialState(viewModelContext: ViewModelContext): EmptyViewState {
            return EmptyViewState(
            )
        }
    }

    val keysEvents = MutableLiveData<VcardEvents>()

    //导出联系人
    fun exportVcard(saveVCardsPath: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            kotlin.runCatching {
                val response = ContactServiceV2.getCustomTimeOutInstance(100).exportVcf()
                val file = File(saveVCardsPath, "contact.vcf")
                val byteStream = response.body()?.byteStream()
                if (response.code() == 200) {
                    keysEvents.postValue(VcardEvents.ExportVcard(true))
                } else {
                    keysEvents.postValue(VcardEvents.ExportVcard(false))
                }
                byteStream?.let { writeToFile(it, file) }
                loading.postValue(false)
            }.exceptionOrNull()?.let {
                loading.postValue(false)
                LogUtils.e("导出失败", it.message)
                keysEvents.postValue(VcardEvents.ExportVcard(false))
            }
        }
    }

    //加载联系人
    fun loadVCards(vCardFile: File) {
        if (!vCardFile.exists()) return
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            runCatching {
                val vCardRequestBody = vCardFile.asRequestBody("text/v-card".toMediaTypeOrNull())
                val vCardPart =
                    MultipartBody.Part.createFormData("fileName", vCardFile.name, vCardRequestBody)
                val response =
                    ContactServiceV2.getCustomTimeOutInstance(UPLOAD_TIME_OUT).uploadVcf(vCardPart)
                if (response.code == 200) {
                    keysEvents.postValue(VcardEvents.ImportVcard(true))
                }
                LogUtils.iTag("contact", response.toString())
                loading.postValue(false)
            }.exceptionOrNull()?.let {
                LogUtils.eTag("contact", it.message)
                loading.postValue(false)
                if (it !is SocketTimeoutException && it !is HttpException) {//filter  SocketTimeoutException
                    keysEvents.postValue(VcardEvents.ImportVcard(false))
                }
            }
        }
    }

    override fun handle(action: EmptyAction) {

    }
}

sealed class VcardEvents(open var consumed: Boolean = false) {
    data class ImportVcard(val success: Boolean) : VcardEvents()
    data class ExportVcard(val success: Boolean) : VcardEvents()
}
