package im.vector.app.eachchat.utils.string

import android.text.Spanned
import androidx.core.text.HtmlCompat

object HtmlUtils {
    @JvmStatic
    fun String.fromHtml(): Spanned {
         return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}
