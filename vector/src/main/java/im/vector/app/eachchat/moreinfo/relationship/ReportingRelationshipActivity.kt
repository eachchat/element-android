package ai.workly.eachchat.android.contact.relationship

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.viewModel
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityReportingRelationshipBinding
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.moreinfo.relationship.ReportingRelationshipView
import im.vector.app.eachchat.user.UserInfoActivity
import im.vector.app.eachchat.user.UserInfoArg
import im.vector.app.eachchat.utils.ToastUtil

/**
 * Created by chengww on 2020/11/12
 * @author chengww
 */

@AndroidEntryPoint
class ReportingRelationshipActivity : VectorBaseActivity<ActivityReportingRelationshipBinding>() {

    private var departmentUserId: String? = null

    override fun getBinding(): ActivityReportingRelationshipBinding {
        return ActivityReportingRelationshipBinding.inflate(layoutInflater)
    }

    companion object {
        private const val KEY_DEPARTMENT_USER_ID = "KEY_DEPARTMENT_USER_ID"
        fun start(context: Context, departmentUserId: String?) {
            val intent = Intent(context, ReportingRelationshipActivity::class.java).apply {
                putExtra(KEY_DEPARTMENT_USER_ID, departmentUserId)
            }
            context.startActivity(intent)
        }
    }

    val vm :ReportingRelationshipViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        departmentUserId = intent.getStringExtra(KEY_DEPARTMENT_USER_ID)

        if (departmentUserId == null) {
            ToastUtil.showError(BaseModule.getContext(), R.string.load_user_error)
            finish()
            return
        }

        setupToolbar(views.reportRelationshipToolbar)
                .allowBack()


        vm.managers.observe(this) {
            if (it != null) {
                views.layoutReportingRelationship.addItems(it)
            }
        }

        views.layoutReportingRelationship.setUserClickListener(object : ReportingRelationshipView.UserClickListener {
            override fun onLongClick(view: View?, user: User?): Boolean {
//                showPopWindow(view, user)
                return true
            }

            override fun onClick(view: View?, user: User?) {
                UserInfoActivity.start(this@ReportingRelationshipActivity, UserInfoArg(user?.matrixId, departmentUserId = user?.id, displayName = user?.displayName))
            }
        })

        vm.managerAvatars.observe(this) { _managerAvatars ->
            _managerAvatars.keys.forEach { matrixId ->
                _managerAvatars[matrixId]?.observe(this) {
                    views.layoutReportingRelationship.updateAvatar(matrixId, it)
                }
            }
        }
        if (!departmentUserId.isNullOrBlank()) {
            vm.getManagers(departmentUserId!!)
        }
    }

}
