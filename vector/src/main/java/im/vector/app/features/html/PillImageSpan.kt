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

@file:Suppress("DEPRECATION")

package im.vector.app.features.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.style.ReplacementSpan
import android.widget.TextView
import androidx.annotation.UiThread
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.ChipDrawable
import im.vector.app.R
import im.vector.app.core.glide.GlideRequests
import im.vector.app.features.displayname.getBestName
import im.vector.app.features.displayname.getBestNameEachChat
import im.vector.app.features.home.AvatarRenderer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.room.send.MatrixItemSpan
import org.matrix.android.sdk.api.util.MatrixItem
import org.threeten.bp.format.TextStyle
import java.lang.ref.WeakReference

/**
 * This span is able to replace a text by a [ChipDrawable]
 * It's needed to call [bind] method to start requesting avatar, otherwise only the placeholder icon will be displayed if not already cached.
 * Implements MatrixItemSpan so that it could be automatically transformed in matrix links and displayed as pills.
 */
@OptIn(DelicateCoroutinesApi::class) class PillImageSpan(private val glideRequests: GlideRequests,
                                                         private val avatarRenderer: AvatarRenderer,
                                                         private val context: Context,
                                                         override val matrixItem: MatrixItem,
                                                         eachChatBestName: String? = null,
                                                         isEvent: Boolean = false // 将timeLineEvent和editText分开处理，优化交互效果

) : ReplacementSpan(), MatrixItemSpan {

    private var pillDrawable:ChipDrawable = createChipDrawable(eachChatBestName)
    private val target = PillImageSpanTarget(this)
    private var tv: WeakReference<TextView>? = null

    init {
        if (isEvent) {
            GlobalScope.launch(Dispatchers.IO) {
                val name = matrixItem.id.getBestNameEachChat(matrixItem.getBestName())
                pillDrawable = createChipDrawable(name)
            }
        }
    }

    @UiThread
    fun bind(textView: TextView) {
        tv = WeakReference(textView)
        avatarRenderer.render(glideRequests, matrixItem, target)
    }

    // ReplacementSpan *****************************************************************************

    override fun getSize(paint: Paint, text: CharSequence,
                         start: Int,
                         end: Int,
                         fm: Paint.FontMetricsInt?): Int {
        val rect = pillDrawable.bounds
        if (fm != null) {
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.bottom - fmPaint.top
            val drHeight = rect.bottom - rect.top
            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4
            fm.ascent = -bottom
            fm.top = -bottom
            fm.bottom = top
            fm.descent = top
        }
        return rect.right
    }

    override fun draw(canvas: Canvas, text: CharSequence,
                      start: Int,
                      end: Int,
                      x: Float,
                      top: Int,
                      y: Int,
                      bottom: Int,
                      paint: Paint) {
        canvas.save()
        val fm = paint.fontMetricsInt
        val transY: Int = y + (fm.descent + fm.ascent - pillDrawable.bounds.bottom) / 2
        canvas.save()
        canvas.translate(x, transY.toFloat())
        pillDrawable.draw(canvas)
        canvas.restore()
    }

    @OptIn(DelicateCoroutinesApi::class)
    internal fun updateAvatarDrawable(drawable: Drawable?) {
        pillDrawable.chipIcon = drawable
        tv?.get()?.invalidate()
    }

    // Private methods *****************************************************************************

    @OptIn(DelicateCoroutinesApi::class)
    private fun createChipDrawable(name: String?): ChipDrawable {
        val textPadding = context.resources.getDimension(R.dimen.pill_text_padding)
        val icon = try {
            avatarRenderer.getCachedDrawable(glideRequests, matrixItem)
        } catch (exception: Exception) {
            avatarRenderer.getPlaceholderDrawable(matrixItem)
        }

        return ChipDrawable.createFromResource(context, R.xml.pill_view).apply {
            text = name?: matrixItem.getBestName()
            textEndPadding = textPadding
            textStartPadding = textPadding
            setChipMinHeightResource(R.dimen.pill_min_height)
            setChipIconSizeResource(R.dimen.pill_avatar_size)
            chipIcon = icon
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
    }
}

/**
 * Glide target to handle avatar retrieval into [PillImageSpan].
 */
private class PillImageSpanTarget(pillImageSpan: PillImageSpan) : SimpleTarget<Drawable>() {

    private val pillImageSpan = WeakReference(pillImageSpan)

    override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable>?) {
        updateWith(drawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        updateWith(placeholder)
    }

    private fun updateWith(drawable: Drawable?) {
        pillImageSpan.get()?.apply {
            updateAvatarDrawable(drawable)
        }
    }
}
