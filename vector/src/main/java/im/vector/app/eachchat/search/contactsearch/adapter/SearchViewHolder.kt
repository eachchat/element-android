package im.vector.app.eachchat.search.contactsearch.adapter

import GlideUtils
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.api.BaseConstant
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.department.ContactUtils
import im.vector.app.eachchat.department.DepartmentActivity
import im.vector.app.eachchat.department.data.IDisplayBean
import im.vector.app.eachchat.mqtt.UserCache
import im.vector.app.eachchat.search.contactsearch.data.AppConstant
import im.vector.app.eachchat.search.contactsearch.data.SearchGroupMessageBean
import im.vector.app.eachchat.search.contactsearch.data.SearchParam
import im.vector.app.eachchat.search.contactsearch.event.SearchDataEvent
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.utils.ToastUtil
import im.vector.app.eachchat.utils.string.HtmlUtils.fromHtml
import org.greenrobot.eventbus.EventBus
import java.util.Locale

/**
 * Created by zhouguanjie on 2019/9/9.
 */
class SearchViewHolder(private val activity: Activity, view: View, private val isV2: Boolean) : RecyclerView.ViewHolder(view) {
    companion object {
        private const val KEY_USER_ID = "userId"
        const val KEY_USER_NAME = "key_user_name"
    }
    private val mImage: ImageView
    private val mMainTV: TextView
    private val mMinorTV: TextView
    private val mView: View
    private val mTitle: TextView
    private val mViewMore: View
    private val mDiv: View
    private val mBottomDivLine: View
    private val delIV: View
    fun bindView(context: Context?,
                 pos: Int,
                 search: IDisplayBean,
                 preSearch: IDisplayBean?,
                 nextSearch: IDisplayBean?,
                 keyWord: String,
                 param: SearchParam) {
        val type = param.searchType
        delIV.visibility = if (param.isShowDelIcon
                && !TextUtils.equals(search.id, UserCache.getUserId())) View.VISIBLE else View.GONE
        val mainContent = if (TextUtils.isEmpty(search.mainContent)) "" else search.mainContent
        if (TextUtils.isEmpty(mainContent)) {
            mMainTV.visibility = View.INVISIBLE
        } else {
            mMainTV.visibility = View.VISIBLE
            mMainTV.text = mainContent.replace(keyWord, "<font color='#000000'>$keyWord</font>").fromHtml()
        }
        var content = search.minorContent
        if (TextUtils.isEmpty(content)) {
            if (search.type == AppConstant.SEARCH_DEPARTMENT_CONTACTS_TYPE ||
                    search is User || search.type == BaseConstant.SEARCH_USER_TYPE || search.type == BaseConstant.SEARCH_CONTACT_TYPE) {
                mMinorTV.visibility = View.INVISIBLE
            } else {
                mMinorTV.visibility = View.GONE
            }
        } else {
            mMinorTV.visibility = View.VISIBLE
            content = content.replace(keyWord, "<font color='#24B36B'>$keyWord</font>")
            if (type == AppConstant.SEARCH_MULTI_TYPE && search.type == AppConstant.SEARCH_GROUP_MESSAGE_TYPE && search.count > 1) {
                mMinorTV.text = String.format(Locale.getDefault(),
                        "%d%s", search.count,
                        mView.context.getString(R.string.search_message_count))
            } else if (type == AppConstant.SEARCH_MESSAGE_TYPE && search.count > 1) {
                mMinorTV.text = String.format(Locale.getDefault(),
                        "%d%s", search.count,
                        mView.context.getString(R.string.search_message_count))
            } else {
                mMinorTV.text = content.fromHtml()
            }
        }
        //设置小标题
        showTitle(pos, param, search, preSearch, nextSearch)
        //设置头像
        setImageByType(param.searchType, search)
        //点击事件
        mView.setOnClickListener { v: View ->
            if (param.isOnlyReturnData) {
                //点击的数据通过EventBus通知
                EventBus.getDefault().post(SearchDataEvent(context, search.type, search.id, search.mainContent))
                return@setOnClickListener
            }
            if (type == AppConstant.SEARCH_MULTI_TYPE) {
                clickByType(v.context, keyWord, search.type, search, param.isSingleChooseMode)
            } else {
                clickByType(v.context, keyWord, param.searchType, search, param.isSingleChooseMode)
            }
        }
        delIV.setOnClickListener { EventBus.getDefault().post(SearchDelEvent(search.id)) }
    }

    private fun setImageByType(type: Int, search: IDisplayBean?) {
        when (type) {
            AppConstant.SEARCH_MESSAGE_TYPE, BaseConstant.SEARCH_USER_TYPE, BaseConstant.SEARCH_CONTACT_TYPE, AppConstant.SEARCH_GROUP_MESSAGE_TYPE, AppConstant.SEARCH_GROUP_CONTACTS_TYPE, AppConstant.SEARCH_DEPARTMENT_CONTACTS_TYPE, AppConstant.SEARCH_GROUP, AppConstant.SEARCH_TEAM_MEMBER_TYPE, AppConstant.SEARCH_TOPIC_MENTION_MEMBER_TYPE, AppConstant.SEARCH_TEAM_CHAT_MESSAGE -> setUserAvatar(search)
            BaseConstant.SEARCH_CONTACT_GROUP_TYPE                                                                                                                                                                                                                                                                                                                                          -> GlideUtils.loadCircleRoomImage(mImage, search?.avatar)
            BaseConstant.SEARCH_DEPARTMENT_TYPE                                                                                                                                                                                                                                                                                                                                             -> search?.let{ mImage.setImageResource(ContactUtils.getDepartmentIcon(it)) }
            AppConstant.SEARCH_MULTI_TYPE                                                                                                                                                                                                                                                                                                                                                   -> setImageByType(search!!.type, search)
            else                                                                                                                                                                                                                                                                                                                                                                            -> mImage.setImageResource(R.drawable.default_person_icon)
        }
    }

    private fun setUserAvatar(bean: IDisplayBean?) {
        var avatarUrl: String? = null
        if (bean != null) {
            avatarUrl = bean.avatar
        }
        GlideUtils.loadCircleImage(mImage, avatarUrl)
    }

    private fun clickByType(context: Context, keyWord: String, type: Int, search: IDisplayBean, isSingleMode: Boolean) {
        if (type == AppConstant.SEARCH_MESSAGE_TYPE) {
            val bean: SearchGroupMessageBean = search as SearchGroupMessageBean
            if (TextUtils.isEmpty(bean.groupId)) {
                ToastUtil.showError(context, R.string.group_may_not_exist)
                return
            }
            // if (search.count > 1) {
//                StartSearch.startSearchMessageInGroup(context as Activity, bean.getGroupId(),
//                        bean.getGroupName(), bean.getCount(), keyWord)
//                return
            // }
            // ChatStart.start(context, bean.getGroupId(), bean.getGroupName(), bean.getSeqId(), false)
        } else if (type == BaseConstant.SEARCH_USER_TYPE
                || type == AppConstant.SEARCH_GROUP_CONTACTS_TYPE) {
            if (isSingleMode) {
                val intent = Intent()
                intent.putExtra(KEY_USER_ID, search.id)
                intent.putExtra(KEY_USER_NAME, search.mainContent)
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            } else {
               // UserInfoActivity.start(search.id)
            }
        } else if (type == AppConstant.SEARCH_GROUP_MESSAGE_TYPE) {
            if (search is SearchGroupMessageBean) {
                val bean: SearchGroupMessageBean = search
                if (isV2 && search.count > 1) {
                    //复合搜索跳转到具体群组聊天记录搜索
                    EventBus.getDefault().post(SearchGroupMessageEvent(bean.groupId, keyWord))
                    return
                }
                // ChatStart.start(context, bean.getGroupId(), null, bean.getSeqId(), false)
            }
        } else if (type == BaseConstant.SEARCH_DEPARTMENT_TYPE) {
            DepartmentActivity.start(context, search.mainContent, search.id)
        } else if (type == AppConstant.SEARCH_TOPIC_MENTION_MEMBER_TYPE) {
            val intent = Intent()
            intent.putExtra(KEY_USER_ID, search.id)
            intent.putExtra(KEY_USER_NAME, search.mainContent)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        } else if (type == AppConstant.SEARCH_CHANNEL_MEMBER_TYPE) {
            val intent = Intent()
            intent.putExtra(KEY_USER_ID, search.id)
            intent.putExtra(KEY_USER_NAME, search.mainContent)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        } else if (type == BaseConstant.SEARCH_CONTACT_TYPE) {
            if (isSingleMode) {
                val intent = Intent()
                intent.putExtra(KEY_USER_ID, search.id)
                intent.putExtra(KEY_USER_NAME, search.mainContent)
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            } else {
                // Contact.INSTANCE.contactInfoActivity(search.id, true, false)
            }
        }
        //else if (type == BaseConstant.SEARCH_CONTACT_GROUP_TYPE) {
//            ARouter.getInstance().build(RoutePath.CHAT_DETAIL)
//                    .withString(BaseConstant.KEY_ROOM_ID, search.id)
//                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    .navigation()
        // }
    }

    private fun showTitle(pos: Int,
                          param: SearchParam,
                          search: IDisplayBean,
                          preSearch: IDisplayBean?,
                          nextSearch: IDisplayBean?) {
        var isShowTitle = false
        var showBottomLine = false
        if (pos == 0) {
            isShowTitle = true
        } else if (preSearch != null && search.type != preSearch.type) {
            isShowTitle = true
        }
        if (nextSearch != null && search.type == nextSearch.type) {
            showBottomLine = true
        }
        mTitle.visibility = if (isShowTitle) View.VISIBLE else View.GONE
        setTitleByType(param.searchType, param.keyWord, param.count, search)
        if (param.searchType == AppConstant.SEARCH_MULTI_TYPE) {
            setTitleByType(search.type, param.keyWord, param.count, search)
        }
        mDiv.visibility = if (showBottomLine) View.VISIBLE else View.GONE
    }

    private fun setTitleByType(type: Int, keyword: String, count: Int, search: IDisplayBean) {
        if (type == AppConstant.SEARCH_MESSAGE_TYPE || type == AppConstant.SEARCH_TEAM_CHAT_MESSAGE) {
            mTitle.setText(R.string.message_record)
        } else if (type == BaseConstant.SEARCH_CONTACT_TYPE) {
            mTitle.setText(R.string.contacts)
        } else if (type == BaseConstant.SEARCH_USER_TYPE) {
            if (AppCache.getIsOpenOrg()) {
                mTitle.setText(R.string.organization_framework)
            } else {
                mTitle.setText(R.string.team)
            }
        } else if (type == BaseConstant.SEARCH_CONTACT_GROUP_TYPE) {
            mTitle.setText(R.string.title_group_chat)
        } else if (type == AppConstant.SEARCH_GROUP_MESSAGE_TYPE) {
            if (TextUtils.isEmpty(keyword)) {
                mTitle.setText(R.string.message_record)
            } else {
                if (isV2 && search is SearchGroupMessageBean) {
                    val bean: SearchGroupMessageBean = search
                    val builder = SpannableStringBuilder(java.lang.String.format(
                            activity.getString(R.string.group_message_record), bean.groupName))
                    val span = ForegroundColorSpan(ResourcesCompat.getColor(activity.resources, R.color.success_text, null))
                    builder.setSpan(span, 0, bean.groupName.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    mTitle.text = builder
                    return
                }
                mTitle.text = java.lang.String.format(BaseModule.getContext().getString(R.string.all_message_record), count)
            }
        } else if (type == BaseConstant.SEARCH_DEPARTMENT_TYPE) {
            mTitle.setText(R.string.department)
        } else if (type == AppConstant.SEARCH_GROUP) {
            mTitle.setText(R.string.groups)
        } else if (type == AppConstant.SEARCH_GROUP_CONTACTS_TYPE) {
            mTitle.visibility = View.GONE
        }
    }

    //查看更多
    fun tryShowMoreView() {
//        if (!GlobalConfig.NEW_VERSION_SEARCH) {
            mViewMore.visibility = View.GONE
            return
        // }
        //        int count = 0;
//        IDisplayBean currentItem = results.get(currentPos);
//        for (int index = currentPos; index >= 1; index--) {
//            if (currentItem.getType() == results.get(index - 1).getType()) {
//                count++;
//            }
//            if (count + 1 >= maxLimitCount) {
//                mBottomDivLine.setVisibility(View.VISIBLE);
//                mDiv.setVisibility(View.GONE);
//                mViewMore.setVisibility(View.VISIBLE);
//                mViewMore.setOnClickListener(v ->
//                        EventBus.getDefault().post(
//                                new ViewMoreEvent(currentItem.getType(), currentItem.getCount(), keyword)));
//                return;
//            }
//        }
        // hideMoreView()
    }

    fun hideMoreView() {
        mViewMore.visibility = View.GONE
        mBottomDivLine.visibility = View.GONE
    }

    init {
        mImage = view.findViewById(R.id.image_view)
        mMainTV = view.findViewById(R.id.main_tv)
        mMinorTV = view.findViewById(R.id.minor_tv)
        mTitle = view.findViewById(R.id.title)
        mViewMore = view.findViewById(R.id.search_more_tv)
        mDiv = view.findViewById(R.id.div_line)
        mBottomDivLine = view.findViewById(R.id.div_bottom_line)
        delIV = view.findViewById(R.id.del_iv)
        mView = view
    }
}
