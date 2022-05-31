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

package im.vector.app.eachchat.widget.bot.bot_info

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
import javax.inject.Inject

class BotInfoProfileController @Inject constructor(
        private val stringProvider: StringProvider
) : TypedEpoxyController<BotInfoViewState>() {

    var callback: Callback? = null

    interface Callback {
        fun onOpenDmClicked()
        fun onOverrideColorClicked()
        fun onJumpToReadReceiptClicked()
        fun onMentionClicked()
        fun onKickClicked(isSpace: Boolean)
        fun onBanClicked(isSpace: Boolean, isUserBanned: Boolean)
        fun onCancelInviteClicked()
        fun onInviteClicked()
    }

    override fun buildModels(data: BotInfoViewState?) {
        if (data == null) return
//        if (data?.userMatrixItem?.invoke() == null) {
//
//        }
        buildBotInfo(data)
        buildUserActions(data)

    }

    private fun buildBotInfo(state: BotInfoViewState) {
        customHeightDividerItem {
            id("divider_bot_description")
            customHeight(8)
        }
        state.botDescription?.let {
            buildUserProfileInfoItem(stringProvider.getString(R.string.description), it)
        }
        state.botDeveloper?.let {
            buildUserProfileInfoItem(stringProvider.getString(R.string.developer), it)
        }
        state.botHelp?.let {
            customHeightDividerItem {
                id("divider_bot_help")
                customHeight(8)
            }
            buildUserProfileInfoItem(stringProvider.getString(R.string.use_help), it)
        }
    }

    private fun buildUserActions(state: BotInfoViewState) {
        customHeightDividerItem {
            id("divider_room_member_profile")
            customHeight(8)
        }
        if (!state.isMine) {
            buildProfileAction(
                    id = "direct",
                    editable = false,
                    title = stringProvider.getString(R.string.room_member_open_or_create_dm),
                    icon = R.mipmap.ic_send_message,
                    action = { callback?.onOpenDmClicked() }
            )
        }
    }
}
