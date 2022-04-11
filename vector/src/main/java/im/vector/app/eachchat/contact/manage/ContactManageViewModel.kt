package im.vector.app.eachchat.contact.manage


import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.Utils
import com.huawei.hms.framework.common.IoUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.dialogs.GalleryOrCameraDialogHelper
import im.vector.app.core.platform.EmptyViewEvents
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.base.EmptyAction
import im.vector.app.eachchat.base.EmptyViewState
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.mycontacts.MyContactViewModel
import im.vector.lib.multipicker.entity.MultiPickerFileType
import im.vector.lib.multipicker.utils.getColumnIndexOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.BufferedSink
import okio.source
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

    private val REQUEST_CODE_ANDROID_11_OR_HIGHER_FILE_SELECT_FOR_OUTPUT = 5

    val keysEvents = MutableLiveData<VcardEvents>()

    //导出联系人
    fun exportVcard(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            kotlin.runCatching {
                val response = ContactServiceV2.getCustomTimeOutInstance(100).exportVcf()
//                val file = UriUtils.uri2File(uri)
                val byteStream = response.body()?.byteStream()
                if (response.code() == 200) {
                    keysEvents.postValue(VcardEvents.ExportVcard(true))
                } else {
                    keysEvents.postValue(VcardEvents.ExportVcard(false))
                }
                val out = BaseModule.getContext().contentResolver.openOutputStream(uri);
                byteStream?.let { out?.write(it.readBytes()) }
                loading.postValue(false)
            }.exceptionOrNull()?.let {
                loading.postValue(false)
                LogUtils.e("导出失败", it.message)
                keysEvents.postValue(VcardEvents.ExportVcard(false))
            }
        }
    }

    //加载联系人
    fun loadVCards(uri: Uri) {
        val vCardFile = UriUtils.uri2File(uri)
        if (!vCardFile.exists()) return
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            runCatching {
                val vCardRequestBody = uri.asRequestBody("text/v-card".toMediaTypeOrNull())
                val vCardPart =
                    MultipartBody.Part.createFormData("fileName", uri.getFileName(), vCardRequestBody)
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

fun Uri.asRequestBody(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {
        override fun contentType() = contentType

        override fun contentLength() = BaseModule.getContext().contentResolver.query(this@asRequestBody, null, null, null, null)
                ?.use { cursor ->
                    var size: Long = 0L
                    val sizeColumn = cursor.getColumnIndexOrNull(OpenableColumns.SIZE) ?: return@use null
                    if (cursor.moveToFirst()) {
                        size = cursor.getLongOrNull(sizeColumn) ?: 0L
                    }
                    return@use  size
                }?: 0L

        override fun writeTo(sink: BufferedSink) {
            val input = BaseModule.getContext().contentResolver.openInputStream(this@asRequestBody)
            input?.readBytes()?.let { sink.write(it) }
        }
    }
}

fun Uri.getFileName(): String = BaseModule.getContext().contentResolver.query(this, null, null, null, null)
            ?.use { cursor ->
                var name = ""
                val nameColumn = cursor.getColumnIndexOrNull(OpenableColumns.DISPLAY_NAME) ?: return@use null
                if (cursor.moveToFirst()) {
                    name = cursor.getStringOrNull(nameColumn) ?: ""
                }
                return  name
            }?: ""


