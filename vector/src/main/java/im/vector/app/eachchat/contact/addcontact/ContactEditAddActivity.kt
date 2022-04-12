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

package im.vector.app.eachchat.contact.addcontact

import ai.workly.eachchat.android.contact.edit.typeInput.TypeInputDialog
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.airbnb.mvrx.viewModel
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityContactEditAddBinding
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.ADD_FAIL
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.ADD_SUCCESS
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.EDIT_FAIL
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.EDIT_SUCCESS
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.INVALID_MATRIX_ID
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.REPEAT_MATRIX_ID
import im.vector.app.eachchat.contact.addcontact.ContactEditAddViewModel.Companion.UNKNOWN_HOST_EXCEPTION
import im.vector.app.eachchat.contact.data.AddressBean
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.DateBean
import im.vector.app.eachchat.contact.data.EditAddressList
import im.vector.app.eachchat.contact.data.EditDateList
import im.vector.app.eachchat.contact.data.EditEmailList
import im.vector.app.eachchat.contact.data.EditImppList
import im.vector.app.eachchat.contact.data.EditPhoneList
import im.vector.app.eachchat.contact.data.EditUrlList
import im.vector.app.eachchat.contact.data.EmailBean
import im.vector.app.eachchat.contact.data.ImppBean
import im.vector.app.eachchat.contact.data.TelephoneBean
import im.vector.app.eachchat.contact.data.UrlBean
import im.vector.app.eachchat.department.ContactUtils
import im.vector.app.eachchat.ui.view.pickerview.builder.TimePickerBuilder
import im.vector.app.eachchat.ui.view.pickerview.view.TimePickerView
import im.vector.app.eachchat.utils.ScreenUtils
import im.vector.app.eachchat.utils.ToastUtil
import im.vector.lib.ui.styles.dialogs.MaterialProgressDialog
import java.text.SimpleDateFormat
import java.util.Date

object RouterConstant {
//    const val RequestContactSettingsCode = 0xec01
//    const val RequestContactAddCode = 0xec02
//    const val RequestContactEditCode = 0xec03
//    const val RequestContactInfoCode = 0xec04
//    const val ContactDeleted = "ContactDeleted"
    const val ContactAfterEdited = "ContactAfterEdited"
    const val ContactAfterAdded = "ContactAfterAdded"
//    const val ContactPosition = "ContactPosition"
}

@AndroidEntryPoint
class ContactEditAddActivity: VectorBaseActivity<ActivityContactEditAddBinding>() {
    private val mPhoneList = ArrayList<ContactEditAddLayout>()
    private val mEmailList = ArrayList<ContactEditAddLayout>()
    private val mWebsiteList = ArrayList<ContactEditAddLayout>()
    private val mDateList = ArrayList<ContactEditAddLayout>()
    private val mImppList = ArrayList<ContactEditAddLayout>()
    private val mAddressList = ArrayList<AddressEditLayout>()

    private var emailTypes: List<String>? = null
    private var addressTypes: List<String>? = null
    private var phoneTypes: List<String>? = null
    private var dateTypes: List<String>? = null
    private var imppTypes: List<String>? = null
    private var websiteTypes: List<String>? = null

    var mode: Int = MODE_ADD //添加或编辑模式
    var id: String? = null
    var contact: ContactsDisplayBean? = null

    var dialog: AlertDialog? = null

    override fun getBinding(): ActivityContactEditAddBinding {
        return ActivityContactEditAddBinding.inflate(layoutInflater)
    }

    private val vm: ContactEditAddViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initClickListener()
        id?.let {
            vm.getContact(id!!)
        }

        vm.loading.observe(this) {
            if (it) {
                dialog = MaterialProgressDialog(this).show(getString(R.string.please_wait))
            } else {
                dialog?.dismiss()
            }
        }

//        views.titleBar.setLeftClickListener { finish() }.setImmersive(false)
//                .addAction(confirmAction)
        //hide some line
        views.layoutMatrixId.setViewLineVisible(false)
        views.layoutCompany.setViewLineVisible(false)
        views.layoutLabel.setViewLineVisible(false)
        views.layoutRemark.setViewLineVisible(false)
        views.layoutFamilyName.setViewLineVisible(false)
        //must fill in the family name and the given name
        views.layoutFamilyName.setRequired(true)
        views.layoutGivenName.setRequired(true)
        //disable confirm when family name or given name is empty
        val confirmText = views.confirmTv
        confirmText.isEnabled = false
        confirmText.onClick {
            addContact()
        }
        confirmText.setTextColor(ContextCompat.getColor(this, R.color.ff999999))
        views.layoutFamilyName.etEdit.doAfterTextChanged {
            if (it.isNullOrBlank() || views.layoutGivenName.etEdit.text.isNullOrBlank()) {
                confirmText.isEnabled = false
                confirmText.setTextColor(ContextCompat.getColor(this, R.color.ff999999))
            } else {
                confirmText.isEnabled = true
                confirmText.setTextColor(ContextCompat.getColor(this, R.color.green_text))
            }
        }
        views.layoutGivenName.etEdit.doAfterTextChanged {
            if (it.isNullOrBlank() || views.layoutFamilyName.etEdit.text.isNullOrBlank()) {
                confirmText.isEnabled = false
                confirmText.setTextColor(ContextCompat.getColor(this, R.color.ff999999))
            } else {
                confirmText.isEnabled = true
                confirmText.setTextColor(ContextCompat.getColor(this, R.color.green_text))
            }
        }

        vm.contactLiveData.observe(this) {
            it?.let {
                initData(it)
                addEmptyLayout()
                checkNeedShowMore()
            }
        }

        //deal with kinds of result
        vm.contactEditAddStatues.observe(this) { status ->
            // dismissLoading()
            when (status) {
                ADD_SUCCESS -> {
                    ToastUtil.showSuccess(this, R.string.add_contact_success)
                    setResult(
                            RESULT_OK,
                            intent.also { it.putExtra(RouterConstant.ContactAfterAdded, true) })
                    finish()
                }
                ADD_FAIL -> ToastUtil.showError(this, R.string.add_contact_failed)
                EDIT_SUCCESS -> {
                    ToastUtil.showSuccess(this, R.string.edit_contact_success)
                    setResult(
                            RESULT_OK,
                            intent.also { it.putExtra(RouterConstant.ContactAfterEdited, true) })
                    finish()
                }
                EDIT_FAIL -> ToastUtil.showError(this, R.string.edit_contact_failed)
                REPEAT_MATRIX_ID -> showMatrixIdError()
                INVALID_MATRIX_ID -> showMatrixIdError()
                UNKNOWN_HOST_EXCEPTION -> ToastUtil.showError(this, R.string.network_error)
            }
        }

        emailTypes = listOf(
                getString(R.string.no_type),
                getString(R.string.home),
                getString(R.string.work),
                getString(R.string.other)
        )
        addressTypes = listOf(
                getString(R.string.no_type),
                getString(R.string.house),
                getString(R.string.work),
                getString(R.string.other)
        )
        phoneTypes = listOf(
                getString(R.string.no_type),
                getString(R.string.cell_phone),
                getString(R.string.work_phone),
                getString(R.string.house),
                getString(R.string.other)
        )
        dateTypes = listOf(
                getString(R.string.no_type),
                getString(R.string.birth_day),
                getString(R.string.anniversary),
                getString(R.string.other)
        )
        imppTypes = listOf(
                getString(R.string.no_type),
                getString(R.string.whatsapp),
                getString(R.string.teams),
                getString(R.string.messenger),
                getString(R.string.telegram),
                getString(R.string.facebook),
                getString(R.string.skype),
                getString(R.string.qq),
                getString(R.string.wechat)
        )
        websiteTypes = listOf(
                getString(R.string.no_type),
                getString(R.string.other)
        )

        if (mode == MODE_ADD) {
            addEmptyLayout()
        }
        if (mode == MODE_EDIT) {
            views.groupToolbarTitleView.text = getString(R.string.edit_contact)
        } else {
            views.groupToolbarTitleView.text = getString(R.string.new_contact)
        }
    }

    private fun showMatrixIdError() {
        views.layoutMatrixId.tvError.visibility = View.VISIBLE
        views.layoutMatrixId.tvError.text = getString(R.string.invalid_matrix_id)
    }

    private fun addEmptyLayout() {
        addEditLayout(getString(R.string.tele_phone), views.llPhoneNumberList, mPhoneList)
        addEditLayout(getString(R.string.e_mail), views.llEmailList, mEmailList)
        addEditLayout(getString(R.string.website), views.llWebsiteList, mWebsiteList)
        addEditLayout(
                getString(R.string.communication_tool),
                views.llCommunicationToolList,
                mImppList
        )
        addDateLayout()
        addAddressLayout()
    }

    private fun checkNeedShowMore() {
        for (child in views.linearLayout.children) {
            if (!child.isVisible) return
        }
        views.tvMoreProperty.visibility = View.GONE
    }

    private fun initData(it: ContactsDisplayBeanV2) {
        if (!it.matrixId.isNullOrBlank()) {
            views.layoutMatrixId.etEdit.setText(it.matrixId)
        }
        views.layoutMatrixId.setViewLineVisible(false)
        if (!it.family.isNullOrBlank()) {
            views.layoutFamilyName.etEdit.setText(it.family)
        }
        if (!it.given.isNullOrBlank()) {
            views.layoutGivenName.etEdit.setText(it.given)
        }
        if (!it.firstName.isNullOrBlank()) {
            views.layoutFamilyNamePinyin.visibility = View.VISIBLE
            views.layoutFamilyNamePinyin.etEdit.setText(it.firstName)
        }
        if (!it.middleName.isNullOrBlank()) {
            views.layoutMiddleNamePinyin.visibility = View.VISIBLE
            views.layoutMiddleNamePinyin.etEdit.setText(it.middleName)
        }
        if (!it.lastName.isNullOrBlank()) {
            views.layoutNamePinyin.visibility = View.VISIBLE
            views.layoutNamePinyin.etEdit.setText(it.lastName)
        }
        if (!it.nickName.isNullOrBlank()) {
            views.layoutNickName.visibility = View.VISIBLE
            views.layoutNickName.etEdit.setText(it.nickName)
        }
        if (!it.organization.isNullOrBlank()) {
            views.layoutCompany.etEdit.setText(it.organization)
        }
        views.layoutCompany.setViewLineVisible(false)
        if (!it.department.isNullOrBlank()) {
            views.layoutDepartment.visibility = View.VISIBLE
            views.layoutDepartment.etEdit.setText(it.department)
        }
        if (!it.title.isNullOrBlank()) {
            views.layoutUserTitle.visibility = View.VISIBLE
            views.layoutUserTitle.etEdit.setText(it.title)
        }
        if (!it.emailList.isNullOrEmpty()) {
            for (email in it.emailList!!) {
                addEditLayout(
                        getString(R.string.e_mail),
                        views.llEmailList,
                        mEmailList,
                        ContactUtils.convertEmailType(this, email.type, true),
                        email.value
                )
            }
        }
        if (!it.telephoneList.isNullOrEmpty()) {
            for (phone in it.telephoneList!!) {
                addEditLayout(
                        getString(R.string.tele_phone),
                        views.llPhoneNumberList,
                        mPhoneList,
                        ContactUtils.convertTelephoneType(this, phone.type, true),
                        phone.value
                )
            }
        }
        if (!it.addressList.isNullOrEmpty()) {
            views.llAddress.visibility = View.VISIBLE
            for (address in it.addressList!!) {
                addAddressLayout(ContactUtils.convertAddressType(this, address.type, true), address)
            }
        }
        if (!it.urlList.isNullOrEmpty()) {
            views.llWebsiteList.visibility = View.VISIBLE
            for (url in it.urlList!!) {
                addEditLayout(
                        getString(R.string.website),
                        views.llWebsiteList,
                        mWebsiteList,
                        url.type,
                        url.value
                )
            }
        }
        if (!it.imppList.isNullOrEmpty()) {
            views.llCommunicationToolList.visibility = View.VISIBLE
            for (impp in it.imppList!!) {
                addEditLayout(
                        getString(R.string.communication_tool),
                        views.llCommunicationToolList,
                        mImppList, ContactUtils.convertImppType(this, impp.type, true), impp.value
                )
            }
        }
        if (!it.dateList.isNullOrEmpty()) {
            views.llKeyDate.visibility = View.VISIBLE
            for (url in it.dateList!!) {
                addDateLayout(ContactUtils.convertDateType(this, url.type, true), url.value)
            }
        }
//        if (!it.relationship.isNullOrBlank()) {
//            views.layoutRelationship.visibility = View.VISIBLE
//            views.layoutRelationship.setText(it.relationship)
//        }
        if (!it.note.isNullOrBlank()) {
            views.layoutRemark.visibility = View.VISIBLE
            views.layoutRemark.setText(it.note)
        }
        views.layoutRemark.setViewLineVisible(false)
        if (!it.categories.isNullOrBlank()) {
            views.layoutLabel.visibility = View.VISIBLE
            views.layoutLabel.setText(it.categories)
        }
        views.layoutLabel.setViewLineVisible(false)
    }

    private fun initTimePicker(layout: ContactEditAddLayout): TimePickerView { //Dialog 模式下，在底部弹出
        val pvTime = TimePickerBuilder(this) { date, _ ->
            layout.setText(getTime(date))
        }
                .setTitleBgColor(Color.parseColor("#ffffff"))
                .setCancelColor(Color.parseColor("#ff999999"))
                .setSubmitColor(Color.parseColor("#ff5b6a91"))
                .setType(booleanArrayOf(true, true, true, false, false, false))
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
                .setLineSpacingMultiplier(3.0f)
                .setContentTextSize(16)
                .isAlphaGradient(true)
                .build()
        val mDialog: Dialog = pvTime.dialog
        val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ScreenUtils.getScreenHeight(this) / 2,
                Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvTime.dialogContainerLayout.layoutParams = params
        val dialogWindow = mDialog.window
        if (dialogWindow != null) {
            dialogWindow.setWindowAnimations(R.style.picker_view_slide_anim) //修改动画样式
            dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
            dialogWindow.setDimAmount(0.3f)
        }
        return pvTime
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(date: Date): String? { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyy/MM/dd")
        return format.format(date)
    }

    //获取内容，并添加联系人
    private fun addContact() {
        val mContact = ContactsDisplayBeanV2().apply {
            views.layoutMatrixId.getText()?.let { matrixId = it }
            if (views.layoutFamilyName.getText().isNullOrBlank()) {
                ToastUtil.showError(this@ContactEditAddActivity, R.string.no_family_name)
                return
            }
            if (views.layoutGivenName.getText().isNullOrBlank()) {
                ToastUtil.showError(this@ContactEditAddActivity, R.string.no_name)
                return
            }
            family = views.layoutFamilyName.getText()
            given = views.layoutGivenName.getText()
            views.layoutFamilyNamePinyin.getText()?.let { firstName = it }
            views.layoutMiddleNamePinyin.getText()?.let { middleName = it }
            views.layoutNamePinyin.getText()?.let { lastName = it }
            views.layoutNickName.getText()?.let { nickName = it }
            views.layoutCompany.getText()?.let { organization = it }
            views.layoutDepartment.getText()?.let { department = it }
            views.layoutUserTitle.getText()?.let { title = it }

            val addTelephoneList = ArrayList<TelephoneBean>()
            editTelephoneList = EditPhoneList()
            editTelephoneList?.delList = vm.contactLiveData.value?.telephoneList
            editTelephoneList?.delList?.let {
                for (item in it) {
                    item.contactId = id
                }
            }
            for (layout in mPhoneList) {
                if (layout.getText().isNullOrBlank())
                    continue
                val telephoneBean = TelephoneBean()
                telephoneBean.value = layout.getText()
                telephoneBean.type =
                        ContactUtils.convertTelephoneType(
                                this@ContactEditAddActivity,
                                layout.title.text.toString()
                        )
                addTelephoneList.add(telephoneBean)
            }
            telephoneList = addTelephoneList
            editTelephoneList?.addList = addTelephoneList

            val addEmailList = ArrayList<EmailBean>()
            editEmailList = EditEmailList()
            editEmailList?.delList = vm.contactLiveData.value?.emailList
            editEmailList?.delList?.let {
                for (item in it) {
                    item.contactId = id
                }
            }
            for (layout in mEmailList) {
                if (layout.getText().isNullOrBlank())
                    continue
                val emailBean = EmailBean()
                emailBean.value = layout.getText()
                emailBean.type = ContactUtils.convertEmailType(
                        this@ContactEditAddActivity,
                        layout.title.text.toString()
                )
                addEmailList.add(emailBean)
            }
            emailList = addEmailList
            editEmailList?.addList = addEmailList

            val addWebsiteList = ArrayList<UrlBean>()
            editUrlList = EditUrlList()
            editUrlList?.delList = vm.contactLiveData.value?.urlList
            editUrlList?.delList?.let {
                for (item in it) {
                    item.contactId = id
                }
            }
            for (layout in mWebsiteList) {
                if (layout.getText().isNullOrBlank())
                    continue
                val urlBean = UrlBean()
                urlBean.value = layout.getText()
                urlBean.type = layout.title.text.toString()
                addWebsiteList.add(urlBean)
            }
            urlList = addWebsiteList
            editUrlList?.addList = addWebsiteList

            val addImppList = ArrayList<ImppBean>()
            editImppList = EditImppList()
            editImppList?.delList = vm.contactLiveData.value?.imppList
            editImppList?.delList?.let {
                for (item in it) {
                    item.contactId = id
                }
            }
            for (layout in mImppList) {
                if (layout.getText().isNullOrBlank())
                    continue
                val imppBean = ImppBean()
                imppBean.value = layout.getText()
                imppBean.type = ContactUtils.convertImppType(
                        this@ContactEditAddActivity,
                        layout.title.text.toString()
                )
                addImppList.add(imppBean)
            }
            imppList = addImppList
            editImppList?.addList = addImppList

            val addDateList = ArrayList<DateBean>()
            editDateList = EditDateList()
            editDateList?.delList = vm.contactLiveData.value?.dateList
            editDateList?.delList?.let {
                for (item in it) {
                    item.contactId = id
                }
            }
            for (layout in mDateList) {
                if (layout.getText().isNullOrBlank())
                    continue
                val dateBean = DateBean()
                dateBean.value = layout.getText()
                dateBean.type = ContactUtils.convertDateType(
                        this@ContactEditAddActivity,
                        layout.title.text.toString()
                )
                addDateList.add(dateBean)
            }
            dateList = addDateList
            editDateList?.addList = addDateList

            editAddressList = EditAddressList()
            editAddressList?.delList = vm.contactLiveData.value?.addressList
            editAddressList?.delList?.let {
                for (item in it) {
                    item.contactId = id
                }
            }
            val addAddressList = ArrayList<AddressBean>()
            for (layout in mAddressList) {
                val address = layout.getAddress() ?: continue
                address.type =
                        ContactUtils.convertAddressType(this@ContactEditAddActivity, address.type)
                addAddressList.add(address)
            }
            addressList = addAddressList
            editAddressList?.addList = addAddressList

            views.layoutRemark.getText()?.let { note = it }
//            views.layoutRelationship.getText()?.let { relationship = it }
            views.layoutLabel.getText()?.let { categories = it }
        }
        // showLoading()
        if (mode == MODE_ADD) {
            vm.addContact(mContact)
        } else if (mode == MODE_EDIT) {
            mContact.id = id
            vm.updateContact(mContact, vm.contactLiveData.value?.matrixId != mContact.matrixId)
        }
    }

    private fun showMore() {
        views.layoutFamilyNamePinyin.visibility = View.VISIBLE
        views.layoutMiddleNamePinyin.visibility = View.VISIBLE
        views.layoutNamePinyin.visibility = View.VISIBLE
        views.layoutCompany.visibility = View.VISIBLE
        views.llPhoneNumberList.visibility = View.VISIBLE
        views.llEmailList.visibility = View.VISIBLE
        views.layoutDepartment.visibility = View.VISIBLE
        views.layoutUserTitle.visibility = View.VISIBLE
        views.llAddress.visibility = View.VISIBLE
        views.llWebsiteList.visibility = View.VISIBLE
        views.llCommunicationToolList.visibility = View.VISIBLE
        views.llKeyDate.visibility = View.VISIBLE
//        views.layoutRelationship.visibility = View.VISIBLE
        views.layoutRemark.visibility = View.VISIBLE
        views.layoutLabel.visibility = View.VISIBLE
        views.tvMoreProperty.visibility = View.GONE
    }

    private fun setEditLayoutTextWatch(
            type: String,
            layout: ContactEditAddLayout,
            layoutContainer: LinearLayout,
            layoutList: MutableList<ContactEditAddLayout>
    ) {
        //失去焦点后才能移除空的输入框，同时添加新的输入框
        layout.setFocusChangeListener { _, hasFocus ->
            if (hasFocus) return@setFocusChangeListener
            //删除除了最后一个之外,且为默认标签的空白框
            if (layout.getText()
                            .isNullOrBlank() && layoutList.indexOf(layout) != layoutList.size - 1 && layout.title.text == type
            ) {
//                layoutContainer.requestFocus() //避免自动聚焦到其他的输入框，导致界面错乱
                layoutList.remove(layout)
                layoutContainer.removeView(layout)
                //don't show the first divider
                val contactEditAddLayout = layoutContainer.getChildAt(0) as ContactEditAddLayout?
                contactEditAddLayout?.setViewLineVisible(false)
            }
            if (!layoutList.last().getText().isNullOrBlank())
                addEditLayout(type, layoutContainer, layoutList)
        }
        //进行输入后就要判断是否需要添加新的输入框
        ContactEditAddLayout.setListener(layout) {
            //最后一个框不为空时添加一个框
            if (!layoutList.last().getText().isNullOrBlank())
                addEditLayout(type, layoutContainer, layoutList)
        }
    }

    //增加电话、邮箱、网站、通讯工具的通用方法
    private fun addEditLayout(
            type: String,
            layoutContainer: LinearLayout,
            layoutList: MutableList<ContactEditAddLayout>,
            title: String? = null,
            value: String? = null
    ) {
        val newLayout = ContactEditAddLayout(this)
        newLayout.etEdit.hint = getString(R.string.please_enter)
        if (title != null) {
            newLayout.title.text = title
        } else {
            newLayout.title.text = type
        }
        if (value != null) {
            newLayout.etEdit.setText(value)
        }
        if (type == getString(R.string.tele_phone)) {
            newLayout.etEdit.inputType = EditorInfo.TYPE_CLASS_PHONE
        }
        if (type != getString(R.string.website))
            newLayout.ivArrow.visibility = View.VISIBLE
        layoutList.add(newLayout)
        layoutContainer.addView(newLayout)
        if (layoutContainer.indexOfChild(newLayout) == 0) newLayout.setViewLineVisible(false)
        setEditLayoutTextWatch(type, newLayout, layoutContainer, layoutList)
        newLayout.setOnTitleClickListener {
            if (type == getString(R.string.website)) return@setOnTitleClickListener
            var typeList = getTypes(type)
            if (typeList?.contains(newLayout.title.text) == false && !newLayout.title.text.isNullOrBlank() && newLayout.title.text != type) {
                typeList =
                        typeList.plus(newLayout.title.text as String)
            }
            TypeInputDialog(typeList, type) {
                newLayout.title.text = it
            }.show(supportFragmentManager, "typeInput")
        }
        newLayout.setReduceIcon {
//            layoutContainer.requestFocus() //避免自动聚焦到其他的输入框，导致界面错乱
            layoutList.remove(newLayout)
            layoutContainer.removeView(newLayout)
            val contactEditAddLayout = layoutContainer.getChildAt(0) as ContactEditAddLayout?
            contactEditAddLayout?.setViewLineVisible(false)
        }
    }

    private fun getTypes(type: String): List<String>? {
        when (type) {
            getString(R.string.tele_phone) -> return phoneTypes
            getString(R.string.e_mail) -> return emailTypes
            getString(R.string.address) -> return addressTypes
            getString(R.string.communication_tool) -> return imppTypes
            getString(R.string.website) -> return websiteTypes
        }
        return listOf()
    }

    private fun addDateLayout(title: String? = null, value: String? = null) {
        val newLayout = ContactEditAddLayout(this)
        newLayout.setFocusAble(false)
        newLayout.etEdit.hint = getString(R.string.please_select)
        if (title != null) {
            newLayout.title.text = title
        } else {
            newLayout.title.text = getString(R.string.key_date)
        }
        if (value != null) {
            newLayout.etEdit.setText(value)
        }
        newLayout.ivArrow.visibility = View.VISIBLE
        mDateList.add(newLayout)
        views.llKeyDate.addView(newLayout)
        if (views.llKeyDate.indexOfChild(newLayout) == 0) newLayout.setViewLineVisible(false)
        newLayout.etEdit.setOnClickListener {
            val pvTime = initTimePicker(newLayout)
            pvTime.show()
        }

        newLayout.setOnTitleClickListener {
            if (dateTypes?.contains(newLayout.title.text) == false
                    && !newLayout.title.text.isNullOrBlank()
                    && newLayout.title.text != getString(
                            R.string.key_date
                    )
            ) {
                dateTypes = dateTypes?.plus(newLayout.title.text as String)
            }
            TypeInputDialog(dateTypes, getString(R.string.key_date)) {
                newLayout.title.text = it
            }.show(supportFragmentManager, "typeInput")
        }
        newLayout.setReduceIcon {
            //views.llKeyDate.requestFocus() //避免自动聚焦到其他的输入框，导致界面错乱
            mDateList.remove(newLayout)
            views.llKeyDate.removeView(newLayout)
            val contactEditAddLayout = views.llKeyDate.getChildAt(0) as ContactEditAddLayout?
            contactEditAddLayout?.setViewLineVisible(false)
        }
        setDateTextWatch(newLayout)
    }

    private fun addAddressLayout(title: String? = null, value: AddressBean? = null) {
        val newLayout = AddressEditLayout(this)
        if (title != null) {
            newLayout.setTitle(title)
        } else {
            newLayout.setTitle(getString(R.string.address))
        }
        value?.let {
            newLayout.initAddress(value)
            newLayout.tvCountry.ivReduce.visibility = View.VISIBLE
        }
        //newLayout.ivArror.visibility = View.VISIBLE
        mAddressList.add(newLayout)
        views.llAddress.addView(newLayout)
        if (views.llAddress.indexOfChild(newLayout) == 0) newLayout.tvCountry.setViewLineVisible(false)
        newLayout.setTitleClickListener {
            if (addressTypes?.contains(newLayout.tvCountry.title.text) == false
                    && !newLayout.tvCountry.title.text.isNullOrBlank()
                    && newLayout.tvCountry.title.text != getString(R.string.address)
            ) {
                addressTypes = addressTypes?.plus(newLayout.tvCountry.title.text as String)
            }
            TypeInputDialog(addressTypes, getString(R.string.address)) {
                newLayout.setTitle(it)
            }.show(supportFragmentManager, "typeInput")
        }
        newLayout.setDeleteIcon {
            mAddressList.remove(newLayout)
            views.llAddress.removeView(newLayout)
            val addressEditAddLayout = views.llAddress.getChildAt(0) as AddressEditLayout?
            addressEditAddLayout?.tvCountry?.setViewLineVisible(false)
        }
        setAddressTextWatch(newLayout)
    }

    private fun setAddressTextWatch(layout: AddressEditLayout) {
        layout.setTextWatch()
        layout.addressTextWatcher = {
            if (mAddressList.last().getAddress() != null)
                addAddressLayout()
        }
        layout.addressFocusChangeListener = {
            if (it == null && mAddressList.indexOf(layout) != mAddressList.size - 1 && layout.tvCountry.title.text == getString(
                            R.string.address
                    )
            ) {
//                views.llAddress.requestFocus()
                mAddressList.remove(layout)
                views.llAddress.removeView(layout)
                val addressEditAddLayout = views.llAddress.getChildAt(0) as AddressEditLayout?
                addressEditAddLayout?.tvCountry?.setViewLineVisible(false)
            }
            if (mAddressList.last().getAddress() != null)
                addAddressLayout()
        }
    }

    private fun setDateTextWatch(layout: ContactEditAddLayout) {
        layout.setFocusChangeListener { _, hasFocus ->
            if (hasFocus) return@setFocusChangeListener
            //删除除了最后一个之外的空白框
            if (!mDateList.last().getText()
                            .isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ) {
                addDateLayout()
            }
        }
        ContactEditAddLayout.setListener(layout) {
            //最后一个框不为空时添加一个框
            if (!mDateList.last().getText()
                            .isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ) {
                addDateLayout()
            }
        }
    }

    private fun initClickListener() {
        views.backLayout.onClick {
            onBackPressed()
        }
        views.tvMoreProperty.onClick {
            showMore()
        }
    }

    companion object {
        const val MODE_ADD = 0
        const val MODE_EDIT = 1

        fun start(
                context: Context,
        ) {
            val intent = Intent(context, ContactEditAddActivity::class.java)
            context.startActivity(intent)
        }
    }
}
