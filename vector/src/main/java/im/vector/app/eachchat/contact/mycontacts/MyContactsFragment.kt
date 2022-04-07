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

package im.vector.app.eachchat.contact.mycontacts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.fragmentViewModel
import im.vector.app.core.platform.VectorBaseBottomSheetDialogFragment.ResultListener.Companion.RESULT_OK
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentMyContactsBinding
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.ui.index.IndexView
import im.vector.app.eachchat.ui.stickyHeader.StickyHeaderDecoration
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyContactsFragment @Inject constructor(
) : VectorBaseFragment<FragmentMyContactsBinding>() {
    private val adapter = MyContactsAdapter()
    private var contactsLiveData: LiveData<List<ContactsDisplayBeanV2?>?>? =
            AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().getContactsLiveData()

    private val viewModel: MyContactViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyContactsBinding {
        return FragmentMyContactsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initIndex()
        initListener()
        observeData()
//        loadingText = getString(R.string.loading_my_contacts)

        viewModel.loading.postValue(true)
        // viewModel.loadMyContactsOffset()
        contactsLiveData?.observe(viewLifecycleOwner) {
            viewModel.loadMyContacts(it)
        }
    }

    private fun initListener() {
//        adapter.setOnItemClickListener { adapter, _, position ->
//            val user: User = adapter.getItem(position) as User
//            // 是联系人跳联系人详情; 不是联系人跳成员详情
//            if (TextUtils.isEmpty(user.id) && TextUtils.equals(
//                            user.departmentId,
//                            CONTACTS_DEPARTMENT_ID
//                    )
//            ) {
//                contactInfoActivityV2(user.contactId, this)
//            } else {
//                start(user.id)
//            }
//        }
    }

    private fun observeData() {
        viewModel.contactData.observe(viewLifecycleOwner) {
            //contactAvatarObserver.clearObserver(this)
//            val diffCallback = MyContactsDiffCallBack(it)
            adapter.setNewData(it)
            // if (dialog != null && dialog.isShowing) {
            //    views.myContactsRv.scrollToPosition(0)
            // }
            //contactAvatarObserver.observerContacts(this, adapter, it)
        }
        viewModel.indexChars.observe(viewLifecycleOwner) {
            views.indexView.setCHARS(it)
        }
        viewModel.directlyUpdateContactsLiveData.observe(viewLifecycleOwner) {
            it?.let { it1 -> adapter.setNewData(it1) }
        }
    }

    private fun initRecyclerView() {
        views.myContactsRv.layoutManager = LinearLayoutManager(requireContext())
        views.myContactsRv.adapter = adapter
        views.myContactsRv.addItemDecoration(StickyHeaderDecoration(adapter))
    }

    private fun initIndex() {
        views.indexView.setOnCharIndexChangedListener(object : IndexView.OnCharIndexChangedListener {
            override fun onCharIndexChanged(currentIndex: Char) {
                val pos = adapter.getPosByIndex(currentIndex)
                if (pos >= 0) {
                    views.myContactsRv.layoutManager!!.scrollToPosition(pos)
                }
            }

            override fun onCharIndexSelected(currentIndex: String?, curPos: Int) {
                if (currentIndex != null) {
                    val params = views.indexTv.layoutParams as RelativeLayout.LayoutParams
                    params.setMargins(
                            0, ((views.indexView.top + curPos * views.indexView.itemHeight
                            + views.indexView.itemHeight / 2).toInt()),
                            params.marginEnd, 0
                    )
                    views.indexTv.layoutParams = params
                    views.indexTv.text = currentIndex
                    views.indexTv.visibility = View.VISIBLE
                } else {
                    views.indexTv.visibility = View.GONE
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode != RESULT_OK || data == null) {
//            return
//        }
//        when (requestCode) {
//            RouterConstant.RequestContactInfoCode -> {
//                val contactId = data.getStringExtra(CONTACT_INFO)
//                if (data.getBooleanExtra(RouterConstant.ContactDeleted, false)) {
//                    deleteContact(contactId)
//                } else {
//                    updateContact(contactId)
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateContact(contactId: String?) {
        if (contactId.isNullOrBlank()) return
        viewModel.directlyLoadContact(contactId, adapter.data)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deleteContact(contactId: String?) {
        if (contactId.isNullOrBlank()) return
        GlobalScope.launch(Dispatchers.IO) {
            var index = 0
            while (index < adapter.data.size - 1) {
                index++
                if (adapter.data[index]?.contactId == contactId) {
                    adapter.data.removeAt(index)
                    viewModel.loadIndex(adapter.data.filterNotNull().toMutableList())
                    break
                }
            }
            GlobalScope.launch(Dispatchers.Main) {
                adapter.setNewData(adapter.data)
            }
        }
    }
}
