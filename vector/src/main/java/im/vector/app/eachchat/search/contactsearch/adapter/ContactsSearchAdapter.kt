package ai.workly.eachchat.android.search.adapter

import GlideUtils
import GlideUtils.loadCircleImage
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import im.vector.app.R
import im.vector.app.eachchat.department.ContactUtils
import im.vector.app.eachchat.department.data.IDisplayBean
import im.vector.app.eachchat.search.contactsearch.data.SearchContactsBean
import im.vector.app.eachchat.search.contactsearch.data.SearchUserBean
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.utils.AutoGenerateAvatarUtils
import im.vector.app.eachchat.utils.string.HtmlUtils.fromHtml
import im.vector.app.eachchat.utils.string.StringUtils.getKeywordStr

class ContactsSearchAdapter :
    BaseMultiItemQuickAdapter<ContactsSearchAdapter.BaseItem, BaseViewHolder>(null) {

    private var keyword: String = ""

    companion object {
        const val SUB_TYPE_CLOSE_CONTACT = 111
        const val SUB_TYPE_MY_CONTACT = 222
        const val SUB_TYPE_ORG = 333
        const val SUB_TYPE_DEPARTMENT = 444
        const val TYPE_HEADER = 555
        const val TYPE_FOOTER = 666
        const val TYPE_CONTENT = 777
        const val TYPE_GAP = 888
        const val SUB_TYPE_GROUP_CHAT = 999
        const val TYPE_SEARCH_CONTACT_ONLINE = 1010
        const val SEARCH_MULTI_TYPE = 100

        const val BODY: String = "body"
        const val TYPE_CHAT_RECORD: Int = 1212
    }

    init {
        addItemType(TYPE_HEADER, R.layout.item_contacts_search_header_layout)
        addItemType(TYPE_FOOTER, R.layout.item_contacts_search_footer_layout)
        addItemType(TYPE_CONTENT, R.layout.item_contacts_search_content_layout)
        addItemType(TYPE_GAP, R.layout.item_contacts_search_gap_layout)
        addItemType(TYPE_SEARCH_CONTACT_ONLINE, R.layout.layout_search_user_online)
    }

    fun setKeyword(keyWord: String) {
        this.keyword = keyWord
    }

    override fun convert(helper: BaseViewHolder, item: BaseItem?) {
        if (item == null) return
        when {
            item.itemType == TYPE_HEADER && item is HeaderItem -> bindHeaderItem(helper, item)
            item.itemType == TYPE_FOOTER && item is FooterItem -> bindFooterItem()
            item.itemType == TYPE_CONTENT && item is ContentItem -> bindContentItem(helper, item)
            item.itemType == TYPE_GAP && item is GapItem -> bindGapItem()
            item.itemType == TYPE_SEARCH_CONTACT_ONLINE && item is SearchContactOnlineItem ->
                bindSearchContactOnlineItem(helper, item)
        }
    }

    private fun bindHeaderItem(helper: BaseViewHolder, item: HeaderItem) {
        when (item.subType) {
            SUB_TYPE_CLOSE_CONTACT -> helper.setText(
                R.id.header_tv,
                mContext.getString(R.string.most_frequently_contacted)
            )
            SUB_TYPE_MY_CONTACT -> helper.setText(
                R.id.header_tv,
                mContext.getString(R.string.my_contacts)
            )
            SUB_TYPE_ORG -> {
                if (AppCache.getIsOpenOrg()) {
                    helper.setText(
                            R.id.header_tv,
                            mContext.getString(R.string.organization_framework)
                    )
                } else {
                    helper.setText(
                            R.id.header_tv,
                            mContext.getString(R.string.team)
                    )
                }
            }
            SUB_TYPE_DEPARTMENT -> helper.setText(
                R.id.header_tv,
                mContext.getString(R.string.department)
            )
            SUB_TYPE_GROUP_CHAT -> helper.setText(
                R.id.header_tv,
                mContext.getString(R.string.group_chat)
            )
        }
    }

    private fun bindFooterItem() {
        // do nothing
    }

    private fun bindContentItem(helper: BaseViewHolder, contentItem: ContentItem) {
        val item = contentItem.item
        helper.setVisible(R.id.avatar_tv, false)
        if (contentItem.subType == SUB_TYPE_DEPARTMENT) {
            item?.let {
                helper.setImageResource(
                        R.id.avatar_iv,
                        ContactUtils.getDepartmentIcon(it)
                )
            }
        } else if (contentItem.subType == SUB_TYPE_MY_CONTACT) {
            val imageView =
                helper.getView<ImageView>(R.id.avatar_iv)
            val searContactsBean = item as SearchContactsBean
            //first load matrix avatar
            if (!searContactsBean.matrixUserAvatar.isNullOrEmpty()) {
                loadCircleImage(
                    imageView,
                    searContactsBean.matrixUserAvatar
                )
                //then load contact base64 avatar
            } else if (!searContactsBean.contactBase64Avatar.isNullOrEmpty()) {
                GlideUtils.loadCircleBase64Image(
                    imageView.context,
                    imageView,
                    searContactsBean.contactBase64Avatar
                )
                //finally contact url avatar
            } else {
                loadCircleImage(
                    imageView,
                    searContactsBean.contactUrlAvatar
                )
            }
        } else if (contentItem.subType == SUB_TYPE_CLOSE_CONTACT || contentItem.subType == SUB_TYPE_ORG) {
            val imageView =
                helper.getView<ImageView>(R.id.avatar_iv)
            val searUserBean = item as SearchUserBean
            if (!searUserBean.avatar.isNullOrEmpty()) {
                loadCircleImage(
                    imageView,
                    searUserBean.avatar
                )
                //then load contact base64 avatar
            } else if (!searUserBean.contactBase64Avatar.isNullOrEmpty()) {
                GlideUtils.loadCircleBase64Image(
                    imageView.context,
                    imageView,
                    searUserBean.contactBase64Avatar
                )
                //finally contact url avatar
            } else {
                loadCircleImage(
                    imageView,
                    searUserBean.contactUrlAvatar
                )
            }
        } else if (contentItem.subType == SUB_TYPE_GROUP_CHAT) {
            val avatarText = helper.getView<TextView>(R.id.avatar_tv)
            if (item?.avatar.isNullOrBlank()) {
                helper.setVisible(R.id.avatar_tv, true)
                AutoGenerateAvatarUtils.autoGenerateContactRoomAvatar(
                    avatarText,
                    item?.mainContent,
                    item?.avatar
                )
            } else {
                loadCircleImage(helper.getView(R.id.avatar_iv), item?.avatar)
            }
        }
        else {
            loadCircleImage(helper.getView(R.id.avatar_iv), item?.avatar)
        }
        if (!item?.mainContent.isNullOrEmpty()) {
            helper.setVisible(R.id.name_tv, true)
            helper.setText(
                R.id.name_tv,
                getKeywordStr(item?.mainContent, keyword, "#00B368").fromHtml()
            )
        } else {
            helper.setVisible(R.id.name_tv, false)
        }
        if (contentItem.subType == SUB_TYPE_DEPARTMENT || contentItem.subType == SUB_TYPE_GROUP_CHAT) {
            helper.setGone(R.id.title_tv, false)
        } else {
            if (!item?.minorContent.isNullOrEmpty()) {
                helper.setVisible(R.id.title_tv, true)
                helper.setText(R.id.title_tv, item?.minorContent)
            } else {
                helper.setVisible(R.id.title_tv, false)
            }
        }

        if (helper.absoluteAdapterPosition == data.size - 1) {
            helper.setVisible(R.id.div_line, false)
        } else {
            if (helper.absoluteAdapterPosition < data.size && data[helper.absoluteAdapterPosition].itemType == TYPE_FOOTER || data[helper.absoluteAdapterPosition].itemType == TYPE_GAP || data[helper.absoluteAdapterPosition + 1].itemType == TYPE_CONTENT || helper.absoluteAdapterPosition == data.size - 1) {
                helper.setVisible(R.id.div_line, true)
            } else {
                helper.setVisible(R.id.div_line, false)
            }
        }
    }

    private fun bindGapItem() {
        // do nothing
    }

    private fun bindSearchContactOnlineItem (helper: BaseViewHolder, contentItem: SearchContactOnlineItem) {
        val tvKeyWord = helper.getView<TextView>(R.id.search_online_id_tv)
        tvKeyWord.text = contentItem.keyWord
    }

    open class BaseItem(private val type: Int) : MultiItemEntity {
        override fun getItemType(): Int {
            return type
        }
    }

    data class HeaderItem(val type: Int, val subType: Int) : BaseItem(type)
    data class FooterItem(val type: Int, val subType: Int) : BaseItem(type)
    data class ContentItem(val type: Int, val subType: Int, val item: IDisplayBean?) :
        BaseItem(type)
    data class SearchContactOnlineItem(val type: Int, val keyWord: String) :
        BaseItem(type)

    data class GapItem(val type: Int, val subType: Int) : BaseItem(type)

}
