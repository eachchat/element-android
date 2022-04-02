package im.vector.app.yiqia.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import im.vector.app.R
import im.vector.app.yiqia.ui.stickyHeader.StickyHeaderAdapter

/**
 * Created by chengww on 1/21/21
 * @author chengww
 */
class LineDecoration(
        private val mAdapter: StickyHeaderAdapter<out RecyclerView.ViewHolder>? = null,
        private val marginStart: Int? = null) : ItemDecoration() {

    private var dividerPaint = Paint()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        var headerHeight = 0
        if (position != RecyclerView.NO_POSITION && hasHeader(position) && shouldShowHeader(position)) {
            headerHeight = 1
        }
        outRect[0, headerHeight, 0] = 0
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        dividerPaint.color = ContextCompat.getColor(parent.context, R.color.dividerBg)
        val childCount = parent.childCount
        val left = marginStart ?: parent.paddingStart
        val right = parent.width

        for (i in 0 until childCount - 1) {
            val view = parent.getChildAt(i)
            val adapterPos = parent.getChildAdapterPosition(view)
            if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
                val top = view.bottom.toFloat()
                val bottom = (view.bottom + 1).toFloat()
                c.drawRect(left.toFloat(), top, right.toFloat(), bottom, dividerPaint)
            }
        }
    }


    /**
     * Decide whether the item header should be shown. Default Rules:
     * 1. the first item header always should;
     * 2. if the item's header is different than last item, it should be shown, otherwise not.
     *
     * @param itemAdapterPosition adapter position, see RecyclerView docs.
     * @return `true` if this header should be shown.
     */
    private fun shouldShowHeader(itemAdapterPosition: Int): Boolean {
        if (mAdapter == null) return itemAdapterPosition != 0
        return if (itemAdapterPosition == 0) {
            false
        } else mAdapter.getHeaderId(itemAdapterPosition) == mAdapter.getHeaderId(itemAdapterPosition - 1)
    }

    /**
     * Check whether header exists on specified position.
     *
     * @return true if this position has a header.
     */
    private fun hasHeader(adapterPosition: Int): Boolean {
        return mAdapter?.getHeaderId(adapterPosition) != StickyHeaderAdapter.NO_HEADER
    }

}
