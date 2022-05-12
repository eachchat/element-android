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

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.User
import org.matrix.android.sdk.api.session.crypto.crosssigning.MXCrossSigningInfo
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.PowerLevelsContent
import org.matrix.android.sdk.api.util.MatrixItem

data class UserInfoViewState(
        val userId: String? = null,
        val roomId: String? = null,
        val isSpace: Boolean = false,
        val showAsMember: Boolean = false,
        val isMine: Boolean = false,
        val isIgnored: Async<Boolean> = Uninitialized,
        val isRoomEncrypted: Boolean = false,
        val isAlgorithmSupported: Boolean = true,
        val powerLevelsContent: PowerLevelsContent? = null,
        val userPowerLevelString: Async<String> = Uninitialized,
        val userMatrixItem: Async<MatrixItem.UserItem?> = Uninitialized,
        val userMXCrossSigningInfo: MXCrossSigningInfo? = null,
        val allDevicesAreTrusted: Boolean = false,
        val allDevicesAreCrossSignedTrusted: Boolean = false,
        val asyncMembership: Async<Membership> = Uninitialized,
        val hasReadReceipt: Boolean = false,
        val userColorOverride: String? = null,
        val directRoomId: String? = null, // 判断是否显示语音通话和视频通话
        val contact: ContactsDisplayBeanV2? = null, // 用户的联系信息
        val departmentUserId: String? = null, // departmentUser不好parcelize，传一个Id进来间接获取
        val departmentUser: User? = null, // 用户的组织信息
        val displayName: String? = null // 用户的显示名称
) : MavericksState {
     constructor(args: UserInfoArg) : this(userId = args.userId, contact = args.contact, departmentUserId = args.departmentUserId, displayName = args.displayName)
}

data class ActionPermissions(
        val canKick: Boolean = false,
        val canBan: Boolean = false,
        val canInvite: Boolean = false,
        val canEditPowerLevel: Boolean = false
)
