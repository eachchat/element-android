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

package im.vector.app.yiqia.complain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import im.vector.app.R
import im.vector.app.core.extensions.configureWith
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentRoomComplainBinding
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.yiqia.utils.ToastUtil
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class RoomComplainFragment @Inject constructor(
        private val roomComplainController: RoomComplainController,
        private val avatarRenderer: AvatarRenderer
) : VectorBaseFragment<FragmentRoomComplainBinding>() {

    private val viewModel: RoomComplainViewModel by fragmentViewModel()

    override fun invalidate() = withState(viewModel) { viewState ->
        roomComplainController.setData(viewState)
        renderRoomSummary(viewState)
        views.waitingView.root.isVisible = viewState.isLoading
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomComplainBinding {
        return FragmentRoomComplainBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(views.roomSettingsToolbar)
                .allowBack()
        views.waitingView.waitingStatusText.text = getString(R.string.please_wait)
        views.waitingView.waitingStatusText.visibility = View.VISIBLE
        setComplainCallback()
        views.roomSettingsRecyclerView.configureWith(roomComplainController, hasFixedSize = true)
        setConfirmComplainButton()
        roomComplainController.selectComplainCallback = {
            views.roomComplainConfirmTextView.isEnabled = roomComplainController.selectedComplainList.isNotEmpty()
        }
    }

    private fun renderRoomSummary(state: RoomComplainViewState) {
        state.roomSummary()?.let {
            views.roomSettingsToolbarTitleView.text = it.displayName
            avatarRenderer.render(it.toMatrixItem(), views.roomSettingsToolbarAvatarImageView)
            views.roomSettingsDecorationToolbarAvatarImageView.render(it.roomEncryptionTrustLevel)
        }
    }

    private fun setConfirmComplainButton() {
        views.roomComplainConfirmTextView.setOnClickListener {
            if (roomComplainController.selectedComplainList.isEmpty()) {
                return@setOnClickListener
            }
            viewModel.complain(roomComplainController.selectedComplainList)
        }
    }

    private fun setComplainCallback() {
        viewModel.complainSuccessCallback = {
            if (it) {
                ToastUtil.showSuccess(context, getString(R.string.complain_success))
                activity?.onBackPressed()
            } else {
                ToastUtil.showError(context, getString(R.string.network_error))
            }
        }
    }
}
