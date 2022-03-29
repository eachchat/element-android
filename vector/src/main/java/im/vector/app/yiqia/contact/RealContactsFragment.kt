package im.vector.app.yiqia.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentRealContactsLayoutBinding
import im.vector.app.databinding.FragmentRoomMemberListBinding
import im.vector.app.yiqia.contact.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.query.RoomCategoryFilter
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import javax.inject.Inject

class RealContactsFragment @Inject constructor()  : VectorBaseFragment<FragmentRealContactsLayoutBinding>() {
    @Inject lateinit var activeSessionHolder: ActiveSessionHolder

    private val session by lazy {
        activeSessionHolder.getActiveSession()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRealContactsLayoutBinding {
        return FragmentRealContactsLayoutBinding.inflate(inflater, container, false)
    }

    private val realContactsViewModel: RealContactsViewModel by fragmentViewModel()
    private val adapter = RealContactsAdapter()
    private var contactActionView: View? = null
    private val header by lazy {
        layoutInflater.inflate(
            R.layout.fragment_real_contacts_header_layout,
            views.realContactRv,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        views.titleBar.setLeftVisible(false)
                // .set(R.string.footer_menu_contact)
//        views.titleBar.setLeftClickListener {
//                activity?.onBackPressed()
//            }.apply {
//                addAction(object : TitleBar.ImageAction(R.mipmap.ic_search) {
//                    override fun performAction(view: View?) {
//                        if (nullableCheck()) return
//                        navigationTo(App.ContactsSearchActivity)
//                    }
//                })
//                contactActionView = addAction(object :
//                    TitleBar.ImageAction(R.mipmap.m_base_home_more_add_contact_icon) {
//                    override fun performAction(view: View?) {
//                        Contact.addContactHomeActivity()
//                    }
//
//                })

//                contactActionView = addAction(object :
//                    TitleBar.ImageAction(ai.workly.eachchat.android.chat.R.mipmap.m_base_home_more_icon) {
//                    override fun performAction(view: View?) {
//                        view?.let {
//                            ContactMoreFunctionPopup(
//                                requireActivity(),
//                                requireContext(),
//                                it
//                            ).show()
//                        }
//                    }
//                })
           // }
        initRecyclerView()
        initListener()
        observeData()
        realContactsViewModel.loadCloseContacts(this)
    }

    private fun initListener() {
        adapter.setOnItemClickListener { adapter, _, position ->
            val user: User = adapter.getItem(position) as User
            kotlin.runCatching {
                lifecycleScope.launch (Dispatchers.IO) {
                    val existingRoomId = user.matrixId?.let { userId ->
                        session.getExistingDirectRoomWithUser(userId)
                    }
                    if (existingRoomId != null) {
                        lifecycleScope.launch (Dispatchers.Main) {
                            navigator.openRoom(requireContext(), existingRoomId)
                        }
                    }
                }
            }
        }
//        header.invite_ll.setOnClickListener {
//            navigationTo(Contact.RoomInviteActivity)
//        }
//        header.my_contact_ll.setOnClickListener {
//            navigationTo(Contact.MyContactsActivity)
//        }
//        header.group_chat_ll.setOnClickListener {
//            navigationTo(Contact.ContactsGroupChatActivity)
//        }
//        header.org_ll.setOnClickListener {
//            DepartmentActivity.start(
//                requireContext(),
//                getString(R.string.organization_framework),
//                null,
//                true
//            )
//        }
    }

    private fun observeData() {
        realContactsViewModel.closeContactData.observe(viewLifecycleOwner) {
            adapter.setNewData(it)
        }
//        contactsViewModel.size.observe(viewLifecycleOwner) {
//            header.invite_count_tv.isVisible = it > 0
//            header.invite_count_tv.text = it.toString()
//        }
        val orgLayout = header.findViewById<LinearLayout>(R.id.org_ll)
        val myContactsLayout = header.findViewById<LinearLayout>(R.id.my_contact_ll)
        val groupChatLayout = header.findViewById<LinearLayout>(R.id.group_chat_ll)
        realContactsViewModel.isOpenOrgLiveData.observe(viewLifecycleOwner) {
            if (it == true) {
                orgLayout.visibility = View.VISIBLE
            } else {
                orgLayout.visibility = View.GONE
            }
        }
        realContactsViewModel.isOpenContactLiveData.observe(viewLifecycleOwner) {
            if (it == true) {
                myContactsLayout.visibility = View.VISIBLE
                contactActionView?.visibility = View.VISIBLE
            } else {
                myContactsLayout.visibility = View.GONE
                contactActionView?.visibility = View.GONE
            }
        }
        realContactsViewModel.isOpenGroupLiveData.observe(viewLifecycleOwner) {
            if (it == true) {
                groupChatLayout.visibility = View.VISIBLE
            } else {
                groupChatLayout.visibility = View.GONE
            }
        }
    }

    private fun initRecyclerView() {
        views.realContactRv.layoutManager = LinearLayoutManager(requireContext())
        views.realContactRv.adapter = adapter
        adapter.removeAllHeaderView()
        adapter.addHeaderView(header)
    }

//    private fun nullableCheck(): Boolean {
//        return isFinishing || isDetached || activity == null
//    }
//
//    fun checkBookSwitch() {
//        realContactsViewModel.getGMSConfigCache()
//        realContactsViewModel.getGMSConfig()
//    }


}
