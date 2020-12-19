package com.zdan.todoapp.util


import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zdan.todoapp.R
import timber.log.Timber

private const val SWIPE_THRESHOLD = 0.7f

abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.Callback() {

    private val mBackground = ColorDrawable(Color.RED)
    private val mClearPaint = Paint()
    private val deleteDrawable: Drawable
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int

    init {
        mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!
        intrinsicWidth = deleteDrawable.intrinsicWidth
        intrinsicHeight = deleteDrawable.intrinsicHeight
    }

    // swipe left only
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) = makeMovementFlags(0, ItemTouchHelper.LEFT)

    // minimum swipe movement to be recognized as fully swiped
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = SWIPE_THRESHOLD

    abstract override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false // disable move

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // change view holder while swiping
        Timber.d( "onChildDrawOver called")
        if (isCurrentlyActive) {
            getDefaultUIUtil().onDrawOver(
                c,
                recyclerView,
                viewHolder?.forSwipe(),
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
        else if (dX == 0f) {
            getDefaultUIUtil().onDrawOver(
                c,
                recyclerView,
                viewHolder?.onSwipeCancelled()?.itemView,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        getDefaultUIUtil().clearView(viewHolder.onSwipeCancelled().itemView)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // add background and icon behind view holder while swiping
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val itemHeight = itemView.height

//        val isCancelled : Boolean = ((dX == 0f) && !isCurrentlyActive)
        val isCancelled: Boolean = !isCurrentlyActive

        if (isCancelled) {
            // clear background when cancelled
            Timber.d( "swipe cancelled")
            drawBackground(itemView, dX, canvas)
        } else {
            // draw background color
            drawBackground(itemView, dX, canvas)

            // draw icon
            drawIcon(itemHeight, itemView, canvas)
        }
    }

    private fun drawIcon(
        itemHeight: Int,
        itemView: View,
        canvas: Canvas
    ) {
        val deleteIconMargin: Int = (itemHeight - intrinsicHeight) / 2
        val deleteIconTop: Int = itemView.top + deleteIconMargin
        val deleteIconLeft: Int = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight: Int = itemView.right - deleteIconMargin
        val deleteIconBottom: Int = deleteIconTop + intrinsicHeight

        deleteDrawable.setBounds(
            deleteIconLeft,
            deleteIconTop,
            deleteIconRight,
            deleteIconBottom
        )
        deleteDrawable.draw(canvas)
    }

    private fun drawBackground(
        itemView: View,
        dX: Float,
        canvas: Canvas
    ) {
        val backgroundCornerOffset = 20
        mBackground.setBounds(
            itemView.right + dX.toInt() - backgroundCornerOffset,
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        mBackground.draw(canvas)
    }

    private fun RecyclerView.ViewHolder.forSwipe(): View = itemView
        .apply {
            Timber.d("forSwipe called")
            // change view holder background shape when swiping
            background =
                ContextCompat.getDrawable(context, R.drawable.shape_list_item_swiped)
        }

    private fun RecyclerView.ViewHolder.onSwipeCancelled(): RecyclerView.ViewHolder =
        this.apply {
            Timber.d("onSwipeCancelled called")
            // change view holder background shape when swiping
            itemView.background =
                ContextCompat.getDrawable(itemView.context, R.drawable.shape_list_item)
        }
}