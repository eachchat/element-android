package im.vector.app.eachchat.moreinfo.relationship

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import im.vector.app.R
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.utils.ScreenUtils
import timber.log.Timber

/**
 * Created by chengww on 2019-11-13
 *
 * @author chengww
 */
class ReportingRelationshipView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private var users: List<User>? = null
    private fun init() {
        orientation = VERTICAL
    }

    @SuppressLint("CutPasteId")
    fun addItems(users: List<User>) {
        this.users = users
        val dp1 = ScreenUtils.dip2pxFloat(context, 1f)
        val dp6 = 7.5f * dp1
        val dp7 = 9 * dp1
        val dp16 = 16 * dp1
        for (i in users.indices) {
            @SuppressLint("InflateParams") val inflate = LayoutInflater.from(context).inflate(R.layout.item_reporting_relationship, null)
            val user = users[i]
            val layoutUser = inflate.findViewById<View>(R.id.layout_user)
            if (clickListener != null) {
                layoutUser.setOnClickListener { v: View? -> clickListener!!.onClick(v, user) }
                layoutUser.setOnLongClickListener { v: View? -> clickListener!!.onLongClick(v, user) }
            }
            val res = resources
            val conf = res.configuration
            if (conf.fontScale > 1) {
                val viewDashBottom = inflate.findViewById<View>(R.id.view_dash_bottom)
                val viewDashTop = inflate.findViewById<View>(R.id.view_dash_top)
                val viewDashStart = inflate.findViewById<View>(R.id.view_dash_start)
                val viewDashEnd = inflate.findViewById<View>(R.id.view_dash_end)
                val viewCircle = inflate.findViewById<View>(R.id.view_circle)
                val layoutSwitchLine = inflate.findViewById<View>(R.id.layout_switch_line)
                val viewDashSwitch = inflate.findViewById<View>(R.id.view_dash_switch)
                val viewIvSwitch = inflate.findViewById<View>(R.id.iv_switch)
                val paramsLayoutUser = layoutUser.layoutParams
                val paramsBottom = viewDashBottom.layoutParams
                val paramsTop = viewDashTop.layoutParams
                val paramsStart = viewDashStart.layoutParams
                val paramsDashSwitch = viewDashSwitch.layoutParams
                val paramsIvSwitch = viewIvSwitch.layoutParams
                val paramsEnd = viewDashEnd.layoutParams as LayoutParams
                val paramsCircle = viewCircle.layoutParams as LayoutParams
                val paramsSwitch = layoutSwitchLine.layoutParams as LayoutParams
                paramsLayoutUser.width = (paramsLayoutUser.width * conf.fontScale).toInt()
                paramsLayoutUser.height = (paramsLayoutUser.height * conf.fontScale).toInt()
                paramsBottom.width = (paramsBottom.width * conf.fontScale).toInt()
                paramsBottom.height = (paramsBottom.height * conf.fontScale).toInt()
                paramsTop.width = (paramsTop.width * conf.fontScale).toInt()
                paramsTop.height = (paramsTop.height * conf.fontScale).toInt()
                paramsStart.width = (paramsStart.width * conf.fontScale).toInt()
                paramsStart.height = (paramsStart.height * conf.fontScale).toInt()
                paramsDashSwitch.width = (paramsDashSwitch.width * conf.fontScale).toInt()
                paramsDashSwitch.height = (paramsDashSwitch.height * conf.fontScale).toInt()
                paramsIvSwitch.width = (paramsIvSwitch.width * conf.fontScale).toInt()
                paramsIvSwitch.height = (paramsIvSwitch.height * conf.fontScale).toInt()
                paramsEnd.width = (paramsEnd.width * conf.fontScale).toInt()
                paramsEnd.height = (paramsEnd.height * conf.fontScale).toInt()
                paramsEnd.topMargin = (paramsEnd.topMargin * conf.fontScale).toInt()
                paramsCircle.width = (paramsCircle.width * conf.fontScale).toInt()
                paramsCircle.height = (paramsCircle.height * conf.fontScale).toInt()
                paramsCircle.topMargin = (paramsCircle.topMargin * conf.fontScale).toInt()
                paramsCircle.rightMargin = (paramsCircle.rightMargin * conf.fontScale).toInt()
                paramsSwitch.topMargin = (paramsSwitch.topMargin * conf.fontScale).toInt()
                paramsSwitch.width = (paramsSwitch.width * conf.fontScale).toInt()
                paramsSwitch.height = (paramsSwitch.height * conf.fontScale).toInt()
            }
            val tvUserName = layoutUser.findViewById<TextView>(R.id.tv_user_name)
            val tvUserTitle = layoutUser.findViewById<TextView>(R.id.tv_user_title)
            val ivUserAvatar = layoutUser.findViewById<ImageView>(R.id.iv_user_avatar)
            tvUserName.text = user.name
            val userTitle = user.userTitle
            tvUserTitle.text = userTitle
            tvUserTitle.visibility = if (TextUtils.isEmpty(userTitle)) GONE else VISIBLE
            User.loadAvatar(context, user.avatarTUrl, ivUserAvatar)
            addView(inflate)

            // Layout
            if (i == 0) {
                inflate.findViewById<View>(R.id.view_dash_start).visibility = GONE
                inflate.findViewById<View>(R.id.view_dash_top).visibility = INVISIBLE
            } else {
                val params = inflate.layoutParams as LayoutParams
                var marginStart = dp16 * (i - 1) + dp6 * (i - 1) + dp7 - dp1 * (i - 1)
                if (conf.fontScale > 1) marginStart *= conf.fontScale
                params.marginStart = marginStart.toInt()
            }
            // The Last One
            if (i == users.size - 1) {
                inflate.findViewById<View>(R.id.iv_switch).visibility = GONE
                inflate.findViewById<View>(R.id.view_dash_switch).visibility = VISIBLE
                inflate.findViewById<View>(R.id.view_dash_bottom).visibility = GONE
            }
        }
    }

    fun updateAvatar(matrixId: String?, avatar: String?) {
        if (users.isNullOrEmpty()) return
        try {
            for (i in users!!.indices) {
                val user = users!![i]
                if (TextUtils.equals(matrixId, user.matrixId)) {
                    val ivUser = getChildAt(i).findViewById<ImageView>(R.id.iv_user_avatar)
                    User.loadAvatar(context, avatar, ivUser)
                    break
                }
            }
        } catch (e: Exception) {
            Timber.e("updateAvatar %s", e.localizedMessage)
        }
    }

    private var clickListener: UserClickListener? = null

    interface UserClickListener {
        fun onLongClick(view: View?, user: User?): Boolean
        fun onClick(view: View?, user: User?)
    }

    /**
     * First set UserClickListener then addItems
     *
     * @param clickListener [UserClickListener]
     */
    fun setUserClickListener(clickListener: UserClickListener?) {
        this.clickListener = clickListener
    }

    init {
        init()
    }
}
