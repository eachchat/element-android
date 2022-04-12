package im.vector.app.eachchat.utils.string

import android.text.Spanned
import im.vector.app.eachchat.utils.string.HtmlUtils.fromHtml

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

    @JvmStatic
    //calculate Chinese char number
    fun calcChineseChar(charSequence: CharSequence?): Int {
        if (charSequence.isNullOrEmpty()) {
            return 0
        }

        var sum = 0
        for (c in charSequence) {
            sum += getCharTextCount(c)
        }
        return sum
    }

    @JvmStatic
    private fun getCharTextCount(c: Char) = if (isChinese(c)) 1 else 0

    @JvmStatic
    fun isChinese(c: Char): Boolean {
        return c.code in 0x4E00..0x9FA5
    }

    fun getKeywordStr(oldStr: String?, keyword: String?, highlightColor: String = "#00B368"): String {
        if (oldStr.isNullOrEmpty()) {
            return ""
        }
        if (keyword.isNullOrEmpty()) {
            return oldStr
        }
        val startIndex = oldStr.indexOf(keyword, 0, true)
        if (startIndex < 0) {
            return oldStr
        }
        val str = oldStr.substring(startIndex, startIndex + keyword.length)

        return oldStr.replace(str, "<font color='$highlightColor'>$str</font>", false)
    }
}
