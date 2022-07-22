package com.progcorp.unitedmessengers.ui.conversations

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
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

    init {
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(viewModel, getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return viewModel.conversationsList.value?.get(position)?.id ?: RecyclerView.NO_ID
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
        return oldItem.lastMessage?.content?.text == newItem.lastMessage?.content?.text &&
                oldItem.lastMessage?.timeStamp == newItem.lastMessage?.timeStamp &&
                oldItem.getLastOnline() == newItem.getLastOnline() &&
                oldItem.unreadCount == newItem.unreadCount &&
                oldItem.getPhoto() == newItem.getPhoto() &&
                oldItem == newItem
    }
}