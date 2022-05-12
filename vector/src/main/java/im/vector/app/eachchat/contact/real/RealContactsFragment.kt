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

package im.vector.app.eachchat.contact.real

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.fragmentViewModel
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentRealContactsLayoutBinding
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.ContactSyncUtils
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.contact.invite.InviteActivity
import im.vector.app.eachchat.contact.mycontacts.MyContactsActivity
import im.vector.app.eachchat.department.DepartmentActivity
import im.vector.app.eachchat.utils.AppCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RealContactsFragment @Inject constructor()  : VectorBaseFragment<FragmentRealContactsLayoutBinding>() {

    private val session = BaseModule.getSession()

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
        activity?.application?.let { ContactSyncUtils.getInstance().init(requireContext(), it) }
        initRecyclerView()
        initListener()
        setupToolbar()
        observeData()
        checkBookSwitch()
        initOrg()
        realContactsViewModel.loadCloseContacts(this)
    }

    private fun setupToolbar() {
        // setupToolbar(views.groupToolbar)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initOrg() {
        if (!AppCache.getIsOpenOrg()) {
            header.findViewById<TextView>(R.id.org_tv).text = getString(R.string.team)
            header.findViewById<ImageView>(R.id.org_iv).setImageDrawable(activity?.getDrawable(R.drawable.ic_team))
        }
    }

    private fun initListener() {
        adapter.setOnItemClickListener { adapter, _, position ->
            val user: User = adapter.getItem(position) as User
            kotlin.runCatching {
                lifecycleScope.launch (Dispatchers.IO) {
                    if (user.roomId != null) {
                        navigator.openRoom(requireContext(), user.roomId!!)
                    } else {
                        val existingRoomId = user.matrixId?.let { userId ->
                            session.getExistingDirectRoomWithUser(userId)
                        }
                        if (existingRoomId != null) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                navigator.openRoom(requireContext(), existingRoomId)
                            }
                        }
                    }
                }
            }
        }
        header.findViewById<LinearLayout>(R.id.invite_ll).setOnClickListener {
            InviteActivity.start(requireContext())
        }
        header.findViewById<LinearLayout>(R.id.my_contact_ll).setOnClickListener {
            MyContactsActivity.start(requireContext())
        }
//        header.group_chat_ll.setOnClickListener {
//            navigationTo(Contact.ContactsGroupChatActivity)
//        }
        header.findViewById<LinearLayout>(R.id.org_ll).setOnClickListener {
            DepartmentActivity.start(
                requireContext(),
                getString(R.string.organization_framework),
                null,
                true
            )
        }
//        views.backLayout.setOnClickListener {
//            requireActivity().onBackPressed()
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
        // val orgLayout = header.findViewById<LinearLayout>(R.id.org_ll)
        val myContactsLayout = header.findViewById<LinearLayout>(R.id.my_contact_ll)
        // val groupChatLayout = header.findViewById<LinearLayout>(R.id.group_chat_ll)
//        realContactsViewModel.isOpenOrgLiveData.observe(viewLifecycleOwner) {
//            if (it == true) {
//                orgLayout.visibility = View.VISIBLE
//            } else {
//                orgLayout.visibility = View.GONE
//            }
//        }
        realContactsViewModel.isOpenContactLiveData.observe(viewLifecycleOwner) {
            if (it == true) {
                myContactsLayout.visibility = View.VISIBLE
                contactActionView?.visibility = View.VISIBLE
            } else {
                myContactsLayout.visibility = View.GONE
                contactActionView?.visibility = View.GONE
            }
        }
//        realContactsViewModel.isOpenGroupLiveData.observe(viewLifecycleOwner) {
//            if (it == true) {
//                groupChatLayout.visibility = View.VISIBLE
//            } else {
//                groupChatLayout.visibility = View.GONE
//            }
//        }
    }

    private fun initRecyclerView() {
        views.realContactRv.layoutManager = LinearLayoutManager(requireContext())
        adapter.setHasStableIds(true)
        views.realContactRv.adapter = adapter
        adapter.removeAllHeaderView()
        adapter.addHeaderView(header)
    }

    private fun nullableCheck(): Boolean {
        return isFinishing() || isDetached || activity == null
    }

    private fun checkBookSwitch() {
        realContactsViewModel.getGMSConfigCache()
        realContactsViewModel.getGMSConfig()
    }
}
