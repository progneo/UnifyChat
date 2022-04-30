@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversations.vk

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation

@BindingAdapter("bind_conversations_list")
fun bindConversationsList(listView: RecyclerView, items: List<Conversation>?) {
    items?.let { (listView.adapter as ConversationsListAdapter).submitList(items) }
}

@BindingAdapter("bind_read_state")
fun TextView.bindReadState(unreadCount: Int) {
    if (unreadCount == -1) {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.INVISIBLE
    }
}

@BindingAdapter("bind_unread_count")
fun TextView.bindUnreadCount(unreadCount: Int) {
    when {
        unreadCount <= 0 -> {
            this.visibility = View.INVISIBLE
        }
        unreadCount > 9 -> {
            this.text = unreadCount.toString()
            this.visibility = View.VISIBLE
            this.width = 100
        }
        else -> {
            this.text = unreadCount.toString()
            this.visibility = View.VISIBLE
            this.width = 50
        }
    }
}

@BindingAdapter("bind_online")
fun View.bindOnline(isOnline: Boolean) {
    if (isOnline) {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.GONE
    }
}