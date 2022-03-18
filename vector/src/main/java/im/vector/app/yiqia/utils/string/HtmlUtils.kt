package im.vector.app.yiqia.utils.string

import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat

object HtmlUtils {

    @JvmStatic
    fun String.fromHtml(): Spanned {
         return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}
