/*
 * Copyright (c) 2021 New Vector Ltd
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

package im.vector.app.features.displayname

import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.database.AppDatabase
import org.matrix.android.sdk.api.util.MatrixItem

val database = AppDatabase.getInstance(BaseModule.getContext())

fun MatrixItem.getBestName(): String {
    // Note: this code is copied from [DisplayNameResolver] in the SDK
    return if (this is MatrixItem.GroupItem || this is MatrixItem.RoomAliasItem) {
        // Best name is the id, and we keep the displayName of the room for the case we need the first letter
        id
    } else {
        displayName
                ?.takeIf { it.isNotBlank() }
                ?: VectorMatrixItemDisplayNameFallbackProvider.getDefaultName(this)
    }
}

// 通过matrixId获取最佳显示名
fun String.getBestNameEachChat(displayName: String, unit: (String) -> Unit){
    val contactDisplayName = database.contactDaoV2().getContactByMatrixId(this)?.displayName
    val orgDisplayName = database.userDao().getBriefUserByMatrixId(this)?.displayName
    val bestName = contactDisplayName?.takeIf { it.isNotBlank() }?:
    orgDisplayName?.takeIf { it.isNotBlank() }?:
    displayName
    unit.invoke(bestName)
}

fun String.getBestNameEachChat(displayName: String) : String{
    val contactDisplayName = database.contactDaoV2().getContactByMatrixId(this)?.displayName
    val orgDisplayName = database.userDao().getBriefUserByMatrixId(this)?.displayName
    val bestName = contactDisplayName?.takeIf { it.isNotBlank() }?:
    orgDisplayName?.takeIf { it.isNotBlank() }?:
    displayName
    return bestName
}
