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

package im.vector.app.eachchat.contact.addcontact.dialog

import android.app.Activity
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import im.vector.app.R
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.databinding.DialogEditTypeBinding

object TypeSelectDialog {
    var selectedType: String? = null

    fun showChoice(activity: Activity,
                   defaultType: String,
                   currentType: String,
                   types: List<String>?,
                   listener: (String) -> Unit) {
        val dialogLayout = activity.layoutInflater.inflate(R.layout.dialog_edit_type, null)
        val views = DialogEditTypeBinding.bind(dialogLayout)
        val radioButtons = ArrayList<RadioButton>()

        // views.powerLevelCustomEdit.setText(currentRole.value.toString())
        views.typeCustomEditLayout.visibility = View.GONE

        types?.forEach {
            val radioButton = RadioButton(activity)
            radioButton.text = it
            views.typeRadioGroup.addView(radioButton)
            val lp = RadioGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            radioButton.layoutParams = lp
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    selectedType = radioButton.text.toString()
            }
            radioButtons.add(radioButton)
        }

        val customRadioButton = RadioButton(activity)
        val lp = RadioGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        customRadioButton.layoutParams = lp
        customRadioButton.text = activity.getString(R.string.custom)
        customRadioButton.setOnCheckedChangeListener { _, isChecked ->
            views.typeCustomEditLayout.isVisible = isChecked
        }
        views.typeRadioGroup.addView(customRadioButton)

        radioButtons.forEach {
            if (it.text == currentType) {
                it.isChecked = true
            }
        }

        MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.change_type)
                .setView(dialogLayout)
                .setPositiveButton(R.string.confirm) { _, _ ->
//                    val newValue = when (views.powerLevelRadioGroup.checkedRadioButtonId) {
//                        R.id.powerLevelAdminRadio     -> Role.Admin.value
//                        R.id.powerLevelModeratorRadio -> Role.Moderator.value
//                        R.id.powerLevelDefaultRadio   -> Role.Default.value
//                        else                          -> {
//                            views.powerLevelCustomEdit.text?.toString()?.toInt() ?: currentRole.value
//                        }
//                    }
                    if (!selectedType.isNullOrBlank()) {
                        if (!customRadioButton.isChecked) {
                            listener.invoke(selectedType!!)
                        } else if (views.typeCustomEdit.text.toString() == activity.getString(R.string.no_type)){
                            listener.invoke(defaultType)
                        } else {
                            listener.invoke(views.typeCustomEdit.text.toString())
                        }
                    }
                }
                .setNegativeButton(R.string.action_cancel, null)
                .setOnKeyListener(DialogInterface.OnKeyListener
                { dialog, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel()
                        return@OnKeyListener true
                    }
                    false
                })
                .setOnDismissListener {
                    dialogLayout.hideKeyboard()
                }
                .create()
                .show()
    }

    fun showValidation(activity: Activity, onValidate: () -> Unit) {
        // Ask to the user the confirmation to upgrade.
        MaterialAlertDialogBuilder(activity)
                .setMessage(R.string.room_participants_power_level_prompt)
                .setPositiveButton(R.string.yes) { _, _ ->
                    onValidate()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }

    fun showDemoteWarning(activity: Activity, onValidate: () -> Unit) {
        // Ask to the user the confirmation to downgrade his own role.
        MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.room_participants_power_level_demote_warning_title)
                .setMessage(R.string.room_participants_power_level_demote_warning_prompt)
                .setPositiveButton(R.string.room_participants_power_level_demote) { _, _ ->
                    onValidate()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
