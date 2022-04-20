@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversations.vk

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation

@BindingAdapter("bind_conversations_list")
fun bindConversationsList(listView: RecyclerView, items: List<Conversation>?) {
    items?.let { (listView.adapter as ConversationsListAdapter).submitList(items) }
}