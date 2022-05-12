package im.vector.app.eachchat.search.chatRecord

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
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.department.DepartmentActivity
import im.vector.app.eachchat.search.contactsearch.ContactsSearchViewModel
import im.vector.app.eachchat.search.contactsearch.data.SearchContactsBean
import im.vector.app.eachchat.search.contactsearch.data.SearchData
import im.vector.app.eachchat.search.contactsearch.data.SearchUserBean
import im.vector.app.eachchat.search.contactsearch.searchmore.SearchMoreActivity
import im.vector.app.eachchat.ui.dialog.AlertDialog
import im.vector.app.eachchat.user.UserInfoActivity
import im.vector.app.eachchat.user.UserInfoArg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.MatrixPatterns
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams

@AndroidEntryPoint
open class ChatRecordSearchActivity :
        VectorBaseActivity<ActivityContactsSearchLayoutBinding>() {

    var roomId: String? = null

    var keyword: String? = null

    var prefix: String? = null

    var adapter: SearchAllAdapter = SearchAllAdapter()

    var count: Int? = null

    val vm: ContactsSearchViewModel by viewModel()

    override fun getBinding(): ActivityContactsSearchLayoutBinding {
        return ActivityContactsSearchLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyword = intent.getStringExtra(KEY_KEYWORD)
        prefix = intent.getStringExtra(KEY_PREFIX)
        roomId = intent.getStringExtra(KEY_ROOM_ID)
        count = intent.getIntExtra(KEY_COUNT, 20)
        adapter.count = count?: 20
        initTitleBar()
        initRecyclerView()
        initListener()
        observeData()
        if (keyword.isNullOrBlank()) {
            requestFocus()
        }
        keyword?.let {
            search(it)
            adapter.keyword = it
        }
    }

    private fun initTitleBar() {
        views.searchTitleBar.backIv.visibility = View.VISIBLE
        views.searchTitleBar.backIv.setOnClickListener {
            onBackPressed()
        }
        views.searchTitleBar.etSearch.hint = getString(R.string.search_chat_record)
        if (!prefix.isNullOrBlank()) {
            views.searchTitleBar.tvPrefix.visibility = View.VISIBLE
            views.searchTitleBar.tvPrefix.text = prefix
        }
        adapter.showFooter = false
        adapter.showSendTime = true
        vm.action.postValue(SearchAction.SearchResult(null))
        adapter.setLoadMoreView(LoadMoreCollectionView())
        adapter.setOnLoadMoreListener({
            adapter.keyword?.let { vm.searchGroupMessage(it, roomId, adapter.data.size) }
        }, views.searchResultRv)

        if (!keyword.isNullOrEmpty()) {
            views.searchTitleBar.etSearch.setText(keyword)
            views.searchTitleBar.etSearch.setSelection(keyword!!.length)
        }
        views.searchTitleBar.etSearch.doAfterTextChanged {
            adapter.setNewData(null)
            search(it.toString())
            adapter.keyword = it.toString()
        }
    }

    private fun initListener() {
        adapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            if (item is SearchData) {
                item.id?.let { navigator.openRoom(this, it, item.targetId) }
            }
        }
        adapter.openRoomCallback = {roomId, eventId ->
            navigator.openRoom(this, roomId, eventId)
        }
    }

    private fun initRecyclerView() {
        views.searchResultRv.layoutManager = LinearLayoutManager(this)
        views.searchResultRv.adapter = adapter
    }

    private fun observeData() {
        vm.action.observe(this) {
            when (it) {
                is SearchAction.SearchResult     -> showSearchResults(it.result, false)
                is SearchAction.SearchMoreResult -> showSearchResults(it.result, true)
            }
        }
        vm.loading.observe(this) {
            showSearingView(it)
        }
        vm.total.observe(this) {
            adapter.count = it
        }
    }

    fun showSearchResults(results: List<SearchData?>?, isLoadMore: Boolean) {
        if (isFinishing || isDestroyed) {
            return
        }
        showSearingView(false)
        if (isLoadMore) {
            if (results != null) {
                adapter.addData(results)
                if (results.size == vm.pageCount) {
                    adapter.loadMoreComplete()
                } else {
                    adapter.loadMoreEnd()
                }
            } else {
                adapter.loadMoreEnd()
            }
            return
        }
        adapter.setEnableLoadMore(results != null && results.size == vm.pageCount)
        adapter.setNewData(results)
        try {
            adapter.disableLoadMoreIfNotFullPage()
        } catch (e: Exception) {

        }
    }

    private fun showSearingView(isShow: Boolean) {
        if (isShow) {
            val fromDegrees = 0
            val toDegrees = 360
            val pivotX = 0.5f
            val pivotY = 0.5f
            val loadingAnimation = RotateAnimation(fromDegrees.toFloat(), toDegrees.toFloat(),
                    Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
            loadingAnimation.duration = 3000
            loadingAnimation.repeatCount = Animation.INFINITE
            loadingAnimation.interpolator = LinearInterpolator()
            views.searchingLayout.loadingIv.animation = loadingAnimation
            loadingAnimation.startNow()
            views.searchingLayout.root.visibility = View.VISIBLE
        } else {
            views.searchingLayout.loadingIv.clearAnimation()
            views.searchingLayout.root.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // requestFocus()
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

    fun search(keyword: String) {
        vm.searchGroupMessage(keyword, roomId, adapter.data.size)
        showSearingView(true)
    }

    companion object {
        private const val KEY_KEYWORD = "KEY_KEYWORD"
        private const val KEY_ROOM_ID = "KEY_ROOM_ID"
        private const val KEY_PREFIX = "KEY_PREFIX"
        private const val KEY_COUNT = "KEY_COUNT"

        fun start(
                context: Context,
                roomId: String?,
                keyword: String?,
                prefix: String?,
                count: Int
        ) {
            val intent = Intent(context, ChatRecordSearchActivity::class.java)
            intent.putExtra(KEY_ROOM_ID, roomId)
            intent.putExtra(KEY_KEYWORD, keyword)
            intent.putExtra(KEY_PREFIX, prefix)
            intent.putExtra(KEY_COUNT, count)
            context.startActivity(intent)
        }
    }

}
