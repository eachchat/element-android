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

package im.vector.app.eachchat.badge

import android.app.Service
import android.content.Intent
import android.os.IBinder
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.utils.YQBadgeUtils
import org.matrix.android.sdk.api.query.RoomCategoryFilter
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams

class BadgeService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val queryParams = roomSummaryQueryParams {
            memberships = listOf(Membership.JOIN)
        }
        BaseModule.getSession().getRoomSummariesLive(queryParams).observeForever { roomSummaries ->
            val unreadRoomSummaries = roomSummaries.filter { it.hasUnreadMessages }
            YQBadgeUtils.setCount(unreadRoomSummaries.size, BaseModule.getContext())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
