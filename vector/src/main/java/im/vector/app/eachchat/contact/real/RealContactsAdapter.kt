package im.vector.app.eachchat.contact.real

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import im.vector.app.R
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.utils.string.HtmlUtils.fromHtml

class RealContactsAdapter(
        layoutResId: Int = R.layout.item_real_contacts_layout,
        data: MutableList<User>? = ArrayList()
) : BaseQuickAdapter<User, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: User?) {
        if (item == null) return
        val ivAvatar = helper.getView<ImageView>(R.id.avatar_iv)
        if (!item.avatarTUrl.isNullOrEmpty()) {
            GlideUtils.loadCircleImage(ivAvatar, item.avatarTUrl)
        } else if (!item.contactBase64Avatar.isNullOrEmpty()) {
            GlideUtils.loadCircleBase64Image(mContext, ivAvatar, item.contactBase64Avatar)
        } else if (!item.contactUrlAvatar.isNullOrEmpty()) {
            GlideUtils.loadCircleImage(ivAvatar, item.contactUrlAvatar)
        } else {
            GlideUtils.loadDefaultCircleImage(ivAvatar)
        }
        helper.setText(R.id.name_tv, item.displayName?.fromHtml())
        if (!item.userTitle.isNullOrEmpty()) {
            helper.setText(R.id.title_tv, item.userTitle)
            helper.setVisible(R.id.title_tv, true)
        } else {
            helper.setText(R.id.title_tv, "")
            helper.setGone(R.id.title_tv, false)
        }
    }
}
