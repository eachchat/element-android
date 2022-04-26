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

package im.vector.app.eachchat.user

import com.airbnb.epoxy.TypedEpoxyController
import im.vector.app.R
import im.vector.app.core.epoxy.customHeightDividerItem
import im.vector.app.core.epoxy.profiles.buildProfileAction
import im.vector.app.core.epoxy.profiles.buildProfileSection
import im.vector.app.core.epoxy.profiles.buildShowMoreInfoInfoItem
import im.vector.app.core.epoxy.profiles.buildUserProfileInfoItem
import im.vector.app.core.resources.StringProvider
import im.vector.app.core.ui.list.genericFooterItem
import im.vector.app.eachchat.contact.api.bean.Department
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.EmailBean
import im.vector.app.eachchat.contact.data.TelephoneBean
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.department.DepartmentStoreHelper
import im.vector.lib.core.utils.epoxy.charsequence.toEpoxyCharSequence
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import java.util.ArrayList
import javax.inject.Inject

class UserInfoProfileController @Inject constructor(
        private val stringProvider: StringProvider,
        private val session: Session
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
        buildUserInfo(data)
        if (data.showAsMember) {
            buildRoomMemberActions(data)
        } else {
            buildUserActions(data)
        }
    }

    private fun buildUserInfo(state: UserInfoViewState) {
        var hasDivider = false

        // 昵称
        if (!state.contact?.nickName.isNullOrBlank()) {
            if (!hasDivider) {
                hasDivider = true
                customHeightDividerItem {
                    id("divider_room_member_profile_info")
                    customHeight(8)
                }
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.nick_name), state.contact?.nickName)
        }

        // 电话
        var phone: TelephoneBean? = null
        if (state.departmentUser?.phoneNumbers != null && state.departmentUser.phoneNumbers!!.size > 0 && !state.departmentUser.phoneNumbers!![0].value.isNullOrBlank()) {
            phone = TelephoneBean()
            phone.type = stringProvider.getString(R.string.cell_phone)
            phone.value = state.departmentUser.phoneNumbers!![0].value
        }
        if (phone == null && state.contact != null && state.contact.telephoneList != null && state.contact.telephoneList!!.size > 0) {
            phone = state.contact.telephoneList!![0]
        }
        phone?.let {
            if (!hasDivider) {
                hasDivider = true
                customHeightDividerItem {
                    id("divider_room_member_profile_info")
                    customHeight(8)
                }
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.cell_phone), it.value)
        }

        // 邮件
        var email: EmailBean? = null
        if (state.departmentUser?.emails != null && state.departmentUser.emails!!.size > 0) {
            email = EmailBean()
            email.type = state.departmentUser.emails!![0].type
            email.value = state.departmentUser.emails!![0].value
        }
        if (email == null && state.contact != null && state.contact.emailList != null && state.contact.emailList!!.size > 0) {
            email = state.contact.emailList!![0]
        }
        email?.let {
            if (!hasDivider) {
                hasDivider = true
                customHeightDividerItem {
                    id("divider_room_member_profile_info")
                    customHeight(8)
                }
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.e_mail), it.value)
        }

        var department: String? = null
        if (state.departmentUser?.departmentId != null) {
            department = getDepartments(state.departmentUser)
        }
        if (department.isNullOrBlank()) {
            department = state.contact?.let { initTitle(it) }
        }
        if (!department.isNullOrBlank()) {
            if (!hasDivider) {
                hasDivider = true
                customHeightDividerItem {
                    id("divider_room_member_profile_info")
                    customHeight(8)
                }
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.department), department)
        }

        var userTitle: String? = null
        if (!state.departmentUser?.userTitle.isNullOrBlank()) {
            userTitle = state.departmentUser?.userTitle
        }
        if (userTitle.isNullOrBlank()) {
            userTitle = state.contact?.title
        }
        if (!userTitle.isNullOrBlank()) {
            if (!hasDivider) {
                hasDivider = true
                customHeightDividerItem {
                    id("divider_room_member_profile_info")
                    customHeight(8)
                }
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.user_title), userTitle)
        }
        if (state.contact != null || state.departmentUser != null) {
            buildShowMoreInfoInfoItem {

            }
        }
    }

    private fun getDepartments(userFound: User): String {
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

    private fun buildUserActions(state: UserInfoViewState) {
        // val ignoreActionTitle = state.buildIgnoreActionTitle() ?: return
        // More
//        buildProfileSection(stringProvider.getString(R.string.room_profile_section_more))
//        buildProfileAction(
//                id = "ignore",
//                title = ignoreActionTitle,
//                destructive = true,
//                editable = false,
//                divider = false,
//                action = { callback?.onIgnoreClicked() }
//        )
        customHeightDividerItem {
            id("divider_room_member_profile")
            customHeight(8)
        }
        if (!state.isMine) {
            if (state.userMatrixItem.invoke() != null) {
                buildProfileAction(
                        id = "direct",
                        editable = false,
                        title = stringProvider.getString(R.string.room_member_open_or_create_dm),
                        icon = R.mipmap.ic_send_message,
                        action = { callback?.onOpenDmClicked() }
                )
            }
            if (state.directRoomId != null) {
                buildProfileAction(
                        id = "voice_call",
                        editable = false,
                        title = stringProvider.getString(R.string.action_voice_call),
                        icon = R.drawable.ic_call_answer,
                        action = { callback?.onVoiceCall() }
                )
                buildProfileAction(
                        id = "video_call",
                        editable = false,
                        title = stringProvider.getString(R.string.action_video_call),
                        icon = R.drawable.ic_call_answer_video,
                        action = { callback?.onVideoCall() }
                )
            }
        }
    }

    private fun buildRoomMemberActions(state: UserInfoViewState) {
        customHeightDividerItem {
            id("divider_room_member_profile")
            customHeight(8)
        }
        if (!state.isSpace) {
//            buildSecuritySection(state)
        }
        if (!state.isMine) {
            if (state.userMatrixItem.invoke() != null) {
                buildProfileAction(
                        id = "direct",
                        editable = false,
                        title = stringProvider.getString(R.string.room_member_open_or_create_dm),
                        icon = R.mipmap.ic_send_message,
                        action = { callback?.onOpenDmClicked() }
                )
            }
            if (state.directRoomId != null) {
                buildProfileAction(
                        id = "voice_call",
                        editable = false,
                        title = stringProvider.getString(R.string.action_voice_call),
                        icon = R.drawable.ic_call_answer,
                        action = { callback?.onVoiceCall() }
                )
                buildProfileAction(
                        id = "video_call",
                        editable = false,
                        title = stringProvider.getString(R.string.action_video_call),
                        icon = R.drawable.ic_call_answer_video,
                        action = { callback?.onVideoCall() }
                )
            }
            // val ignoreActionTitle = state.buildIgnoreActionTitle()
//            buildProfileAction(
//                    id = "mention",
//                    title = stringProvider.getString(R.string.room_participants_action_mention),
//                    editable = false,
//                    divider = ignoreActionTitle != null,
//                    action = { callback?.onMentionClicked() }
//            )
        }
        //buildMoreSection(state)
    }

    private fun buildSecuritySection(state: UserInfoViewState) {
        // Security
        val host = this

        if (state.isRoomEncrypted) {
            if (!state.isAlgorithmSupported) {
                // TODO find sensible message to display here
                // For now we just remove the verify actions as well as the Security status
            } else if (state.userMXCrossSigningInfo != null) {
                buildProfileSection(stringProvider.getString(R.string.room_profile_section_security))
                // Cross signing is enabled for this user
                if (state.userMXCrossSigningInfo.isTrusted()) {
                    // User is trusted
                    val icon = if (state.allDevicesAreTrusted) {
                        R.drawable.ic_shield_trusted
                    } else {
                        R.drawable.ic_shield_warning
                    }

                    val titleRes = if (state.allDevicesAreTrusted) {
                        R.string.verification_profile_verified
                    } else {
                        R.string.verification_profile_warning
                    }

                    buildProfileAction(
                            id = "learn_more",
                            title = stringProvider.getString(titleRes),
                            editable = true,
                            icon = icon,
                            tintIcon = false,
                            divider = false,
                            action = { callback?.onShowDeviceList() }
                    )
                } else {
                    // Not trusted, propose to verify
                    if (!state.isMine) {
                        buildProfileAction(
                                id = "learn_more",
                                title = stringProvider.getString(R.string.verification_profile_verify),
                                editable = true,
                                icon = R.drawable.ic_shield_black,
                                divider = false,
                                action = { callback?.onTapVerify() }
                        )
                    } else {
                        buildProfileAction(
                                id = "learn_more",
                                title = stringProvider.getString(R.string.room_profile_section_security_learn_more),
                                editable = false,
                                divider = false,
                                action = { callback?.onShowDeviceListNoCrossSigning() }
                        )
                    }

                    genericFooterItem {
                        id("verify_footer")
                        text(host.stringProvider.getString(R.string.room_profile_encrypted_subtitle).toEpoxyCharSequence())
                        centered(false)
                    }
                }
            } else {
                buildProfileSection(stringProvider.getString(R.string.room_profile_section_security))

                buildProfileAction(
                        id = "learn_more",
                        title = stringProvider.getString(R.string.room_profile_section_security_learn_more),
                        editable = false,
                        divider = false,
                        subtitle = stringProvider.getString(R.string.room_profile_encrypted_subtitle),
                        action = { callback?.onShowDeviceListNoCrossSigning() }
                )
            }
        } else {
            buildProfileSection(stringProvider.getString(R.string.room_profile_section_security))

            genericFooterItem {
                id("verify_footer_not_encrypted")
                text(host.stringProvider.getString(R.string.room_profile_not_encrypted_subtitle).toEpoxyCharSequence())
                centered(false)
            }
        }
    }

    private fun buildMoreSection(state: UserInfoViewState) {
        // More
        buildProfileSection(stringProvider.getString(R.string.room_profile_section_more))

        buildProfileAction(
                id = "overrideColor",
                editable = false,
                title = stringProvider.getString(R.string.room_member_override_nick_color),
                subtitle = state.userColorOverride,
                divider = !state.isMine,
                action = { callback?.onOverrideColorClicked() }
        )

        if (!state.isMine) {
            //                                                                                                                                                                                                             val membership = state.asyncMembership() ?: return

            buildProfileAction(
                    id = "direct",
                    editable = false,
                    title = stringProvider.getString(R.string.room_member_open_or_create_dm),
                    action = { callback?.onOpenDmClicked() }
            )

            if (!state.isSpace && state.hasReadReceipt) {
                buildProfileAction(
                        id = "read_receipt",
                        editable = false,
                        title = stringProvider.getString(R.string.room_member_jump_to_read_receipt),
                        action = { callback?.onJumpToReadReceiptClicked() }
                )
            }

        }
    }

}
