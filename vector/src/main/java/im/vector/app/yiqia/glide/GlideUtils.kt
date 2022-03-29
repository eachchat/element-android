import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import im.vector.app.R
import im.vector.app.core.glide.GlideApp
import im.vector.app.yiqia.contact.data.resolveMxc

object GlideUtils {
    /**
     * 加载圆形图片
     *
     * @param context  context
     * @param iv       imageView
     * @param url      图片地址
     * @param emptyImg 默认展位图
     */
    @JvmStatic
    fun loadCircleImage(iv: ImageView?, url: String?, emptyImg: Int = R.drawable.default_person_icon) {
        val context = iv?.context
        if (context is Activity && context.isDestroyed && context.isFinishing)
            return
        kotlin.runCatching {
            GlideApp.with(iv?.context!!)
                    .load(url.resolveMxc())
                    .error(emptyImg)
                    .placeholder(emptyImg)
                    .transform(CircleCrop()).into(iv)
        }
    }

    @JvmStatic
    fun loadCircleBase64Image(context: Context?, iv: ImageView?, base64: String?) {
        if (context is Activity && context.isDestroyed && context.isFinishing)
            return
        kotlin.runCatching {
            GlideApp.with(context!!)
                    .load("data:image/jpg;base64,$base64")
                    .error(R.drawable.default_person_icon)
                    .placeholder(R.drawable.default_person_icon)
                    .transform(CircleCrop()).into(iv!!)
        }
    }

    @JvmStatic
    fun loadDefaultCircleImage(iv: ImageView) {
        GlideApp.with(iv.context)
                .load(R.drawable.default_person_icon)
                .transform(CircleCrop()).into(iv)
    }
}
