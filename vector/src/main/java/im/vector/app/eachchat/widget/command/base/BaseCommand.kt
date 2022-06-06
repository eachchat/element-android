/*
 * Copyright 2019 New Vector Ltd
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
 */

package im.vector.app.eachchat.widget.command.base

import androidx.annotation.StringRes
import im.vector.app.R

/**
 * Defines the command line operations
 * the user can write theses messages to perform some actions
 * the list will be displayed in this order
 */
enum class BaseCommand(val command: String,
                       val aliases: Array<CharSequence>?,
                       val parameters: String,
                       @StringRes val description: Int,
                       val isDevCommand: Boolean,
                       val isThreadCommand: Boolean) {
    // BOT("!bot", null, "", R.string.command_description_emote, false, true),
    EMAIL("!mail", null, "", R.string.command_description_emote, false, true);

    val allAliases = arrayOf(command, *aliases.orEmpty())

    var keyword: String? = null // controller不能直接传值，通过Command数据结构，将keyword带进去

    fun matches(inputCommand: CharSequence) = allAliases.any { it.contentEquals(inputCommand, true) }

    fun startsWith(input: CharSequence) =
            allAliases.any { it.startsWith(input, 1, true) }
}
