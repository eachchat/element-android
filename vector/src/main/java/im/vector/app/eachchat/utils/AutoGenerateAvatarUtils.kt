package im.vector.app.eachchat.utils


import android.content.Context
import android.graphics.*
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import im.vector.app.R

class AutoGenerateAvatarUtils {
    companion object {
        val backgroundColorList = listOf(
            R.drawable.green_round_bg,
            R.drawable.blue_round_bg,
            R.drawable.purple_round_bg
        )

        @JvmStatic
        fun randomColor(string: String?): Int {
            if (string.isNullOrEmpty()) return backgroundColorList[0]
            var index = string[0].code
            index %= 3
            return backgroundColorList[index]
        }

        @JvmStatic
        fun getFirstChar(string: String?): String {
            if (string.isNullOrEmpty()) return ""
            val firstChar =
                if (string.length > 1 && containsEmoji(string.substring(0, 2))) {
                    string.subSequence(0, 2).toString()
                } else {
                    string.subSequence(0, 1).toString()
                }
            return firstChar
        }

        @JvmStatic
        fun autoGenerateAvatar(string: String?, context: Context): Bitmap {
            val bitmap =
                Bitmap.createBitmap(
                    ScreenUtils.dip2px(context, 50F),
                    ScreenUtils.dip2px(context, 50F),
                    Bitmap.Config.RGB_565
                )//创建一个RGB图
            val canvas = Canvas(bitmap)//初始化画布绘制的图像到icon上
            canvas.drawColor(Color.WHITE)
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)//创建画笔
            circlePaint.color = ContextCompat.getColor(context, randomColor(string))
            canvas.drawCircle(
                ScreenUtils.dip2px(context, 25F).toFloat(),
                ScreenUtils.dip2px(context, 25F).toFloat(),
                ScreenUtils.dip2px(context, 25F).toFloat(), circlePaint
            )
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)//创建画笔
            paint.textSize = ScreenUtils.dip2px(context, 25F).toFloat()//设置文字的大小
            paint.typeface = Typeface.DEFAULT_BOLD//文字的样式(加粗)
            paint.color = Color.WHITE //文字的颜色
            val firstChar = getFirstChar(string)
            canvas.drawText(
                getFirstChar(string),
                ScreenUtils.dip2px(context, 25F).toFloat() - paint.measureText(firstChar) / 2,
                ScreenUtils.dip2px(context, 25F).toFloat() + paint.measureText(firstChar) / 2, paint
            )//文字在图层上的初始位置
            canvas.save()//保存所有图层
            canvas.restore()
            return bitmap
        }


        @JvmStatic
        fun containsEmoji(source: String): Boolean {
            val len = source.length
            val isEmoji = false
            for (i in 0 until len) {
                val hs = source[i]
                if (hs.code in 0xd800..0xdbff) {
                    if (source.length > 1) {
                        val ls = source[i + 1]
                        val uc = (hs.code - 0xd800) * 0x400 + (ls.code - 0xdc00) + 0x10000
                        if (uc in 0x1d000..0x1f77f) {
                            return true
                        }
                    }
                } else {
                    // non surrogate
                    if (hs.code in 0x2100..0x27ff && hs.code != 0x263b) {
                        return true
                    } else if (hs.code in 0x2B05..0x2b07) {
                        return true
                    } else if (hs.code in 0x2934..0x2935) {
                        return true
                    } else if (hs.code in 0x3297..0x3299) {
                        return true
                    } else if (hs.code == 0xa9 || hs.code == 0xae || hs.code == 0x303d || hs.code == 0x3030 || hs.code == 0x2b55 || hs.code == 0x2b1c || hs.code == 0x2b1b || hs.code == 0x2b50 || hs.code == 0x231a) {
                        return true
                    }
                    if (!isEmoji && source.length > 1 && i < source.length - 1) {
                        val ls = source[i + 1]
                        if (ls.code == 0x20e3) {
                            return true
                        }
                    }
                }
            }
            return isEmoji
        }

        @BindingAdapter(value = ["autoGenerateContactRoomAvatar", "avatarUrl"], requireAll = false)
        @JvmStatic
        fun autoGenerateContactRoomAvatar(tv: TextView, name: String?, avatarUrl: String?) {
            if (!avatarUrl.isNullOrBlank()) return
            if (name.isNullOrBlank()) return
            tv.visibility = View.VISIBLE
            tv.setBackgroundResource(randomColor(name))
            tv.text = getFirstChar(name)
        }
    }
}
