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

package im.vector.app.features.roomprofile.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import im.vector.app.R
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.extensions.setAttributeTintedImageResource
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentRoomContactBinding
import im.vector.app.eachchat.complain.RoomComplainViewState
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.features.home.AvatarRenderer
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class RoomContactFragment @Inject constructor(
        private val avatarRenderer: AvatarRenderer
) : VectorBaseFragment<FragmentRoomContactBinding>() {

    private val viewModel: RoomContactViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomContactBinding {
        return FragmentRoomContactBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(views.roomSettingsToolbar)
                .allowBack()
        views.waitingView.waitingStatusText.text = getString(R.string.please_wait)
        views.waitingView.waitingStatusText.visibility = View.VISIBLE
        initClickListener()
        initLoadingObserver()
    }

    //和complain的state差不多，就直接拿来用了
    override fun invalidate() = withState(viewModel) { viewState ->
        renderRoomSummary(viewState)
        viewState.roomSummary()?.let { initContactObserver(it) }
        views.waitingView.root.isVisible = viewState.isLoading
    }

    private fun renderRoomSummary(state: RoomComplainViewState) {
        state.roomSummary()?.let {
            views.roomSettingsToolbarTitleView.text = it.displayName
            avatarRenderer.render(it.toMatrixItem(), views.roomSettingsToolbarAvatarImageView)
            views.roomSettingsDecorationToolbarAvatarImageView.render(it.roomEncryptionTrustLevel)
        }
    }

    private fun initClickListener() {
        views.clSaveAction.onClick {
            viewModel.addContact()
        }
        views.clUnsavedAction.onClick {
            viewModel.deleteContact()
        }
    }

    private fun initContactObserver(roomSummary: RoomSummary) {
        if (roomSummary.otherMemberIds.isEmpty()) return
        AppDatabase.getInstance(requireContext()).contactDaoV2()
                .getContactByMatrixIdLive(roomSummary.otherMemberIds[0])
                .observe(viewLifecycleOwner) {
                    if (it != null) {
                        views.saveRadioIcon.setAttributeTintedImageResource(R.drawable.ic_radio_on, R.attr.colorPrimary)
                        views.saveRadioIcon.contentDescription = getString(R.string.a11y_checked)
                        views.unsavedRadioIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_radio_off))
                        views.unsavedRadioIcon.contentDescription = getString(R.string.a11y_unchecked)
                    } else {
                        views.unsavedRadioIcon.setAttributeTintedImageResource(R.drawable.ic_radio_on, R.attr.colorPrimary)
                        views.unsavedRadioIcon.contentDescription = getString(R.string.a11y_checked)
                        views.saveRadioIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_radio_off))
                        views.saveRadioIcon.contentDescription = getString(R.string.a11y_unchecked)
                    }
                }
    }

    private fun initLoadingObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            views.waitingView.waitingView.isVisible = it
        }
    }
}
