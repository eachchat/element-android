package im.vector.app.eachchat.widget.bot

import GlideUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import im.vector.app.R
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.contact.data.resolveMxc
import im.vector.app.eachchat.department.ContactsHeaderHolder
import im.vector.app.eachchat.ui.stickyHeader.StickyHeaderAdapter
import im.vector.app.eachchat.utils.string.HtmlUtils.fromHtml
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class BotAdapter(
        layoutResId: Int = R.layout.item_my_contacts_layout,
        data: MutableList<User?>? = ArrayList()
) : BaseQuickAdapter<User?, BaseViewHolder>(layoutResId, data){

    @OptIn(DelicateCoroutinesApi::class)
    override fun convert(holder: BaseViewHolder, item: User?) {
        if (item == null) return
        val imageView = holder.getView<ImageView>(R.id.avatar_iv)
        when {
            !item.contactBase64Avatar.isNullOrEmpty() -> {
                GlideUtils.loadCircleBase64Image(imageView.context, imageView, item.contactBase64Avatar)
            }
            else -> {
                GlideUtils.loadCircleImage(imageView, item.contactUrlAvatar)
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val user = item.matrixId?.let { BaseModule.getSession()?.getUser(it) }
                GlobalScope.launch(Dispatchers.Main) {
                    if (!user?.avatarUrl.isNullOrBlank()) {
                        GlideUtils.loadCircleImage(imageView, user?.avatarUrl.resolveMxc())
                    } else {
                        when {
                            !item.contactBase64Avatar.isNullOrEmpty() -> {
                                GlideUtils.loadCircleBase64Image(imageView.context, imageView, item.contactBase64Avatar)
                            }
                            else -> {
                                GlideUtils.loadCircleImage(imageView, item.contactUrlAvatar)
                            }
                        }
                    }
                }
            }.exceptionOrNull()?.printStackTrace()
        }
        if (item.displayName?.isNotEmpty() == true) {
            holder.setText(R.id.name_tv, item.displayName?.fromHtml())
        } else {
            holder.setText(R.id.name_tv, "")
        }
        if (!item.userTitle.isNullOrEmpty()) {
            holder.setText(R.id.title_tv, item.userTitle)
            holder.setVisible(R.id.title_tv, true)
        } else {
            holder.setText(R.id.title_tv, "")
            holder.setVisible(R.id.title_tv, false)
        }

        if (holder.absoluteAdapterPosition == data.size + headerLayoutCount - 1) {
            holder.setVisible(R.id.divider_view, false)
        } else {
            holder.setVisible(R.id.divider_view, false)
        }
    }

    fun getPosByIndex(c: Char): Int {
        for (index in data.indices) {
            if (c == data[index]?.firstChar) {
                return index + headerLayoutCount
            }
        }
        return -1
    }
}
