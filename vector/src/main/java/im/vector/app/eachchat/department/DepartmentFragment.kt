package im.vector.app.eachchat.department

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import im.vector.app.R
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.utils.DimensionConverter
import im.vector.app.databinding.DepartmentFragmentBinding
import im.vector.app.eachchat.contact.api.bean.Department
import im.vector.app.eachchat.department.adapter.DepartmentAdapter
import im.vector.app.eachchat.department.data.DepartmentUserBean
import im.vector.app.eachchat.department.data.IDisplayBean
import im.vector.app.eachchat.rx.SimpleObserver
import im.vector.app.eachchat.ui.LineDecoration
import im.vector.app.eachchat.ui.breadcrumbs.BreadDepartmentItem
import im.vector.app.eachchat.ui.breadcrumbs.BreadcrumbsView
import im.vector.app.eachchat.ui.breadcrumbs.DefaultBreadcrumbsCallback
import im.vector.app.eachchat.ui.index.IndexView
import im.vector.app.eachchat.ui.stickyHeader.StickyHeaderDecoration
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.features.home.HomeActivity
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Collections
import javax.inject.Inject

/**
 * Created by zhouguanjie on 2019/9/10.
 */
class DepartmentFragment @Inject constructor(

): SelectBaseFragment<DepartmentFragmentBinding>(), DepartmentContract.View {
    var mRecyclerView: RecyclerView? = null
    var mNoMemberView: View? = null
    private var mAdapter: DepartmentAdapter? = null
    private var mPresenter: DepartmentContract.Presenter? = null
    var name: String? = null
    var departmentId: String? = null
    var title: String? = null
    private var addContactBreadcrumbs = true
    var showTitle = false
    var showChooseAll = false
    var showSlideMenu = false
    var isShowSearch = true // 是否显示搜索按钮: 从成员详情进来不需要显示

    //https://github.com/HamidrezaAmz/BreadcrumbsView
    var breadcrumbsView: BreadcrumbsView? = null
    var mIndexView: IndexView? = null
    var mIndexTV: TextView? = null
    var btnAddContacts: FloatingActionButton? = null
    private var mManager: LinearLayoutManager? = null
     private val observer: ContactAvatarObserver = ContactAvatarObserver()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.department_fragment, container, false)
        // Fix click the bottom empty place will be clicked of
        // the previous fragment, RecentFragment that is.
        // This may cause that when the user click the empty place in the current page,
        // but the same position of RecentFragment's user is clicked
        view.setOnClickListener { }
        mRecyclerView = view.findViewById(R.id.recycler_view)
        mNoMemberView = view.findViewById(R.id.department_no_member_layout)
        breadcrumbsView = view.findViewById(R.id.breadcrumbs_view)
        mIndexView = view.findViewById(R.id.index_view)
        mIndexTV = view.findViewById(R.id.index_tv)
        if (!AppCache.getIsOpenOrg()) {
            view.findViewById<TextView>(R.id.groupToolbarTitleView).text = getString(R.string.team)
            view.findViewById<RelativeLayout>(R.id.backLayout).onClick {
                activity?.onBackPressed()
            }
        }
//        btnAddContacts = view.findViewById(R.id.btn_add_contacts)
        // btnAddContacts.setOnClickListener(View.OnClickListener { v: View? -> addContacts() })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initRecyclerView()
        initBreadcrumbsView()
        initPresenter()
        setupToolbar(views.groupToolbar)
    }

    private fun initData() {
        val bundle = arguments ?: return
        title = bundle.getString(TITLE_PARAM)
        name = bundle.getString(DEPARTMENT_NAME_PARAM)
        departmentId = bundle.getString(DEPARTMENT_ID_PARAM)
        val departments: List<Department>? = bundle.getParcelableArrayList(DEPARTMENTS_PARAM)
        showTitle = bundle.getBoolean(SHOW_TITLE_PARAM, true)
        showSlideMenu = bundle.getBoolean(SHOW_SLIDE_MENU_PARAM, false)
        showChooseAll = bundle.getBoolean(SHOW_CHOOSE_ALL, true)
        isShowSearch = bundle.getBoolean(DEPARTMENT_IS_SHOW_SEARCH_PARAM, true)
        if (departments != null && departments.isNotEmpty()) {
            name = departments[0].displayName
            departmentId = departments[0].id
        }
        addContactBreadcrumbs = bundle.getBoolean(ADD_CONTACT_BREADCRUMBS, true)
    }

    private fun initRecyclerView() {
        mManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = mManager
        mAdapter = DepartmentAdapter(context, if (TextUtils.equals(departmentId, ROOT_ID)) activity else null, viewLifecycleOwner)
        mAdapter?.setOnClickListener { v ->
            if (nullableCheck()) {
                return@setOnClickListener
            }
            val department: IDisplayBean = v.getTag() as IDisplayBean
            if (false) { //  && activity is HomeActivity
//                if (TextUtils.equals(department.getId(), GROUP_CHAT_ID)) {
//                    Navigation.INSTANCE.navigationTo(Contact.ContactsGroupChatActivity)
//                    return@setOnClickListener
//                }
                DepartmentActivity.start(requireContext(), getString(R.string.organization), null, true)
                return@setOnClickListener
            }
            val mgr = requireActivity().supportFragmentManager
            val ft = mgr.beginTransaction()
            val departmentFragment = DepartmentFragment()
            val bundle = Bundle()
            bundle.putBoolean(ADD_CONTACT_BREADCRUMBS, addContactBreadcrumbs)
            bundle.putString(DEPARTMENT_ID_PARAM, department.getId())
            bundle.putString(DEPARTMENT_NAME_PARAM, department.getMainContent())
            bundle.putString(TITLE_PARAM, department.getMainContent())
            bundle.putBoolean(SHOW_SLIDE_MENU_PARAM, showSlideMenu)
            bundle.putBoolean(DEPARTMENT_IS_SHOW_SEARCH_PARAM, isShowSearch)
            departmentFragment.arguments = bundle
            ft.replace(R.id.contacts_fragment_container, departmentFragment, department.getMainContent())
            ft.addToBackStack(department.getMainContent())
            ft.commit()
        }
        mRecyclerView!!.adapter = mAdapter
        mIndexView?.setOnCharIndexChangedListener(object : IndexView.OnCharIndexChangedListener {
            override fun onCharIndexChanged(currentIndex: Char) {
                val pos: Int? = mAdapter?.getPosByIndex(currentIndex)
                if (pos != null) {
                    if (pos >= 0) {
                        mManager!!.scrollToPositionWithOffset(pos, 0)
                    }
                }
            }

            override fun onCharIndexSelected(currentIndex: String?, curPos: Int) {
                if (currentIndex != null) {
                    val params: RelativeLayout.LayoutParams = mIndexTV!!.layoutParams as RelativeLayout.LayoutParams
                    params.setMargins(0, (mIndexView!!.top + curPos * mIndexView!!.itemHeight + mIndexView!!.itemHeight / 2).toInt(),
                            params.marginEnd, 0)
                    mIndexTV!!.layoutParams = params
                    mIndexTV!!.text = currentIndex
                    mIndexTV!!.visibility = View.VISIBLE
                } else {
                    mIndexTV!!.visibility = View.GONE
                }
            }
        })
        mRecyclerView!!.addItemDecoration(StickyHeaderDecoration(mAdapter))
        mRecyclerView!!.addItemDecoration(LineDecoration(mAdapter, DimensionConverter(resources).dpToPx(66)))
    }

    private fun initBreadcrumbsView() {
        if (nullableCheck()) {
            return
        }

        // Add root
        if (addContactBreadcrumbs) {
            breadcrumbsView?.addItem(BreadDepartmentItem.createItem(
                    Department(ROOT_ID, getString(R.string.contacts_book))))
        }
        var departmentHome = Department(null, getString(R.string.organization_framework))
        if(!AppCache.getIsOpenOrg()) {
            departmentHome = Department(null, getString(R.string.team))
        }

        departmentHome.setParentId(ROOT_ID)
        breadcrumbsView?.addItem(BreadDepartmentItem.createItem(departmentHome))

        //从数据库搜索当前部门的上级部门
        Observable.create<List<Department>?>(ObservableOnSubscribe<List<Department>?> { emitter: ObservableEmitter<List<Department>?> ->
            val results: MutableList<Department> = ArrayList<Department>()
            var department: Department? = departmentId?.let { DepartmentStoreHelper.getDepartmentById(it) }
            if (department != null && !TextUtils.isEmpty(department.getParentId())) {
                results.add(department)
                while (true) {
                    department = department?.getParentId()?.let { DepartmentStoreHelper.getDepartmentById(it) }
                    if (department != null && !TextUtils.isEmpty(department.getParentId())) {
                        results.add(department)
                        continue
                    }
                    break
                }
            }
            emitter.onNext(results)
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SimpleObserver<List<Department?>?>() {
                    override fun onNext(departments: List<Department?>?) {
                        if (nullableCheck()) {
                            return
                        }
                        if (departments != null && departments.size > 0) {
                            for (index in departments.indices.reversed()) {
                                breadcrumbsView?.addItem(BreadDepartmentItem.createItem(
                                        Department(departments[index]?.getId(), departments[index]?.getDisplayName())))
                            }
                            breadcrumbsView?.setVisibility(View.VISIBLE)
                            btnAddContacts?.setVisibility(View.GONE)
                        } else if (TextUtils.equals(departmentId, ROOT_ID)) {
                            breadcrumbsView?.setVisibility(View.GONE)
                            btnAddContacts?.setVisibility(View.VISIBLE)
                        } else if (!addContactBreadcrumbs && TextUtils.isEmpty(departmentId)) {
                            breadcrumbsView?.setVisibility(View.GONE)
                            btnAddContacts?.setVisibility(View.GONE)
                        } else {
                            breadcrumbsView?.setVisibility(View.VISIBLE)
                            btnAddContacts?.setVisibility(View.GONE)
                        }
//                        if (!AppCache.getIsOpenOrg()) {
//                            breadcrumbsView?.isVisible = false
//                        }
                    }
                })
        breadcrumbsView?.setCallback(object : DefaultBreadcrumbsCallback<BreadDepartmentItem?>() {
            override fun onNavigateBack(item: BreadDepartmentItem?, position: Int) {
                val mgr = activity!!.supportFragmentManager
                val ft = mgr.beginTransaction()
                val fragment: Fragment
                if (TextUtils.equals(item?.items?.get(item.selectedIndex)?.id, ROOT_ID)) {
                    requireActivity().finish()
                    return
                }
                val bundle = Bundle()
                bundle.putBoolean(ADD_CONTACT_BREADCRUMBS, addContactBreadcrumbs)
                if (activity is HomeActivity || activity is DepartmentActivity) {
                    fragment = DepartmentFragment()
                    bundle.putString(DEPARTMENT_ID_PARAM, item?.items?.get(item.selectedIndex)?.getId())
                    bundle.putString(DEPARTMENT_NAME_PARAM, item?.selectedItem)
                    if (TextUtils.equals(title, getString(R.string.footer_menu_contact))) {
                        bundle.putString(TITLE_PARAM, title)
                        bundle.putBoolean(SHOW_SLIDE_MENU_PARAM, true)
                    } else {
                        bundle.putString(TITLE_PARAM, item?.getSelectedItem())
                    }
                    fragment.arguments = bundle
                    ft.replace(R.id.contacts_fragment_container, fragment, item?.selectedItem)
                    ft.commitAllowingStateLoss()
                } else {
//                    fragment = SelectDepartmentUserFragment()
//                    if (TextUtils.equals(item.getItems().get(item.getSelectedIndex()).getId(), ROOT_ID)) {
//                        mgr.popBackStack(null, 1)
//                        return
//                    } else {
//                        bundle.putString(TITLE_PARAM, title)
//                        bundle.putString(DEPARTMENT_ID_PARAM, item.getItems().get(item.getSelectedIndex()).getId())
//                        bundle.putString(DEPARTMENT_NAME_PARAM, item.getItems().get(item.getSelectedIndex()).getDisplayName())
//                    }
                }
                //                mgr.popBackStack(item.getSelectedItem(), 0);
//                fragment.arguments = bundle
//                ft.replace(R.id.contacts_fragment_container, fragment, item?.selectedItem)
//                ft.commitAllowingStateLoss()
            }

            override fun onNavigateNewLocation(newItem: BreadDepartmentItem?, changedPosition: Int) {}
        })
    }

    private fun initPresenter() {
        if (activity == null) {
            return
        }
        if (requireActivity().intent == null) {
            requireActivity().finish()
        }
        mPresenter = DepartmentPresenter(this, departmentId)
    }

    override fun showToast(message: String?, isError: Boolean) {

    }

    override fun showLoading(loadingText: String?) {

    }

    override fun dismissLoading() {

    }

    override fun showData(departments: List<IDisplayBean>, isAllUser: Boolean, showMembersTagPos: Int) {
        if (nullableCheck()) {
            return
        }
        if (isAllUser) {
            Observable.create<List<DepartmentUserBean>?>(ObservableOnSubscribe<List<DepartmentUserBean>?> { emitter: ObservableEmitter<List<DepartmentUserBean>?> ->
                val users: MutableList<DepartmentUserBean> = ArrayList<DepartmentUserBean>()
                for (bean in departments) {
                    if (bean !is DepartmentUserBean) {
                        continue
                    }
                    users.add(bean)
                }
                Collections.sort(users)
                emitter.onNext(users)
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SimpleObserver<List<DepartmentUserBean>?>() {
                        override fun onNext(results: List<DepartmentUserBean>?) {
                            if (nullableCheck()) {
                                return
                            }
                            mIndexView?.setCHARSAsyncEx(results)
                            mIndexView?.setVisibility(View.VISIBLE)
                            observer.clearObserver(this@DepartmentFragment)
                            if (results != null) {
                                val users: List<IDisplayBean> = ArrayList(results)
                                mAdapter?.setDepartments(results)
                                mAdapter?.let { observer.observer(viewLifecycleOwner, it, users, TextUtils.equals(departmentId, ROOT_ID)) }
                            }
                            mAdapter?.setAllUser(true)
                        }
                    })
            return
        }
        Collections.sort(departments, java.util.Comparator { o1: IDisplayBean, o2: IDisplayBean? ->
            if (o1 is DepartmentUserBean && o2 is DepartmentUserBean) {
                val d1: DepartmentUserBean = o1
                val d2: DepartmentUserBean? = o2
                if (d2 != null) {
                    return@Comparator d1.compareTo(d2)
                }
            }
            0
        })
        mNoMemberView!!.visibility = if (departments.size == 0) View.VISIBLE else View.GONE
        mIndexView?.setCHARSAsyncEx(departments)
        mIndexView?.setVisibility(View.VISIBLE)
        mAdapter?.setShowMembersTagPos(showMembersTagPos)
        observer.clearObserver(this@DepartmentFragment)
        mAdapter?.setDepartments(departments)
        mAdapter?.let { observer.observer(viewLifecycleOwner, it, departments, TextUtils.equals(departmentId, ROOT_ID)) }
    }

//    fun addContacts() {
//        Contact.INSTANCE.addContactHomeActivity()
//    }

    @SuppressLint("NotifyDataSetChanged")
    override fun refresh() {
        if (nullableCheck()) {
            return
        }
        mAdapter?.notifyDataSetChanged()
    }

    fun refreshData() {
        if (mPresenter == null ||
                !TextUtils.equals(departmentId, ROOT_ID) || mAdapter == null || nullableCheck()) return
        mPresenter?.initData(departmentId)
    }

    private fun nullableCheck(): Boolean {
        return isFinishing() ||
                isDetached || activity == null || mRecyclerView == null
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): DepartmentFragmentBinding {
        return DepartmentFragmentBinding.inflate(inflater, container, false)
    }

    // override val mavericksViewInternalViewModel: MavericksViewInternalViewModel<EmptyViewState> by fragmentViewModel()

    companion object {
        const val ROOT_ID = "each_chat_android_contacts_root"
        const val GROUP_CHAT_ID = "group_chat"
        const val DEPARTMENT_ID_PARAM = "department_id_param"
        const val DEPARTMENT_NAME_PARAM = "department_name_param"
        const val DEPARTMENTS_PARAM = "departments_param"
        const val TITLE_PARAM = "title_param"
        const val DEPARTMENT_IS_SHOW_SEARCH_PARAM = "department_is_show_search_param"

        //需要从数据库搜索上级部门 填充面包屑
        const val ADD_CONTACT_BREADCRUMBS = "key_show_contact_breadcrumbs"
        const val SHOW_TITLE_PARAM = "show_title_param"
        const val SHOW_CHOOSE_ALL = "show_choose_all"
        const val SHOW_SLIDE_MENU_PARAM = "show_slide_menu_param"
    }
}
