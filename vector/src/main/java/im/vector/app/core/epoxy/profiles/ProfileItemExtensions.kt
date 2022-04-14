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

package im.vector.app.core.epoxy.profiles

import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyController
import im.vector.app.core.epoxy.ClickListener
import im.vector.app.core.epoxy.customHeightDividerItem
import im.vector.app.core.epoxy.dividerItem
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.features.form.formSwitchItem
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.features.roomprofile.RoomProfileController.Companion.END_LOADING
import im.vector.app.features.roomprofile.RoomProfileController.Companion.START_LOADING
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.util.MatrixItem

fun EpoxyController.buildProfileSection(title: String) {
    profileSectionItem {
        id("section_$title")
        title(title)
    }
}

fun EpoxyController.buildProfileAction(
        id: String,
        title: String,
        subtitle: String? = null,
        editable: Boolean = true,
        @DrawableRes icon: Int = 0,
        tintIcon: Boolean = true,
        @DrawableRes editableRes: Int? = null,
        destructive: Boolean = false,
        divider: Boolean = true,
        action: ClickListener? = null,
        @DrawableRes accessory: Int = 0,
        accessoryMatrixItem: MatrixItem? = null,
        avatarRenderer: AvatarRenderer? = null
) {
    profileActionItem {
        iconRes(icon)
        tintIcon(tintIcon)
        id("action_$id")
        subtitle(subtitle)
        editable(editable)
        editableRes?.let { editableRes(editableRes) }
        destructive(destructive)
        title(title)
        accessoryRes(accessory)
        accessoryMatrixItem(accessoryMatrixItem)
        avatarRenderer(avatarRenderer)
        listener(action)
    }

    if (divider) {
        dividerItem {
            id("divider_$title")
        }
    }
}

fun EpoxyController.buildDivider(dividerName: String, height: Int) {
    customHeightDividerItem {
        id("divider_$dividerName")
        customHeight(height)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun EpoxyController.addContactSwitchItem(title: String, matrixId: String, contact: ContactsDisplayBeanV2, callBack: (String) -> Unit) {
    contactSwitchItem {
        id("add_contact_switch_item")
        enabled(true)
        title(title)
        switchChecked(false)
        matrixId(matrixId)
        listener {
            callBack.invoke(START_LOADING)
            GlobalScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    if (it) {
                        val response = ContactServiceV2.getInstance().add(contact)
                        if (response.obj != null) {
                            contact.id = response.obj?.id
                            AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().insertContact(contact)
                            callBack.invoke(END_LOADING)
                        } else {
                            callBack.invoke(END_LOADING)
                        }
                    } else {
                        val mContact = AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().getContactByMatrixId(matrixId)
                        val response = mContact?.id?.let { it1 -> ContactServiceV2.getInstance().delete(it1) }
                        if (response?.isSuccess == true) {
                            mContact.del = 1
                            AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2().update(mContact)
                            callBack.invoke(END_LOADING)
                        } else {
                            callBack.invoke(END_LOADING)
                        }
                    }
                }.exceptionOrNull()?.let {
                    callBack.invoke(END_LOADING)
                }
            }
        }
    }
    dividerItem {
        id("divider_add_contact")
    }
}
