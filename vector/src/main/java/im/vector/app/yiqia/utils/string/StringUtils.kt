package im.vector.app.yiqia.utils.string

import android.text.Spanned
import im.vector.app.yiqia.utils.string.HtmlUtils.fromHtml

object StringUtils {
    @JvmStatic
    fun List<String>.highlightKeyword(keyword: String): List<Spanned> {
        val highlightList = ArrayList<Spanned>()
        this.forEach {
            highlightList.add(it.getKeywordStr(keyword).fromHtml())
        }
        return highlightList
    }

    @JvmStatic
    private fun String.getKeywordStr(keyword: String?): String {
        if (this.isEmpty()) {
            return ""
        }
        if (keyword.isNullOrEmpty()) {
            return this
        }
        val startIndex = this.indexOf(keyword, 0, true)
        if (startIndex < 0) {
            return this
        }
        val str = this.substring(startIndex, startIndex + keyword.length)

        return this.replace(str, "<font color='#00B368'>$str</font>", false)
    }
}
