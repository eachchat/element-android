/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.eachchat.department

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import com.blankj.utilcode.util.ObjectUtils
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.ContactFragmentBinding
import im.vector.app.eachchat.contact.addcontact.ContactAddHomeActivity
import im.vector.app.eachchat.contact.api.bean.Department
import im.vector.app.eachchat.contact.manage.ContactManageActivity
import im.vector.app.eachchat.search.contactsearch.searchmore.SearchMoreActivity

@AndroidEntryPoint
open class DepartmentActivity: VectorBaseActivity<ContactFragmentBinding>() {

    private var contactMode = false

    override fun getBinding(): ContactFragmentBinding {
        return ContactFragmentBinding.inflate(layoutInflater)
    }

    override fun getMenuRes() = R.menu.org

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_org_filter      -> {
                SearchMoreActivity.start(this, null, false, SearchMoreActivity.SEARCH_MORE_TYPE_IN_DEPARTMENT, needFocus = true)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    companion object {
        private const val KEY_CONTACT_MODE = "key_contact_mode"
        private const val KEY_DEPARTMENT_NAME = "key_department_name"
        private const val KEY_DEPARTMENT_ID = "key_department_id"
        private const val KEY_DEPARTMENTS = "key_departments"
        private const val KEY_IS_SHOW_SEARCH = "key_is_show_search"

        fun start(context: Context, departmentName: String?, departmentId: String?) {
            start(context, departmentName, departmentId, false)
        }

        fun start(context: Context, departments: java.util.ArrayList<Department?>?) {
            val intent = Intent(context, DepartmentActivity::class.java)
            intent.putParcelableArrayListExtra(KEY_DEPARTMENTS, departments)
            context.startActivity(intent)
        }

        fun start(context: Context, departmentName: String?, departmentId: String?, contactMode: Boolean) {
            val intent = Intent(context, DepartmentActivity::class.java)
            intent.putExtra(KEY_DEPARTMENT_NAME, departmentName)
            intent.putExtra(KEY_DEPARTMENT_ID, departmentId)
            intent.putExtra(KEY_CONTACT_MODE, contactMode)
            context.startActivity(intent)
        }
    }

    protected fun init() {
        var name: String? = null
        var departmentId: String? = null
        var departments: ArrayList<Department?>? = null
        var isShowSearch = true
        if (intent != null) {
            name = intent.getStringExtra(KEY_DEPARTMENT_NAME)
            departmentId = intent.getStringExtra(KEY_DEPARTMENT_ID)
            contactMode = intent.getBooleanExtra(KEY_CONTACT_MODE, false)
            departments = intent.getParcelableArrayListExtra(KEY_DEPARTMENTS)
            isShowSearch = intent.getBooleanExtra(KEY_IS_SHOW_SEARCH, true)
        }
        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        val fragment: DepartmentFragment
        val bundle = Bundle()
        bundle.putBoolean(DepartmentFragment.ADD_CONTACT_BREADCRUMBS, contactMode)
        if (departments != null) {
            fragment = DepartmentFragment()
            bundle.putParcelableArrayList(DepartmentFragment.DEPARTMENTS_PARAM, departments)
            if (!ObjectUtils.isEmpty(departments) && departments[0] != null && !TextUtils.isEmpty(departments[0]?.displayName)) {
                bundle.putString(DepartmentFragment.TITLE_PARAM, departments[0]?.displayName)
            } else {
                bundle.putString(DepartmentFragment.TITLE_PARAM, getString(R.string.department))
            }
            bundle.putBoolean(DepartmentFragment.DEPARTMENT_IS_SHOW_SEARCH_PARAM, isShowSearch)
        } else {
            bundle.putString(DepartmentFragment.DEPARTMENT_NAME_PARAM, name)
            bundle.putString(DepartmentFragment.DEPARTMENT_ID_PARAM, departmentId)
            bundle.putString(DepartmentFragment.TITLE_PARAM, name)
            bundle.putBoolean(DepartmentFragment.DEPARTMENT_IS_SHOW_SEARCH_PARAM, isShowSearch)
            fragment = DepartmentFragment()
        }
        fragment.arguments = bundle
        ft.replace(R.id.contacts_fragment_container, fragment, name)
        ft.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if (contactMode) {
            var backHandled = false
            val fragments = supportFragmentManager.fragments
            if (fragments.size > 0) {
                val fragment = fragments[fragments.size - 1]
                if (fragment is VectorBaseFragment<*> && fragment.backHandler()) {
                    backHandled = true
                }
            }
            if (!backHandled) super.onBackPressed()
        } else {
            finish()
        }
    }
}
