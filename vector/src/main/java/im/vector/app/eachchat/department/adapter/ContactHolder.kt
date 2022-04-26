package im.vector.app.eachchat.department.adapter

import GlideUtils
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.department.data.IDisplayBean
import im.vector.app.eachchat.user.UserInfoActivity
import im.vector.app.eachchat.user.UserInfoArg
import im.vector.app.features.roommemberprofile.RoomMemberProfileActivity
import im.vector.app.features.roommemberprofile.RoomMemberProfileArgs
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Created by zhouguanjie on 2019/8/22.
 */
class ContactHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var mAvatar: ImageView? = null

    private var mName: TextView? = null

    private var mMinorContent: TextView? = null
    private var mRoot: View = view.findViewById(R.id.root)

    var mTitle: TextView? = null

    private var mCheckView: ImageView? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun bindView(user: IDisplayBean, showHeader: Boolean) {
        mAvatar = mRoot.findViewById(R.id.iv_header)
        mName = mRoot.findViewById(R.id.tv_name)
        mMinorContent = mRoot.findViewById(R.id.tv_title)
        mTitle = mRoot.findViewById(R.id.title)
        mCheckView = mRoot.findViewById(R.id.check_view)
        mName?.text = user.mainContent
        GlideUtils.loadCircleImage(mAvatar, user.avatar)
        mTitle!!.visibility = if (showHeader) View.VISIBLE else View.GONE
        if (TextUtils.isEmpty(user.minorContent)) {
            mMinorContent!!.visibility = View.INVISIBLE
        } else {
            mMinorContent!!.visibility = View.VISIBLE
            mMinorContent?.text = user.minorContent
        }
        mRoot.setOnClickListener {
            if (user is User) {
                val contact: User = user
                UserInfoActivity.start(mRoot.context, UserInfoArg(userId = contact.matrixId, departmentUserId = contact.id))
            }
        }
    }

    fun setCheckView(canCheck: Boolean, isChecked: Boolean) {
        if (!canCheck) {
            mCheckView!!.setImageResource(R.mipmap.select_contacts_disable)
            return
        }
        mCheckView?.setImageResource(if (isChecked) R.mipmap.select_contacts_checked else R.mipmap.select_contacts_nocheck)
    }
}
