package com.progcorp.unitedmessengers.ui.conversation

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.progcorp.unitedmessengers.data.model.Message

@BindingAdapter("bind_reply_message")
fun View.bindReplyMessageLayout(message: Message?) {
    if (message != null) {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.GONE
    }
}

@BindingAdapter("bind_reply_name")
fun TextView.bindReplyMessageName(message: Message?) {
    if (message != null) {
        this.text = message.content.text
    }
}

@BindingAdapter("bind_reply_text")
fun TextView.bindReplyMessageText(message: Message?) {
    if (message != null) {
        this.text = message.content.text
    }
}

