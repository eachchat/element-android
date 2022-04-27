/*
 * Copyright 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package im.vector.app.eachchat.moreinfo

import com.airbnb.epoxy.TypedEpoxyController
import im.vector.app.R
import im.vector.app.core.epoxy.customHeightDividerItem
import im.vector.app.core.epoxy.profiles.buildMultiUserProfileInfoItem
import im.vector.app.core.epoxy.profiles.buildProfileSection
import im.vector.app.core.epoxy.profiles.buildUserProfileInfoItem
import im.vector.app.core.resources.StringProvider
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.api.bean.Department
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.EmailBean
import im.vector.app.eachchat.contact.data.TelephoneBean
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.department.ContactUtils
import im.vector.app.eachchat.department.DepartmentStoreHelper
import im.vector.app.eachchat.user.UserInfoViewState
import im.vector.app.eachchat.utils.string.StringUtils
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import javax.inject.Inject

class MoreInfoProfileController @Inject constructor(
        private val stringProvider: StringProvider
) : TypedEpoxyController<UserInfoViewState>() {

    var callback: Callback? = null

    interface Callback {
        fun onIgnoreClicked()
        fun onTapVerify()
        fun onShowDeviceList()
        fun onShowDeviceListNoCrossSigning()
        fun onOpenDmClicked()
        fun onOverrideColorClicked()
        fun onJumpToReadReceiptClicked()
        fun onMentionClicked()
        fun onEditPowerLevel(currentRole: Role)
        fun onKickClicked(isSpace: Boolean)
        fun onBanClicked(isSpace: Boolean, isUserBanned: Boolean)
        fun onCancelInviteClicked()
        fun onInviteClicked()
        fun onVoiceCall()
        fun onVideoCall()
    }

    override fun buildModels(data: UserInfoViewState?) {
        if (data == null) return
//        if (data?.userMatrixItem?.invoke() == null) {
//
//        }
        // buildUserInfo(data)

        data.contact?.let {
            initBaseInfo(it)
        }
        initPhone(data)
        initEmail(data)
        initAddress(data)
        initAbout(data)
    }

    private fun initTitle(contact: ContactsDisplayBeanV2): String? {
        var titleText = ""
        contact.organization?.let {
            titleText += "$it/"
        }
        contact.department?.let {
            titleText += it
        }
        if (titleText.isBlank()) return null
        titleText = titleText.replace("//", "/")//去掉连续的点
        //去掉开头的/
        while (titleText.startsWith("/")) {
            titleText = titleText.replaceFirst("/", "")
        }
        //去掉最后的/
        while (titleText.endsWith("/")) {
            titleText = titleText.replaceRange(titleText.length - 1, titleText.length, "")
        }
        return titleText
    }

    private fun initBaseInfo(contact: ContactsDisplayBeanV2) {
        initName(contact)
        initPinyin(contact)
        initNickName(contact)
    }

    private fun getUserName(contact: ContactsDisplayBeanV2): CharSequence? {
        return if (!contact.family.isNull() || !contact.given.isNull()
        ) {
            var family = contact.family
            var given = contact.given
            if (contact.family.isNull()) family = ""
            if (contact.given.isNull()) given = ""
            if (StringUtils.isAllEnglish(family) && StringUtils.isAllEnglish(given)) {
                return "$given $family"
            }
            family + given
        } else if (!contact.nickName.isNull()) {
            contact.nickName
        } else {
            ""
        }
    }

    private fun initPhone(data: UserInfoViewState) {
        var hasTitle = false
        val contact = data.contact
        val orgMember = data.departmentUser
        contact?.telephoneList?.let {
            for (phone in it) {
                if (phone.value.isNullOrEmpty())
                    continue
                if (!hasTitle) {
                    buildProfileSection(stringProvider.getString(R.string.tele_phone))
                    hasTitle = true
                }
                buildUserProfileInfoItem(ContactUtils.convertTelephoneType(BaseModule.getContext(), phone.type, true), phone.value)
            }
        }
        orgMember?.phoneNumbers?.forEach { phone ->
            if (contact?.telephoneList != null) {
                //remove duplicate item
                for (telephoneBean in contact.telephoneList!!) {
                    if (telephoneBean.value == phone.value) {
                        return@forEach
                    }
                }
                if (phone.value.isNullOrEmpty())
                    return@forEach
                if (!hasTitle) {
                    buildProfileSection(stringProvider.getString(R.string.tele_phone))
                    hasTitle = true
                }
                buildUserProfileInfoItem(ContactUtils.convertTelephoneType(BaseModule.getContext(), stringProvider.getString(R.string.work), true), phone.value)
            } else {
                if (phone.value.isNullOrEmpty())
                    return@forEach
                if (!hasTitle) {
                    buildProfileSection(stringProvider.getString(R.string.tele_phone))
                    hasTitle = true
                }
                buildUserProfileInfoItem(ContactUtils.convertTelephoneType(BaseModule.getContext(), phone.type, true), phone.value)
            }
        }
    }

    private fun initEmail(data: UserInfoViewState) {
        var hasTitle = false
        val contact = data.contact
        val orgMember = data.departmentUser
        contact?.emailList?.let {
            for (email in it) {
                if (email.value.isNullOrEmpty())
                    continue
                if (!hasTitle) {
                    buildProfileSection(stringProvider.getString(R.string.e_mail))
                    hasTitle = true
                }
                buildUserProfileInfoItem(ContactUtils.convertEmailType(BaseModule.getContext(), email.type, true), email.value)
            }
        }
        orgMember?.emails?.forEach { email ->
            if (contact?.emailList != null) {
                //remove duplicate item
                for (emailBean in contact.emailList!!) {
                    if (emailBean.value == email.value) {
                        return@forEach
                    }
                }
                if (email.value.isNullOrEmpty())
                    return@forEach
                if (!hasTitle) {
                    buildProfileSection(stringProvider.getString(R.string.e_mail))
                    hasTitle = true
                }
                buildUserProfileInfoItem(ContactUtils.convertTelephoneType(BaseModule.getContext(), stringProvider.getString(R.string.e_mail), true), email.value)
            } else {
                if (email.value.isNullOrEmpty())
                    return@forEach
                if (!hasTitle) {
                    buildProfileSection(stringProvider.getString(R.string.e_mail))
                    hasTitle = true
                }
                buildUserProfileInfoItem(ContactUtils.convertEmailType(BaseModule.getContext(), email.type, true), email.value)
            }
        }
    }

    private fun initAddress(data: UserInfoViewState) {
        var hasTitle = false
        val contact = data.contact
        if (contact?.addressList == null) {
            return
        }
        contact.addressList?.let {
            if (it.size == 0) {
                return
            }
            for (address in it) {
                var addressText = ""
                if (!address.country.isNullOrBlank())
                    addressText += address.country + "\n"
                if (!address.region.isNullOrBlank())
                    addressText += address.region + "\n"
                if (!address.locality.isNullOrBlank())
                    addressText += address.locality + "\n"
                if (!address.subLocality.isNullOrBlank())
                    addressText += address.subLocality + "\n"
                if (!address.streetAddress.isNullOrBlank())
                    addressText += address.streetAddress + "\n"
                if (!address.postalCode.isNullOrBlank())
                    addressText += address.postalCode + "\n"
                if (addressText.isNotEmpty()) {
                    if (addressText.endsWith("\n")) {
                        addressText =
                                addressText.removeRange(addressText.length - 1, addressText.length)
                    }
                    if (!hasTitle) {
                        buildProfileSection(stringProvider.getString(R.string.address))
                        hasTitle = true
                    }
                    buildUserProfileInfoItem(ContactUtils.convertAddressType(BaseModule.getContext(), address.type, true), addressText)
                }
            }
        }
    }

    private fun initAbout(data: UserInfoViewState) {
        val contact = data.contact
        customHeightDividerItem {
            id("divider_more_info_about")
            customHeight(8)
        }
        contact?.let {
            if (contact.given.isNull())
                contact.given = ""
            initUrl(contact)
        }
        initOrg(data)
        contact?.let {
            initTitle(contact)
            initImpp(contact)
            initDate(contact)
            initRelationship(contact)
            initNote(contact)
            initCategories(contact)
        }
    }

    private fun initName(contact: ContactsDisplayBeanV2) {
        val name = getUserName(contact)
        if (name.isNullOrBlank()) return
        buildUserProfileInfoItem(stringProvider.getString(R.string.name), name.toString())
    }

    private fun initNickName(contact: ContactsDisplayBeanV2) {
        if (contact.nickName.isNullOrBlank()) return
        buildUserProfileInfoItem(stringProvider.getString(R.string.nick_name), contact.nickName)
    }

    private fun initPinyin(contact: ContactsDisplayBeanV2) {
        var familyPinYin: String? = ""
        var givenPinYin: String? = ""
        var additionPinYin: String? = ""
        if (contact.firstName.isNull() && contact.lastName.isNull()) return
        if (!contact.firstName.isNull()) familyPinYin = contact.firstName
        if (!contact.lastName.isNull()) givenPinYin = contact.lastName
        if (!contact.middleName.isNull()) additionPinYin = contact.middleName
        buildUserProfileInfoItem(stringProvider.getString(R.string.given_and_family_pinyin), stringProvider.getString(R.string.name_pinyin_text, familyPinYin, additionPinYin, givenPinYin))
    }

    private fun initUrl(contact: ContactsDisplayBeanV2) {
        contact.urlList?.let {
            if (it.size == 0) return
            for (url in it) {
                if (url.value.isNullOrEmpty())
                    continue
                buildUserProfileInfoItem(stringProvider.getString(R.string.website), url.value)
            }
        }
    }

    private fun initOrg(data: UserInfoViewState) {
        val contact = data.contact
        val orgMember = data.departmentUser
        //first take org info
        if (orgMember != null) {
            val departments = getDepartments(orgMember)
            if (departments.isNotBlank()) {
                buildUserProfileInfoItem(stringProvider.getString(R.string.department), departments)
            }
            val userTitle = orgMember.userTitle
            if (!userTitle.isNullOrBlank()) {
                buildUserProfileInfoItem(stringProvider.getString(R.string.position_title), userTitle)
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.reporting_relationship), stringProvider.getString(R.string.view), true) {

            }
//            reportingLayout.setOnClickListener {
//                Contact.reportingRelationshipActivity(orgMember)
//            }
            // v.llOrg.addView(reportingLayout)
            var userRegion: String? = null
            if (orgMember.addresses?.size != null && orgMember.addresses?.size!! > 0) {
                userRegion = orgMember.addresses?.get(0)?.locality
            }
            if (!userRegion.isNullOrBlank()) {
                buildUserProfileInfoItem(stringProvider.getString(R.string.region_title), userRegion)
            }
            //if don't have org info take contact info
        } else if (contact != null) {
            var departmentText = ""
            contact.organization?.let {
                departmentText += "$it/"
            }
            contact.department?.let {
                departmentText += "$it/"
            }
            departmentText = departmentText.replace("//", "/")//去掉连续的杠
            //去掉开头的杠
            while (departmentText.startsWith("/")) {
                departmentText = departmentText.replaceFirst("/", "")
            }
            //去掉最后的杠
            while (departmentText.endsWith("/")) {
                departmentText = departmentText.replaceRange(
                        departmentText.length - 1,
                        departmentText.length,
                        ""
                )
            }
            if (departmentText.isNotEmpty()) {
                buildUserProfileInfoItem(stringProvider.getString(R.string.organization), departmentText)
            }
            if (!contact.title.isNullOrBlank()) {
                buildUserProfileInfoItem(stringProvider.getString(R.string.position_title), contact.title)
            }
        }
    }

    fun getDepartments(userFound: User): String {
        // Get departments of the user
        val departments = ArrayList<Department>()
        var departmentText = ""
        var departmentId = userFound.departmentId
        while (!departmentId.isNullOrEmpty()) {
            val department =
                    runCatching { DepartmentStoreHelper.getDepartmentById(departmentId!!) }.getOrNull()
            departmentId = department?.parentId
            // current department is the org level, break the cycle
            if (department == null || departmentId.isNullOrEmpty()) {
//                this@UserViewModel.company.postValue(company)
                break
            }
            departmentText =
                    if (departmentText.isNotEmpty()) "${department.displayName} / $departmentText"
                    else department.displayName.orEmpty()
            departments.add(department)
        }
        return departmentText
    }

    private fun initImpp(contact: ContactsDisplayBeanV2) {
        contact.imppList?.let {
            if (it.size == 0) return
            val impps = ArrayList<String>()
            for (impp in it) {
                if (impp.value.isNullOrEmpty())
                    continue
                impps.add(
                        ContactUtils.convertImppType(BaseModule.getContext(), impp.type, true)  + "\n" +
                        impp.value
                )
            }
            buildMultiUserProfileInfoItem(stringProvider.getString(R.string.communication_tool), impps)
        }
    }

    private fun initDate(contact: ContactsDisplayBeanV2) {
        contact.dateList?.let {
            if (it.size == 0) return
            val dates = ArrayList<String>()
            for (date in it) {
                dates.add(
                        ContactUtils.convertDateType(BaseModule.getContext(), date.type, true) + "\n" +
                        date.value
                )
            }
            buildMultiUserProfileInfoItem(stringProvider.getString(R.string.key_date), dates)
        }
    }

    private fun initRelationship(contact: ContactsDisplayBeanV2) {
        contact.relationship?.let {
            buildUserProfileInfoItem(stringProvider.getString(R.string.relationship), it)
        }
    }

    private fun initNote(contact: ContactsDisplayBeanV2) {
        contact.note?.let {
            if (it.isBlank()) return
            buildUserProfileInfoItem(stringProvider.getString(R.string.remark), it)
        }
    }

    private fun initCategories(contact: ContactsDisplayBeanV2) {
        contact.categories?.let {
            if (it.isBlank()) return
            buildUserProfileInfoItem(stringProvider.getString(R.string.label), it)
        }
    }

    private fun String?.isNull() = (this.isNullOrEmpty() || this == "null")
}
