package com.progcorp.unitedmessengers.ui.conversation.swipecontroller

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.ceil


class MessageSwipeController(private val context: Context, private val swipeControllerActions: SwipeControllerActions) :
    ItemTouchHelper.Callback() {

    private var _currentItemViewHolder: RecyclerView.ViewHolder? = null
    private var _view: View? = null
    private var _dX = 0f

    private var _swipeBack = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        _view = viewHolder.itemView
        return makeMovementFlags(ACTION_STATE_IDLE, LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (_swipeBack) {
            _swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder)
        }

        if (_view?.translationX!! > pxToDip(-70f, context) || dX > this._dX) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            this._dX = dX
        }
        _currentItemViewHolder = viewHolder
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            _swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (_swipeBack) {
                if (abs(_view!!.translationX) >= this@MessageSwipeController.pxToDip(50f, context)) {
                    swipeControllerActions.replyToMessage(viewHolder.adapterPosition)
                }
            }
            false
        }
    }

    private fun pxToDip(px: Float, context: Context): Int {
        return ceil((context.resources.displayMetrics.density * px).toDouble()).toInt()
    }
}