package im.vector.app.eachchat.search.chatRecord


import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import im.vector.app.R
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.resolveMxc
import im.vector.app.eachchat.search.chatRecord.SearchType.DirectGroupName
import im.vector.app.eachchat.search.chatRecord.SearchType.GroupMessage
import im.vector.app.eachchat.search.chatRecord.SearchType.GroupMessageCount
import im.vector.app.eachchat.search.chatRecord.SearchType.GroupName
import im.vector.app.eachchat.search.contactsearch.data.SearchData
import im.vector.app.eachchat.search.contactsearch.searchmore.SearchMoreActivity
import im.vector.app.eachchat.search.contactsearch.searchmore.SearchMoreActivity.Companion.SEARCH_MORE_TYPE_CHAT_RECORD
import im.vector.app.eachchat.utils.DateUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by zhouguanjie on 2021/1/4.
 */
class SearchAllAdapter(layoutResId: Int = R.layout.search_all_item,
                       data: MutableList<SearchData>? = ArrayList())
    : BaseQuickAdapter<SearchData, BaseViewHolder>(layoutResId, data) {

    var keyword: String? = null

    var showFooter: Boolean = true

    var showSendTime: Boolean = false

    var openRoomCallback: ((String, String)->Unit)? = null

    var count: Int = 20

    override fun convert(helper: BaseViewHolder, item: SearchData) {
        val mainTV = helper.getView<TextView>(R.id.main_tv)
        if ((item.type == GroupName || item.type == DirectGroupName) && item.isEncrypted) {
            helper.setTextColor(R.id.main_tv, ContextCompat.getColor(mContext, R.color.greenDark))
            mainTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.e2e_icon, 0, 0, 0)
        } else {
            helper.setTextColor(R.id.main_tv, ContextCompat.getColor(mContext, R.color.black))
            mainTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        helper.setText(R.id.main_tv, HtmlCompat.fromHtml(item.mainTitle, HtmlCompat.FROM_HTML_MODE_LEGACY))
        if (!item.minor.isNullOrEmpty()) {
            helper.setText(R.id.minor_tv, HtmlCompat.fromHtml(item.minor, HtmlCompat.FROM_HTML_MODE_LEGACY))
            helper.setGone(R.id.minor_tv, true)
        } else {
            helper.setGone(R.id.minor_tv, false)
        }
        val avatarImageView = helper.getView<ImageView>(R.id.avatar)
        var defaultAvatarId = R.drawable.default_room_icon
        if (item.isDirect) {
            defaultAvatarId = R.drawable.default_person_icon
        }
        GlideUtils.loadCircleImage(avatarImageView, item.avatar?.resolveMxc(), defaultAvatarId)

        helper.setGone(R.id.message_time_tv, showSendTime)
        if (showSendTime) {
            if (item.time != null) {
                helper.setText(R.id.message_time_tv, DateUtils.getPrettyDatePattern(mContext, item.time.toString()))
            } else {
                helper.setText(R.id.message_time_tv, "")
            }
        }

        setTitle(helper, item)
        if (showFooter) {
            setFooter(helper, item)
        }
        val view = helper.getView<View>(R.id.content)
        view.setOnClickListener {
             if (item.type == GroupMessageCount) {
                if (item.count == 1) {
//                    ARouter.getInstance()
//                            .build(RoutePath.CHAT_DETAIL)
//                            .withString(BaseConstant.KEY_ROOM_ID, item.id)
//                            .withString(BaseConstant.KEY_TAGERT_EVENT_ID, item.targetId)
//                            .withString(BaseConstant.KEY_TAGERT_KEYWORD, keyword)
//                            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            .navigation()
                    if (item.id != null && item.targetId != null) {
                        openRoomCallback?.invoke(item.targetId, item.id!!)
                    }
                } else {
//                    ARouter.getInstance()
//                        .build(Base.SearchGroupMessage)
//                        .withString(BaseConstant.KEY_ROOM_ID, item.id)
//                        .withString(BaseConstant.KEY_SEARCH_WORD, keyword)
//                        .withString(BaseConstant.KEY_SEARCH_PREFIX, item.mainTitle)
//                        .navigation()
                    ChatRecordSearchActivity.start(mContext, item.id, keyword, item.mainTitle, item.count)
                }
            } else if (item.type == GroupMessage)  {
                 if (item.id != null && item.targetId != null) {
                     openRoomCallback?.invoke(item.targetId, item.id!!)
                 }
             }
        }
    }

    private fun setFooter(helper: BaseViewHolder, item: SearchData) {
        val footerView = helper.getView<View>(R.id.search_more_tv)
        val pos = helper.absoluteAdapterPosition
        var nextItem: SearchData? = null
        if (pos + 1 < data.size) {
            nextItem = data[pos + 1]
        }
        if (item.hasMore && (pos == data.size - 1 || nextItem?.type != item.type)) {
            footerView.visibility = View.VISIBLE
        } else {
            footerView.visibility = View.GONE
        }
        when (item.type) {
            GroupName         -> {
                helper.setText(R.id.footer_tv, mContext.getString(R.string.more_group))
            }
            GroupMessageCount -> {
                helper.setText(R.id.footer_tv, mContext.getString(R.string.more_group_message))
            }
            DirectGroupName   -> {
                helper.setText(R.id.footer_tv, mContext.getString(R.string.more_contact))
            }
        }

        helper.getView<View>(R.id.search_more_tv).setOnClickListener {
//            if (item.type == GroupName || item.type == DirectGroupName) {
//                // var isDirect = false
//                if (item.type == DirectGroupName) {
//                    // isDirect = true
//                }
//                ARouter.getInstance()
//                        .build(Base.SearchGroupName)
//                        .withString(BaseConstant.KEY_SEARCH_WORD, keyword)
//                        .withBoolean(BaseConstant.KEY_IS_DIRECT, isDirect)
//                        .navigation()
//            } else
                if (item.type == GroupMessageCount) {
                    SearchMoreActivity.start(mContext, keyword, searchType = SEARCH_MORE_TYPE_CHAT_RECORD)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setTitle(helper: BaseViewHolder, item: SearchData) {
        val titleTV = helper.getView<TextView>(R.id.title_tv)
        val pos = helper.absoluteAdapterPosition
        var preItem: SearchData? = null
        if (pos - 1 >= 0) {
            preItem = data[pos - 1]
        }
        if (pos == 0 || preItem?.type != item.type) {
            titleTV.visibility = View.VISIBLE
//            helper.setVisible(R.id.title_div, true)
            helper.setGone(R.id.top_div, pos != 0)
            when (item.type) {
                GroupMessageCount -> {
                    titleTV.setText(R.string.message_record_title)
                }
                GroupName         -> {
                    titleTV.setText(R.string.group_chat)
                }
                DirectGroupName   -> {
                    titleTV.setText(R.string.contacts)
                }
                GroupMessage      -> {
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            val session = BaseModule.getSession()
                                    ?: return@withContext ""
                            if (item.targetId == null) {
                                return@withContext ""
                            }
                            val room = session.getRoom(item.targetId) ?: return@withContext ""
                            val roomSummary = room.roomSummary() ?: return@withContext ""
                            if (roomSummary.isDirect) {
                                if (roomSummary.otherMemberIds.isNotEmpty()) {
                                    val matrixUser = session.getUser(roomSummary.otherMemberIds[0])
                                    //                                val directName = getUserDisplayName(matrixUser?.getBestName(), roomSummary.otherMemberIds[0])
                                    return@withContext matrixUser?.displayName
                                }
                            } else {
                                return@withContext roomSummary.displayName
                            }
                            return@withContext ""
                        }
                        try {
                            titleTV.text = BaseModule.getContext()
                                    .getString(R.string.contains_message_record, count.toString())
                        } catch (e: Throwable) {

                        }
                    }
                }
            }
        } else {
            titleTV.visibility = View.GONE
            helper.setGone(R.id.top_div, false)
//            helper.setGone(R.id.title_div, false)
        }
    }
}

object SearchType {
    const val GroupName = 1
    const val GroupMessageCount = 2
    const val GroupMessage = 3
    const val DirectGroupName = 4
}
