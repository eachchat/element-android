package im.vector.app.eachchat.search.contactsearch

import ai.workly.eachchat.android.search.adapter.ContactsSearchAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.viewModel
import com.blankj.utilcode.util.SizeUtils
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityContactsSearchLayoutBinding
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.search.contactsearch.searchmore.SearchMoreActivity
import im.vector.app.eachchat.ui.dialog.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.MatrixPatterns
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams

@AndroidEntryPoint
open class ContactsSearchActivity :
        VectorBaseActivity<ActivityContactsSearchLayoutBinding>() {

    protected val adapter = ContactsSearchAdapter()
    protected var searchJob: Job? = null

    private var isShowSeeMore: Boolean = false // 是否显示查看更多

    val vm: ContactsSearchViewModel by viewModel()

    override fun getBinding(): ActivityContactsSearchLayoutBinding {
        return ActivityContactsSearchLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSearchTitleBar()
        initRecyclerView()
        observeData()
        initListener()
    }

    private fun initSearchTitleBar() {
        views.searchTitleBar.layoutSearch.setPadding(
                0,
                SizeUtils.dp2px(10f), // + StatusBarUtil.getStatusHeight(this),
                0,
                SizeUtils.dp2px(10f)
        )
        views.searchTitleBar.tvCancel.setOnClickListener { onBackPressed() }
        views.searchTitleBar.backIv.setOnClickListener { onBackPressed() }
        views.searchTitleBar.etSearch.doAfterTextChanged {
            if (it == null || it.isBlank()) {
                showSearingView(false)
                showSearchResults(null)
                return@doAfterTextChanged
            }
            val keyWord: String = it.toString()
            showSearchResults(null)
            showSearingView(true)
            doSearch(keyWord.trim())
        }
        views.searchTitleBar.etSearch.setHint(R.string.search_name_id_email_phone)
    }

    private fun initRecyclerView() {
        views.searchResultRv.layoutManager = LinearLayoutManager(this)
        views.searchResultRv.adapter = adapter
    }

    private fun observeData() {
        vm.searchSeeMoreItems.observe(this) {
            isShowSeeMore = true
            showSearchResults(it)
        }
        vm.searchResultsLiveData.observe(this) {
            isShowSeeMore = false
            showSearchResults(it)
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
                // Contact.contactInfoActivity(it)
            }
        }
    }

    private fun initListener() {
        adapter.setOnItemClickListener { adapter, _, position ->
            when (val baseItem = adapter.data[position]) {
                is ContactsSearchAdapter.FooterItem              ->
                    when (baseItem.subType) {
                        ContactsSearchAdapter.SUB_TYPE_MY_CONTACT    -> {
                            SearchMoreActivity.start(this,
                                    views.searchTitleBar.etSearch.text.toString(),
                                    true,
                                    SearchMoreActivity.SEARCH_MORE_TYPE_CONTACT)
                        }
                        ContactsSearchAdapter.SUB_TYPE_ORG           -> {
                            SearchMoreActivity.start(this,
                                    views.searchTitleBar.etSearch.text.toString(),
                                    true,
                                    SearchMoreActivity.SEARCH_MORE_TYPE_ORG_MEMBER)
                        }
                        ContactsSearchAdapter.SUB_TYPE_GROUP_CHAT    -> {
                            SearchMoreActivity.start(this,
                                    views.searchTitleBar.etSearch.text.toString(),
                                    true,
                                    SearchMoreActivity.SEARCH_MORE_TYPE_GROUP_CHAT)
                        }
                        ContactsSearchAdapter.SUB_TYPE_CLOSE_CONTACT -> {
                            SearchMoreActivity.start(this,
                                    views.searchTitleBar.etSearch.text.toString(),
                                    true,
                                    SearchMoreActivity.SEARCH_MORE_TYPE_REAL_CONTACT)
                        }
                        ContactsSearchAdapter.SUB_TYPE_DEPARTMENT    -> {
                            SearchMoreActivity.start(this,
                                    views.searchTitleBar.etSearch.text.toString(),
                                    true,
                                    SearchMoreActivity.SEARCH_MORE_TYPE_DEPARTMENT)
                        }
                        else                                         -> vm.buildSearchSeeMoreData(baseItem.subType)
                    }
                is ContactsSearchAdapter.ContentItem             -> {
                    baseItem.item?.let {
                        when (baseItem.subType) {
                            ContactsSearchAdapter.SUB_TYPE_CLOSE_CONTACT -> {
                                val user: User = baseItem.item as User
                                lifecycleScope.launch(Dispatchers.IO) {
                                    kotlin.runCatching {
                                        val existingRoomId = user.matrixId?.let { matrixId ->
                                            BaseModule.getSession().getExistingDirectRoomWithUser(matrixId)
                                        }
                                        if (existingRoomId != null) {
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                navigator.openRoom(BaseModule.getContext(), existingRoomId)
                                            }
                                        } else {
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                val roomParams = CreateRoomParams()
                                                        .apply {
                                                            user.matrixId?.let { invitedUserIds.add(it) }
                                                            setDirectMessage()
                                                            enableEncryptionIfInvitedUsersSupportIt = false
                                                        }

                                                try {
                                                    val roomId = BaseModule.getSession().createRoom(roomParams)
                                                    navigator.openRoom(BaseModule.getContext(), roomId)
                                                } catch (failure: Throwable) {

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ContactsSearchAdapter.SUB_TYPE_MY_CONTACT    -> {
                                // contactInfoActivityV2(displayBean.id)
                            }
                            ContactsSearchAdapter.SUB_TYPE_ORG           -> {
                                // start(displayBean.id)
                            }
                            ContactsSearchAdapter.SUB_TYPE_DEPARTMENT    -> {
//                                DepartmentActivity.navigation(
//                                    this,
//                                    displayBean.mainContent,
//                                    displayBean.id,
//                                    false,
//                                    false
//                                )
                            }
                            ContactsSearchAdapter.SUB_TYPE_GROUP_CHAT    -> {
                                // Chat.start(displayBean.id)
                            }
                            else                                         -> {}
                        }
                    }
                }
                is ContactsSearchAdapter.SearchContactOnlineItem -> {
                    searchContactOnline(views.searchTitleBar.etSearch.text.toString().trim())
                }
            }
        }
    }

    private fun searchContactOnline(keyWord: String) {
        when {
            keyWord.isBlank()                -> return
            MatrixPatterns.isUserId(keyWord) -> {
                vm.searchMatrixUser(keyWord)
            }
            else                             -> {
                vm.searchMatrixUserByEmailOrPhone(keyWord)
            }
        }
    }

    fun showSearingView(isShow: Boolean) {
        if (isShow) {
            val fromDegrees = 0
            val toDegrees = 360
            val pivotX = 0.5f
            val pivotY = 0.5f
            val loadingAnimation = RotateAnimation(
                    fromDegrees.toFloat(),
                    toDegrees.toFloat(),
                    Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY
            )
            loadingAnimation.duration = 3000
            loadingAnimation.repeatCount = Animation.INFINITE
            loadingAnimation.interpolator = LinearInterpolator()
            views.searchingLayout.loadingIv.animation = loadingAnimation
            loadingAnimation.startNow()
            views.searchingLayout.searchingLl.visibility = View.VISIBLE
        } else {
            views.searchingLayout.loadingIv.clearAnimation()
            views.searchingLayout.searchingLl.visibility = View.GONE
        }
    }

    open fun showSearchResults(results: List<ContactsSearchAdapter.BaseItem>?) {
        if (isFinishing || isDestroyed) {
            return
        }
        views.searchTitleBar.backIv.visibility = if (isShowSeeMore) View.VISIBLE else View.GONE
        showSearingView(false)
//        observer.clearObserver(this)
        adapter.setNewData(results)
    }

    open fun doSearch(keyWord: String) {
        searchJob?.cancel()
        adapter.setKeyword(keyWord)
        searchJob = vm.search(keyWord)
    }

    override fun onResume() {
        super.onResume()
        requestFocus()
    }

    open fun requestFocus() {
        views.searchTitleBar.etSearch.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onPause() {
        super.onPause()
        val inputManger = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManger.hideSoftInputFromWindow(views.searchTitleBar.etSearch.windowToken, 0)
    }

    override fun onBackPressed() {
        if (views.searchTitleBar.backIv.isVisible) {
            isShowSeeMore = false
            showSearchResults(vm.searchResultsLiveData.value)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun start(
                context: Context,
        ) {
            val intent = Intent(context, ContactsSearchActivity::class.java)
            context.startActivity(intent)
        }
    }
}
