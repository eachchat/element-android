package im.vector.app.eachchat.search.contactsearch.searchmore

import ai.workly.eachchat.android.search.adapter.ContactsSearchAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import im.vector.app.R
import im.vector.app.eachchat.search.contactsearch.ContactsSearchActivity

class SearchMoreActivity : ContactsSearchActivity() {
    var keyword: String? = ""
    private var needShowBack: Boolean? = false
    var searchType: String? = null

    companion object {
        const val KEY_SEARCH_MORE_KEYWORD = "KEY_SEARCH_MORE_KEYWORD"
        const val KEY_SEARCH_MORE_NEED_SHOW_BACK = "KEY_SEARCH_MORE_NEED_SHOW_BACK"
        const val KEY_SEARCH_MORE_SEARCH_TYPE = "KEY_SEARCH_MORE_SEARCH_TYPE"
        const val SEARCH_MORE_TYPE_CONTACT = "SEARCH_MORE_TYPE_CONTACT"
        const val SEARCH_MORE_TYPE_DEPARTMENT = "SEARCH_MORE_TYPE_ORG"
        const val SEARCH_MORE_TYPE_ORG_MEMBER = "SEARCH_MORE_TYPE_ORG_MEMBER"
        const val SEARCH_MORE_TYPE_REAL_CONTACT = "SEARCH_MORE_TYPE_REAL_CONTACT"
        const val SEARCH_MORE_TYPE_IN_DEPARTMENT = "SEARCH_MORE_TYPE_IN_DEPARTMENT"

        fun start(context: Context, keyWord: String? = "", needShowBack: Boolean? = false, searchType: String? = null
        ) {
            val intent = Intent(context, SearchMoreActivity::class.java)
            intent.putExtra(KEY_SEARCH_MORE_KEYWORD, keyWord)
            intent.putExtra(KEY_SEARCH_MORE_NEED_SHOW_BACK, needShowBack)
            intent.putExtra(KEY_SEARCH_MORE_SEARCH_TYPE, searchType)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyword = intent.getStringExtra(KEY_SEARCH_MORE_KEYWORD)
        needShowBack = intent.getBooleanExtra(KEY_SEARCH_MORE_NEED_SHOW_BACK, false)
        searchType = intent.getStringExtra(KEY_SEARCH_MORE_SEARCH_TYPE)
        initSearchTitle()
    }

    private fun initSearchTitle() {
        if (!keyword.isNullOrBlank()) {
            views.searchTitleBar.etSearch.setText(keyword)
        }
        views.searchTitleBar.etSearch.setHint(getHint(searchType))
        if (needShowBack == true) {
            views.searchTitleBar.backIv.visibility = View.VISIBLE
        }
    }

    private fun getHint(searchType: String?): String{
        return when(searchType) {
            SEARCH_MORE_TYPE_CONTACT    -> { getString(R.string.search_contact) }
            SEARCH_MORE_TYPE_DEPARTMENT -> { getString(R.string.search_department) }
            SEARCH_MORE_TYPE_ORG_MEMBER -> { getString(R.string.search_org) }
            SEARCH_MORE_TYPE_REAL_CONTACT -> { getString(R.string.search_real_contact) }
            SEARCH_MORE_TYPE_IN_DEPARTMENT -> { getString(R.string.search_org) }
            else -> { getString(R.string.search) }
        }
    }

    override fun onBackPressed() {
        searchJob?.cancel()
        finish()
    }

    override fun showSearchResults(results: List<ContactsSearchAdapter.BaseItem>?) {
        if (isFinishing || isDestroyed) {
            return
        }
        showSearingView(false)
        adapter.setNewData(results)
    }

    override fun requestFocus() {
        //do nothing
    }

    override fun doSearch(keyWord: String) {
        searchJob?.cancel()
        adapter.setKeyword(keyWord)
        searchJob = when(searchType) {
            SEARCH_MORE_TYPE_CONTACT    -> { vm.searchMyContactJob(keyWord) }
            SEARCH_MORE_TYPE_DEPARTMENT -> { vm.searchDepartmentJob(keyWord) }
            SEARCH_MORE_TYPE_ORG_MEMBER -> { vm.searchOrgJob(keyWord) }
            SEARCH_MORE_TYPE_REAL_CONTACT -> { vm.searchRealContactJob(keyWord) }
            SEARCH_MORE_TYPE_IN_DEPARTMENT -> { vm.searchInOrganizationJob(keyWord) }
            else -> { vm.searchDepartmentJob(keyWord) }
        }
    }
}
