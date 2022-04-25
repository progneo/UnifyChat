@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.dialog

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.util.ConvertTime

@BindingAdapter("bind_dialog_messages_list")
fun bindMessagesList(listView: RecyclerView, items: List<Message>?) {
    items?.let {
        (listView.adapter as MessagesListAdapter).submitList(items)
    }
}

@BindingAdapter("bind_online")
fun TextView.bindOnlineText(conversation: Conversation) {
    text = if (conversation.is_online) {
        resources.getString(R.string.online)
    }
    else {
        resources.getString(R.string.last_seen, ConvertTime.toDateTime(conversation.last_online))
    }
}