package im.vector.app.eachchat.complain.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by zhouguanjie on 2021/4/15.
 */
@Parcelize
data class ReportBean(val type: String,
                      var isChecked: Boolean = false) : Parcelable
