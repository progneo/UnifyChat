package com.progcorp.unitedmessengers.ui.conversation

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.util.Constants

@BindingAdapter("bind_reply_message")
fun View.bindReplyMessageLayout(message: Message?) {
    if (message != null) {
        if (this.visibility != View.VISIBLE) {
            this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_up_animation))
        }
        this.visibility = View.VISIBLE
    }
    else {
        this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_down_animation))
        this.visibility = View.GONE
    }
}

@BindingAdapter("bind_edit_message")
fun View.bindEditMessageLayout(message: Message?) {
    if (message != null) {
        if (this.visibility != View.VISIBLE) {
            this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_up_animation))
        }
        this.visibility = View.VISIBLE
    }
    else {
        this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_down_animation))
        this.visibility = View.GONE
    }
}

@BindingAdapter("bind_messenger")
fun TextView.bindMessenger(messenger: Int) {
    this.text = if (messenger == Constants.Messenger.TG) resources.getString(R.string.telegram) else resources.getString(R.string.vk)
}