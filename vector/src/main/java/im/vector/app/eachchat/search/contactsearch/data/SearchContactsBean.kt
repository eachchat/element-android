package im.vector.app.eachchat.search.contactsearch.data

import android.content.Intent
import im.vector.app.eachchat.contact.api.BaseConstant
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.department.data.IDisplayBean

/**
 * Created by chengww on 2020/11/6
 * @author chengww
 */
class SearchContactsBean(val contact: ContactsDisplayBean): IDisplayBean {

    private var intent: Intent? = null
    var matrixUserAvatar: String? = ""
    var contactBase64Avatar: String? = contact.avatar
    var contactUrlAvatar: String? = contact.avatarUrl
    override fun getItemType() = BaseConstant.SEARCH_CONTACT_TYPE // AppConstant.CONTACT_TYPE
    override fun getAvatar() = contact.avatar
    override fun getMainContent() = contact.mainTitle
    override fun getMinorContent() = contact.subTitle
    override fun getId() = contact.contactId
    override fun getCount() = 0
    override fun getType() = BaseConstant.SEARCH_CONTACT_TYPE
    override fun getExtra() = intent
}
