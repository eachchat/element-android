package im.vector.app.eachchat.version_update

import android.content.Context
import android.os.Parcelable
import im.vector.app.BuildConfig
import im.vector.app.eachchat.utils.CommonUtils
import kotlinx.parcelize.Parcelize

/**
 * Created by chengww on 2020/11/10
 * @author chengww
 */
@Parcelize
data class VersionUpdateResult(
        var verCode: String?,
        var verName: String?,
        var downloadUrl: String?,
        var updateTime: Long?,
        var md5Hash: String?,
        var forceUpdate: Int?, // 1 force update, 0 false
        var description: String?) : Parcelable {

    constructor() : this(null, null, null, null, null, null, null)

    fun needUpdate(): Boolean = CommonUtils.isUpdate(BuildConfig.VERSION_NAME,verCode ?: "0")

    /**
     * Is current version force update,
     * <strong>not</strong> represents user need force update.
     * If check whether need force update, check [needUpdate] first.
     */
    val isForceUpdate: Boolean
        get() = forceUpdate ?: 0 > 0
}
