package com.progcorp.unitedmessengers.ui.conversations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.ListItemConversationBinding
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.interfaces.IMessageContent

class ConversationsListAdapter internal constructor(private val viewModel: IConversationsViewModel) :
    ListAdapter<(Conversation), ConversationsListAdapter.ViewHolder>(ConversationDiffCallback()) {

    class ViewHolder(private val binding: ListItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: IConversationsViewModel, item: Conversation) {
            binding.viewmodel = viewModel
            binding.conversation = item
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(viewModel, getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemConversationBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.lastMessage?.timeStamp == newItem.lastMessage?.timeStamp &&
                oldItem.unreadCount == newItem.unreadCount &&
                oldItem.getLastOnline() == newItem.getLastOnline() &&
                oldItem.getPhoto() == newItem.getPhoto() &&
                oldItem == newItem
    }
}