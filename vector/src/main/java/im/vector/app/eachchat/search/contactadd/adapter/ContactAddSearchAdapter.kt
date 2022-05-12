package im.vector.app.eachchat.search.contactadd.adapter

import android.os.Build
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import im.vector.app.R
import im.vector.app.eachchat.base.BaseModule

import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.utils.string.HtmlUtils.fromHtml

/**
 * Created by chengww on 2020/10/27
 * @author chengww
 */
class ContactAddSearchAdapter(data: MutableList<ContactsDisplayBean>? = ArrayList(), layoutResId: Int = R.layout.item_contact_add)
    : BaseQuickAdapter<ContactsDisplayBean?, BaseViewHolder>(layoutResId, data?.toList()) {
    override fun convert(helper: BaseViewHolder?, item: ContactsDisplayBean?) {
        val imageView = helper?.itemView?.findViewById<ImageView>(R.id.iv_avatar)
        val tvUserName = helper?.itemView?.findViewById<TextView>(R.id.tv_user_name)
        val tvUserSubText = helper?.itemView?.findViewById<TextView>(R.id.tv_user_subtext)
        val btnAddContacts = helper?.itemView?.findViewById<TextView>(R.id.btn_add_contacts)
        helper?.addOnClickListener(R.id.btn_add_contacts);
        if (imageView != null) {
            GlideUtils.loadCircleImage(imageView, item?.avatar)
        }
        tvUserName?.text = item?.mainTitle?.fromHtml()
        tvUserSubText?.text = item?.subTitle?.fromHtml()
        btnAddContacts?.isEnabled = item?.contactAdded != true
        if (item?.contactAdded == true) {
            btnAddContacts?.text = BaseModule.getContext().getString(R.string.contacts_added)
        } else {
            btnAddContacts?.text = BaseModule.getContext().getString(R.string.contacts_add)
        }
    }
}

