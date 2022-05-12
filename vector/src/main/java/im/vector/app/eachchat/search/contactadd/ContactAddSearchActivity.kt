package im.vector.app.eachchat.search.contactadd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.viewModel
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityContactAddSearchBinding
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.search.contactadd.adapter.ContactAddSearchAdapter
import im.vector.app.eachchat.ui.LineDecoration
import im.vector.app.eachchat.ui.dialog.AlertDialog
import im.vector.app.eachchat.user.UserInfoActivity
import im.vector.app.eachchat.user.UserInfoArg
import im.vector.app.eachchat.utils.ScreenUtils
import im.vector.app.eachchat.utils.ToastUtil
import im.vector.app.features.roommemberprofile.RoomMemberProfileActivity
import im.vector.app.features.roommemberprofile.RoomMemberProfileArgs
import org.matrix.android.sdk.api.MatrixPatterns

/**
 * Created by chengww on 2020/10/26
 * @author chengww
 */
@AndroidEntryPoint
class ContactAddSearchActivity : VectorBaseActivity<ActivityContactAddSearchBinding>() {
    val vm: ContactAddSearchViewModel by viewModel()

    override fun getBinding(): ActivityContactAddSearchBinding {
        return ActivityContactAddSearchBinding.inflate(layoutInflater)
    }

    companion object {
        const val RequestContactInfoCode = 0xec04
        const val ContactPosition = "ContactPosition"
        const val ContactAfterEdited = "ContactAfterEdited"

        fun start(context: Context) {
            val intent = Intent(context, ContactAddSearchActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

        //init waiting view
        views.waitingView.waitingStatusText.text = getString(R.string.please_wait)
        views.waitingView.waitingStatusText.isVisible = true
        vm.loading.observe(this) {
            views.waitingView.waitingView.isVisible = it
        }
    }

    private var mAdapter = ContactAddSearchAdapter()

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        setOnClick()
        setupToolbar(views.groupToolbar).allowBack(true, true)
        vm.modeNormal.postValue(true)
        views.listUser.run {
            layoutManager = LinearLayoutManager(this@ContactAddSearchActivity)
            itemAnimator = null
            mAdapter = ContactAddSearchAdapter(vm.userList)
            mAdapter.setOnItemClickListener { adapter, _, position ->
                val user = adapter.getItem(position) as ContactsDisplayBean
                if (user.fromOrg) {
                    UserInfoActivity.start(this@ContactAddSearchActivity, UserInfoArg(userId = user.matrixId, departmentUserId = user.contactId))
                } else {
                    val intent = RoomMemberProfileActivity.newIntent(this@ContactAddSearchActivity, RoomMemberProfileArgs(userId = user.matrixId))
                    startActivity(intent)
                }
            }
            mAdapter.setOnItemChildClickListener { adapter, view, position ->
                if (view.id == R.id.btn_add_contacts) {
                    vm.addContacts(position, adapter?.data?.get(position) as ContactsDisplayBean, mAdapter)
                }
            }

            adapter = mAdapter
//            addItemDecoration(LineDecoration(marginStart = ScreenUtils.dip2px(context, 68f)))
        }
        // auto open the soft input
        views.etSearch.requestFocus()
        views.etSearch.requestFocusFromTouch()
        views.etSearch.doAfterTextChanged {
            if (!it.isNullOrBlank()) {
                vm.keyword.postValue(it.toString().trim())
            }
        }
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        vm.keyword.observe(this) {
            val tvSearchContent = views.layoutSearchUserOnline.searchOnlineIdTv
            tvSearchContent.text = it?.trim()
            vm.onKeywordChanged(it)
        }

        vm.addResponse.observe(this) {
            //if (!it.obj?.contactId.isNullOrEmpty()) {
            if (!it.obj?.id.isNullOrEmpty()) {
                ToastUtil.showSuccess(this, getString(R.string.added_to_my_contacts))
            } else {
                ToastUtil.showError(this, getString(R.string.network_error))
            }
        }

        vm.searchUserOnlineLiveData.observe(this) {
            if (it == null) {
                val alertDialog = AlertDialog(this).builder()
                alertDialog.setPositiveButton(R.string.confirm) {
                    alertDialog.dismiss()
                }
                    .setTitle(R.string.this_user_not_exist)
                    .setMsg(getString(R.string.user_not_found))
                    .show()
            } else {
                vm.userList.add(it)
                vm.showContactList.postValue(true)
                vm.showEmptyView.postValue(false)
//                Contact.contactInfoActivity(it)
            }
        }
        
        vm.userListLiveData.observe(this) {
            mAdapter.setNewData(it)
            mAdapter.notifyDataSetChanged()
        }

        views.searchingLayout.loadingIv.animation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f
        ).also {
            it.duration = 2000
            it.repeatCount = Animation.INFINITE
            it.interpolator = LinearInterpolator()
        }.also { it.start() }

        vm.showOnlineSearchLayout.observe(this) {
            views.layoutSearchUserOnline.myContactLl.isVisible = it
        }

        vm.showContactList.observe(this) {
            views.listUser.isVisible = it
        }

        vm.searching.observe(this) {
            views.searchingLayout.searchingLl.isVisible = it
        }

        vm.showEmptyView.observe(this) {
            views.emptyTv.isVisible = it
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RequestContactInfoCode && resultCode == RESULT_OK && data != null) {
            val position = data.getIntExtra(ContactPosition, -1)
            val contactAfter =
                data.getParcelableExtra<ContactsDisplayBean>(ContactAfterEdited)
            if (position > -1 && contactAfter != null && contactAfter.matrixId.isNotBlank()) {
                if (vm.userList.size > position) {
                    val oldContact = vm.userList[position]
                    if (oldContact.fromOrg) {
                        vm.userList[position] = oldContact.also {
                            it.contactAdded = contactAfter.contactAdded
                        }
                    } else {
                        vm.userList[position] = contactAfter.also {
                            vm.keyword.value?.toString()?.let { key ->
                                it.mainTitle =
                                    it.mainTitle?.replace(key, "<font color='#24B36B'>$key</font>")
                                it.subTitle =
                                    it.matrixId.replace(key, "<font color='#24B36B'>$key</font>")
                            }
                        }
                    }
                } else vm.keyword.value?.let { vm.onKeywordChanged(it) }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setOnClick() {
//        views.tvCancel.onClick {
//            finish()
//        }
        views.layoutSearchUserOnline.myContactLl.onClick {
            searchContactOnline(views.etSearch.text.toString().trim())
        }
    }

    private fun searchContactOnline(keyword: String) {
        when {
            keyword.isBlank() ->  return
            MatrixPatterns.isUserId(keyword)-> {
                vm.searchMatrixUser(keyword)
            }
            else -> {
                vm.searchMatrixUserByEmailOrPhone(keyword)
            }
        }
    }
}
