package com.progcorp.unitedmessengers.ui.conversations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.ListItemConversationBinding
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel

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
    override fun areItemsTheSame(oldItem: Conversation, itemWithUserInfo: Conversation): Boolean {
        return oldItem.id == itemWithUserInfo.id
    }

    override fun areContentsTheSame(oldItem: Conversation, itemWithUserInfo: Conversation): Boolean {
        return oldItem.last_message == itemWithUserInfo.last_message &&
                oldItem.unread_count == itemWithUserInfo.unread_count &&
                oldItem.last_online == itemWithUserInfo.last_online &&
                oldItem.photo == itemWithUserInfo.photo
    }
}