package com.progcorp.unitedmessengers.ui.conversation.swipecontroller

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min


class MessageSwipeController(private val context: Context, private val swipeControllerActions: SwipeControllerActions) :
    ItemTouchHelper.Callback() {

    private var _imageDrawable: Drawable? = null

    private var _currentItemViewHolder: RecyclerView.ViewHolder? = null
    private var _view: View? = null
    private var _dX = 0f

    private var _replyButtonProgress: Float = 0.toFloat()
    private var _lastReplyButtonAnimationTime: Long = 0
    private var _swipeBack = false
    private var _isVibrate = false
    private var _startTracking = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        _imageDrawable = context.getDrawable(R.drawable.ic_reply)!!
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
            _startTracking = true
        }
        _currentItemViewHolder = viewHolder
        drawReplyButton(c)
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

    private fun drawReplyButton(canvas: Canvas) {
        if (_currentItemViewHolder == null) {
            return
        }
        val translationX = _view?.translationX!!
        val newTime = System.currentTimeMillis()
        val dt = min(17, newTime - _lastReplyButtonAnimationTime)
        _lastReplyButtonAnimationTime = newTime
        val showing = translationX <= pxToDip(-30f, context)
        if (showing) {
            if (_replyButtonProgress < 1.0f) {
                _replyButtonProgress += dt / 180.0f
                if (_replyButtonProgress > 1.0f) {
                    _replyButtonProgress = 1.0f
                } else {
                    _view?.invalidate()
                }
            }
        } else if (translationX >= 0.0f) {
            _replyButtonProgress = 0f
            _startTracking = false
            _isVibrate = false
        } else {
            if (_replyButtonProgress > 0.0f) {
                _replyButtonProgress -= dt / 180.0f
                if (_replyButtonProgress < 0.1f) {
                    _replyButtonProgress = 0f
                } else {
                    _view?.invalidate()
                }
            }
        }
        val alpha: Int
        val scale: Float
        if (showing) {
            scale = if (_replyButtonProgress <= 0.8f) {
                1.2f * (_replyButtonProgress / 0.8f)
            } else {
                1.2f - 0.2f * ((_replyButtonProgress - 0.8f) / 0.2f)
            }
            alpha = min(255f, 255 * (_replyButtonProgress / 0.8f)).toInt()
        } else {
            scale = _replyButtonProgress
            alpha = min(255f, 255 * _replyButtonProgress).toInt()
        }

        _imageDrawable?.alpha = alpha
        if (_startTracking) {
            if (!_isVibrate && _view?.translationX!! <= pxToDip(-50f, context)) {
                _view!!.performHapticFeedback(
                    HapticFeedbackConstants.GESTURE_START,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                _isVibrate = true
            }
        }

        val x: Int = _view?.measuredWidth!! - pxToDip(30f, context)

        val y = (_view?.top!! + _view?.measuredHeight!! / 2).toFloat()
        _imageDrawable?.setBounds(
            (x - pxToDip(12f, context) * scale).toInt(),
            (y - pxToDip(11f, context) * scale).toInt(),
            (x + pxToDip(12f, context) * scale).toInt(),
            (y + pxToDip(10f, context) * scale).toInt()
        )
        _imageDrawable?.draw(canvas)
        _imageDrawable?.alpha = 255
    }

    private fun pxToDip(px: Float, context: Context): Int {
        return ceil((context.resources.displayMetrics.density * px).toDouble()).toInt()
    }
}