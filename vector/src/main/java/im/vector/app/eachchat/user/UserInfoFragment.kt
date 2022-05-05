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

import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Incomplete
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import im.vector.app.R
import im.vector.app.core.animations.AppBarStateChangeListener
import im.vector.app.core.animations.MatrixItemAppBarStateChangeListener
import im.vector.app.core.dialogs.ConfirmationDialogBuilder
import im.vector.app.core.extensions.cleanup
import im.vector.app.core.extensions.configureWith
import im.vector.app.core.extensions.copyOnLongClick
import im.vector.app.core.platform.StateView
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.core.utils.PERMISSIONS_FOR_AUDIO_IP_CALL
import im.vector.app.core.utils.PERMISSIONS_FOR_VIDEO_IP_CALL
import im.vector.app.core.utils.checkPermissions
import im.vector.app.core.utils.onPermissionDeniedDialog
import im.vector.app.core.utils.registerForPermissionsResult
import im.vector.app.core.utils.startSharePlainTextIntent
import im.vector.app.databinding.DialogBaseEditTextBinding
import im.vector.app.databinding.DialogShareQrCodeBinding
import im.vector.app.databinding.FragmentMatrixProfileBinding
import im.vector.app.databinding.ViewStubRoomMemberProfileHeaderBinding
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.addcontact.ContactEditAddActivity
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.getDepartments
import im.vector.app.eachchat.contact.data.toContact
import im.vector.app.eachchat.moreinfo.MoreInfoActivity
import im.vector.app.eachchat.ui.dialog.AlertDialog
import im.vector.app.features.call.VectorCallActivity
import im.vector.app.features.call.webrtc.WebRtcCallManager
import im.vector.app.features.crypto.verification.VerificationBottomSheet
import im.vector.app.features.displayname.getBestName
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.features.home.room.detail.RoomDetailPendingActionStore
import im.vector.app.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import im.vector.app.features.roommemberprofile.RoomMemberProfileViewEvents
import im.vector.app.features.roommemberprofile.powerlevel.EditPowerLevelDialogs
import im.vector.app.features.settings.VectorPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.matrix.android.sdk.api.crypto.RoomEncryptionTrustLevel
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import org.matrix.android.sdk.api.util.MatrixItem
import javax.inject.Inject

@Parcelize
data class UserInfoArg(
        val userId: String? = null,
        val contact: ContactsDisplayBeanV2? = null,
        val departmentUserId: String? = null,
        val roomId: String? = null,
        val displayName: String? = null,
        val openByContact: Boolean? = false // 如果由联系人打开，删除联系人后需要退出
) : Parcelable {

}

class UserInfoFragment @Inject constructor(
        private val roomMemberProfileController: UserInfoProfileController,
        private val avatarRenderer: AvatarRenderer,
        private val roomDetailPendingActionStore: RoomDetailPendingActionStore,
        private val matrixItemColorProvider: MatrixItemColorProvider,
        private val callManager: WebRtcCallManager,
        private val vectorPreferences: VectorPreferences
) : VectorBaseFragment<FragmentMatrixProfileBinding>(),
        UserInfoProfileController.Callback {

    private lateinit var headerViews: ViewStubRoomMemberProfileHeaderBinding

    private val fragmentArgs: UserInfoArg by args()
    private val viewModel: UserInfoViewModel by fragmentViewModel()

    private var appBarStateChangeListener: AppBarStateChangeListener? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMatrixProfileBinding {
        return FragmentMatrixProfileBinding.inflate(inflater, container, false)
    }

    override fun getMenuRes() = R.menu.vector_room_member_profile

    private var actionAddContact: MenuItem? = null
    private var actionDeleteContact: MenuItem? = null
    private var actionEditContact: MenuItem? = null

    override fun onPrepareOptionsMenu(menu: Menu) {
        actionAddContact = menu.findItem(R.id.userInfoAddContactAction)
        actionDeleteContact = menu.findItem(R.id.userInfoDeleteContactAction)
        actionEditContact = menu.findItem(R.id.userEditContactAction)
        actionDeleteContact?.title?.let {
            val spannableString = SpannableString(it)
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.caution_fc4)), 0, spannableString.length, 0)
            actionDeleteContact?.title = spannableString
        }
        actionAddContact?.setOnMenuItemClickListener {
            withState(viewModel) {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (it.departmentUser != null) {
                        viewModel.addContacts(it.departmentUser.toContact(it.departmentUser.getDepartments(), false))
                    } else if (it.userMatrixItem.invoke() != null) {
                        viewModel.addContacts(ContactsDisplayBean(
                                it.userMatrixItem.invoke()!!.avatarUrl,
                                it.userMatrixItem.invoke()!!.displayName,
                                it.userMatrixItem.invoke()!!.id
                        ))
                    }
                }
            }
            true
        }
        actionDeleteContact?.setOnMenuItemClickListener {
            withState(viewModel) {
                if (it.contact != null) {
                    viewModel.deleteContact(it.contact) {
                        if (fragmentArgs.openByContact == true) {
                            requireActivity().finish()
                        }
                    }
                }
            }
            true
        }
        actionEditContact?.setOnMenuItemClickListener {
            withState(viewModel) {
                ContactEditAddActivity.startEdit(requireContext(), it.contact?.id)
            }
            true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.observeOtherInfo(this)
        // analyticsScreenName = Screen.ScreenName.User
        // viewModel.observeOtherInfo(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(views.matrixProfileToolbar)
                .allowBack()
        val headerView = views.matrixProfileHeaderView.let {
            it.layoutResource = R.layout.view_stub_room_member_profile_header
            it.inflate()
        }
        headerViews = ViewStubRoomMemberProfileHeaderBinding.bind(headerView)
        headerViews.memberProfileStateView.eventCallback = object : StateView.EventCallback {
            override fun onRetryClicked() {
                // viewModel.handle(RoomMemberProfileAction.RetryFetchingInfo)
            }
        }
        headerViews.memberProfileStateView.contentView = headerViews.memberProfileInfoContainer
        views.matrixProfileRecyclerView.configureWith(roomMemberProfileController, hasFixedSize = true, disableItemAnimation = true)
        roomMemberProfileController.callback = this
        appBarStateChangeListener = MatrixItemAppBarStateChangeListener(headerView,
                listOf(
                        views.matrixProfileToolbarAvatarImageView,
                        views.matrixProfileToolbarTitleView,
                        views.matrixProfileDecorationToolbarAvatarImageView
                )
        )
        views.matrixProfileAppBarLayout.addOnOffsetChangedListener(appBarStateChangeListener)
//        viewModel.observeViewEvents {
//            when (it) {
//                is RoomMemberProfileViewEvents.Loading -> showLoading(it.message)
//                is RoomMemberProfileViewEvents.Failure -> showFailure(it.throwable)
//                is RoomMemberProfileViewEvents.StartVerification           -> handleStartVerification(it)
//                is RoomMemberProfileViewEvents.ShareRoomMemberProfile      -> handleShareRoomMemberProfile(it.permalink)
//                is RoomMemberProfileViewEvents.ShowPowerLevelValidation    -> handleShowPowerLevelAdminWarning(it)
//                is RoomMemberProfileViewEvents.ShowPowerLevelDemoteWarning -> handleShowPowerLevelDemoteWarning(it)
//                is RoomMemberProfileViewEvents.OnKickActionSuccess         -> Unit
//                is RoomMemberProfileViewEvents.OnSetPowerLevelSuccess      -> Unit
//                is RoomMemberProfileViewEvents.OnBanActionSuccess          -> Unit
//                is RoomMemberProfileViewEvents.OnIgnoreActionSuccess       -> Unit
//                is RoomMemberProfileViewEvents.OnInviteActionSuccess       -> Unit
//            }.exhaustive
//        }
        setupLongClicks()

        headerViews.memberProfilePowerLevelView.visibility = View.GONE
    }

    private fun setupLongClicks() {
        headerViews.memberProfileNameView.copyOnLongClick()
        headerViews.memberProfileIdView.copyOnLongClick()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.roomMemberProfileShareAction -> {
                //viewModel.handle(RoomMemberProfileAction.ShareRoomMemberProfile)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleStartVerification(startVerification: RoomMemberProfileViewEvents.StartVerification) {
        if (startVerification.canCrossSign) {
            VerificationBottomSheet
                    .withArgs(roomId = null, otherUserId = startVerification.userId)
                    .show(parentFragmentManager, "VERIF")
        } else {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_title_warning)
                    .setMessage(R.string.verify_cannot_cross_sign)
                    .setPositiveButton(R.string.verification_profile_verify) { _, _ ->
                        VerificationBottomSheet
                                .withArgs(roomId = null, otherUserId = startVerification.userId)
                                .show(parentFragmentManager, "VERIF")
                    }
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        }
    }

    override fun onDestroyView() {
        views.matrixProfileAppBarLayout.removeOnOffsetChangedListener(appBarStateChangeListener)
        roomMemberProfileController.callback = null
        appBarStateChangeListener = null
        views.matrixProfileRecyclerView.cleanup()
        super.onDestroyView()
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (val asyncUserMatrixItem = state.userMatrixItem) {
            is Incomplete -> {
                views.matrixProfileToolbarTitleView.text = state.displayName
//                if (!state.userId.isNullOrBlank()) {
//                    avatarRenderer.render(MatrixItem.UserItem(state.userId, null, null), views.matrixProfileToolbarAvatarImageView)
//                }
//                headerViews.memberProfileStateView.state = StateView.State.Loading
                avatarRenderer.renderDefault(views.matrixProfileToolbarAvatarImageView)
                avatarRenderer.renderDefault(headerViews.memberProfileAvatarView)
                headerViews.memberProfileNameView.text = state.displayName
            }
            is Fail       -> {
//                state.userId?.let {
//
//                }
                avatarRenderer.renderDefault(views.matrixProfileToolbarAvatarImageView)
                views.matrixProfileToolbarTitleView.text = state.displayName
                // val failureMessage = errorFormatter.toHumanReadable(asyncUserMatrixItem.error)
                // headerViews.memberProfileStateView.state = StateView.State.Error(failureMessage)
            }
            is Success    -> {
                val userMatrixItem = asyncUserMatrixItem()
                headerViews.memberProfileStateView.state = StateView.State.Content
                headerViews.memberProfileIdView.text = userMatrixItem?.id
                val bestName = userMatrixItem?.getBestName()
                headerViews.memberProfileNameView.text = bestName
                userMatrixItem?.let {
                    headerViews.memberProfileNameView.setTextColor(matrixItemColorProvider.getColor(it))
                }
                views.matrixProfileToolbarTitleView.text = bestName
                if (userMatrixItem != null) {
                    avatarRenderer.render(userMatrixItem, headerViews.memberProfileAvatarView)
                }
                if (userMatrixItem != null) {
                    avatarRenderer.render(userMatrixItem, views.matrixProfileToolbarAvatarImageView)
                }

                if (state.isRoomEncrypted) {
                    headerViews.memberProfileDecorationImageView.isVisible = true
                    val trustLevel = if (state.userMXCrossSigningInfo != null) {
                        // Cross signing is enabled for this user
                        if (state.userMXCrossSigningInfo.isTrusted()) {
                            // User is trusted
                            if (state.allDevicesAreCrossSignedTrusted) {
                                RoomEncryptionTrustLevel.Trusted
                            } else {
                                RoomEncryptionTrustLevel.Warning
                            }
                        } else {
                            RoomEncryptionTrustLevel.Default
                        }
                    } else {
                        // Legacy
                        if (state.allDevicesAreTrusted) {
                            RoomEncryptionTrustLevel.Trusted
                        } else {
                            RoomEncryptionTrustLevel.Warning
                        }
                    }
                    headerViews.memberProfileDecorationImageView.render(trustLevel)
                    views.matrixProfileDecorationToolbarAvatarImageView.render(trustLevel)
                } else {
                    headerViews.memberProfileDecorationImageView.isVisible = false
                }

                headerViews.memberProfileAvatarView.setOnClickListener { view ->
                    if (userMatrixItem != null) {
                        onAvatarClicked(view, userMatrixItem)
                    }
                }
                views.matrixProfileToolbarAvatarImageView.setOnClickListener { view ->
                    if (userMatrixItem != null) {
                        onAvatarClicked(view, userMatrixItem)
                    }
                }
            }
        }
        //headerViews.memberProfilePowerLevelView.setTextOrHide(state.userPowerLevelString())
        if (state.contact != null) {
            headerViews.memberProfileNameView.text = state.contact.displayName
            views.matrixProfileToolbarTitleView.text = state.contact.displayName
        } else if (state.departmentUser != null) {
            headerViews.memberProfileNameView.text = state.departmentUser.displayName
            views.matrixProfileToolbarTitleView.text = state.departmentUser.displayName
        }

        actionAddContact?.isVisible = state.contact == null
        actionDeleteContact?.isVisible = state.contact != null
        actionEditContact?.isVisible = state.contact != null

        roomMemberProfileController.setData(state)
    }

    // RoomMemberProfileController.Callback

    override fun onIgnoreClicked() {
    }

    override fun onTapVerify() {
        // viewModel.handle(RoomMemberProfileAction.VerifyUser)
    }

    override fun onShowDeviceList() {
//        DeviceListBottomSheet.newInstance().show(parentFragmentManager, "DEV_LIST")
    }

    override fun onShowDeviceListNoCrossSigning() {
        // DeviceListBottomSheet.newInstance(it.userId).show(parentFragmentManager, "DEV_LIST")
    }

    override fun onOpenDmClicked() {
        withState(viewModel) {
            if (it.userMatrixItem.invoke()?.id.isNullOrBlank()) {
                AlertDialog(requireContext()).builder()
                        .setTitle(if (it.userMatrixItem.invoke()?.id.isNullOrBlank()) R.string.empty_martix_id else R.string.id_error)
                        .setPositiveButtonColor(R.color.send_btn_color)
                        .setPositiveButton(R.string.edit_contact) {
                            id.let { ContactEditAddActivity.start(requireContext()) }
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
            }
            lifecycleScope.launch(Dispatchers.IO) {
                it.userMatrixItem.invoke()?.let {
                    val existingRoomId = BaseModule.getSession().getExistingDirectRoomWithUser(it.id)
                    if (existingRoomId != null) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            navigator.openRoom(requireContext(), existingRoomId)
                        }
                    } else {
                        val roomParams = CreateRoomParams()
                                .apply {
                                    invitedUserIds.add(it.id)
                                    setDirectMessage()
                                    enableEncryptionIfInvitedUsersSupportIt = false
                                }

                        lifecycleScope.launch(Dispatchers.Main) {
                            navigator.openRoom(requireContext(), BaseModule.getSession().createRoom(roomParams))
                        }
                    }
                }
//        vectorBaseActivity.finish()
            }
        }
    }

    override fun onJumpToReadReceiptClicked() {
//        roomDetailPendingActionStore.data = fragmentArgs.userId?.let { RoomDetailPendingAction.JumpToReadReceipt(it) }
//        vectorBaseActivity.finish()
    }

    override fun onMentionClicked() {
//        roomDetailPendingActionStore.data = fragmentArgs.userId?.let { RoomDetailPendingAction.MentionUser(it) }
//        vectorBaseActivity.finish()
    }

    private fun handleShareRoomMemberProfile(permalink: String) {
        val view = layoutInflater.inflate(R.layout.dialog_share_qr_code, null)
        val views = DialogShareQrCodeBinding.bind(view)
        views.itemShareQrCodeImage.setData(permalink)
        MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setNeutralButton(R.string.ok, null)
                .setPositiveButton(R.string.share_by_text) { _, _ ->
                    startSharePlainTextIntent(
                            fragment = this,
                            activityResultLauncher = null,
                            chooserTitle = null,
                            text = permalink
                    )
                }.show()
    }

    private fun onAvatarClicked(view: View, userMatrixItem: MatrixItem) {
        navigator.openBigImageViewer(requireActivity(), view, userMatrixItem)
    }

    override fun onOverrideColorClicked() {
        val inflater = requireActivity().layoutInflater
        val layout = inflater.inflate(R.layout.dialog_base_edit_text, null)
        val views = DialogBaseEditTextBinding.bind(layout)
        // views.editText.setText(state.userColorOverride)
        views.editText.hint = "#000000"

        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.room_member_override_nick_color)
                .setView(layout)
                .setPositiveButton(R.string.ok) { _, _ ->
                    // val newColor = views.editText.text.toString()
//                    if (newColor != state.userColorOverride) {
//                        viewModel.handle(RoomMemberProfileAction.SetUserColorOverride(newColor))
//                    }
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    override fun onEditPowerLevel(currentRole: Role) {
        EditPowerLevelDialogs.showChoice(requireActivity(), R.string.power_level_edit_title, currentRole) {
            //viewModel.handle(RoomMemberProfileAction.SetPowerLevel(currentRole.value, newPowerLevel, true))
        }
    }

    override fun onKickClicked(isSpace: Boolean) {
        ConfirmationDialogBuilder
                .show(
                        activity = requireActivity(),
                        askForReason = true,
                        confirmationRes = if (isSpace) R.string.space_participants_remove_prompt_msg
                        else R.string.room_participants_remove_prompt_msg,
                        positiveRes = R.string.room_participants_action_remove,
                        reasonHintRes = R.string.room_participants_remove_reason,
                        titleRes = R.string.room_participants_remove_title
                ) {
                    //viewModel.handle(RoomMemberProfileAction.KickUser(reason))
                }
    }

    override fun onBanClicked(isSpace: Boolean, isUserBanned: Boolean) {
        val titleRes: Int
        val positiveButtonRes: Int
        val confirmationRes: Int
        if (isUserBanned) {
            confirmationRes = if (isSpace) R.string.space_participants_unban_prompt_msg
            else R.string.room_participants_unban_prompt_msg
            titleRes = R.string.room_participants_unban_title
            positiveButtonRes = R.string.room_participants_action_unban
        } else {
            confirmationRes = if (isSpace) R.string.space_participants_ban_prompt_msg
            else R.string.room_participants_ban_prompt_msg
            titleRes = R.string.room_participants_ban_title
            positiveButtonRes = R.string.room_participants_action_ban
        }
        ConfirmationDialogBuilder
                .show(
                        activity = requireActivity(),
                        askForReason = !isUserBanned,
                        confirmationRes = confirmationRes,
                        positiveRes = positiveButtonRes,
                        reasonHintRes = R.string.room_participants_ban_reason,
                        titleRes = titleRes
                ) {
                    //viewModel.handle(RoomMemberProfileAction.BanOrUnbanUser(reason))
                }
    }

    override fun onCancelInviteClicked() {
        ConfirmationDialogBuilder
                .show(
                        activity = requireActivity(),
                        askForReason = false,
                        confirmationRes = R.string.room_participants_action_cancel_invite_prompt_msg,
                        positiveRes = R.string.room_participants_action_cancel_invite,
                        reasonHintRes = 0,
                        titleRes = R.string.room_participants_action_cancel_invite_title
                ) {
                    //viewModel.handle(RoomMemberProfileAction.KickUser(null))
                }
    }

    override fun onInviteClicked() {
        //viewModel.handle(RoomMemberProfileAction.InviteUser)
    }

    override fun onVoiceCall() {
        isVideoCall = false
        safeStartCall(false)
    }

    override fun onVideoCall() {
        isVideoCall = true
        safeStartCall(true)
    }

    override fun onMoreInfoClick() {
        withState(viewModel) {
            MoreInfoActivity.start(requireActivity(), UserInfoArg(userId = it.userId, contact = it.contact, departmentUserId = it.departmentUserId))
        }
    }

    private fun onTapToReturnToCall() {
        callManager.getCurrentCall()?.let { call ->
            VectorCallActivity.newIntent(
                    context = requireContext(),
                    callId = call.callId,
                    signalingRoomId = call.signalingRoomId,
                    otherUserId = call.mxCall.opponentUserId,
                    isIncomingCall = !call.mxCall.isOutgoing,
                    isVideoCall = call.mxCall.isVideoCall,
                    mode = null
            ).let {
                startActivity(it)
            }
        }
    }

    private fun showDialogWithMessage(message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), null)
                .show()
    }

    private fun safeStartCall(isVideoCall: Boolean) {
        if (vectorPreferences.preventAccidentalCall()) {
            MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(if (isVideoCall) R.string.start_video_call_prompt_msg else R.string.start_voice_call_prompt_msg)
                    .setPositiveButton(if (isVideoCall) R.string.start_video_call else R.string.start_voice_call) { _, _ ->
                        safeStartCall2(isVideoCall)
                    }
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        } else {
            safeStartCall2(isVideoCall)
        }
    }

    private fun safeStartCall2(isVideoCall: Boolean) {
        // val startCallAction = RoomDetailAction.StartCall(isVideoCall)
        // timelineViewModel.pendingAction = startCallAction
        if (isVideoCall) {
            if (checkPermissions(PERMISSIONS_FOR_VIDEO_IP_CALL,
                            requireActivity(),
                            startCallActivityResultLauncher,
                            R.string.permissions_rationale_msg_camera_and_audio)) {
                // timelineViewModel.pendingAction = null
                startCall(isVideoCall)
            }
        } else {
            if (checkPermissions(PERMISSIONS_FOR_AUDIO_IP_CALL,
                            requireActivity(),
                            startCallActivityResultLauncher,
                            R.string.permissions_rationale_msg_record_audio)) {
                // timelineViewModel.pendingAction = null
                startCall(isVideoCall)
            }
        }
    }

    var isVideoCall = false
    private val startCallActivityResultLauncher = registerForPermissionsResult { allGranted, deniedPermanently ->
        if (allGranted) {
            startCall(isVideoCall)
        } else {
            if (deniedPermanently) {
                activity?.onPermissionDeniedDialog(R.string.denied_permission_generic)
            }
        }
    }

    private fun startCall(isVideoCall: Boolean) {
        withState(viewModel) {
            lifecycleScope.launch(Dispatchers.IO) {
                it.directRoomId?.let { it1 -> it.userId?.let { it2 -> callManager.startOutgoingCall(it1, it2, isVideoCall) } }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getExistingDM()
    }
}