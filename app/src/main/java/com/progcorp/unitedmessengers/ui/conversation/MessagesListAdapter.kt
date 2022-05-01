package com.progcorp.unitedmessengers.ui.conversation

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.databinding.*

class MessagesListAdapter(private val viewModel: ConversationViewModel) : ListAdapter<Message, RecyclerView.ViewHolder>(
    MessageDiffCallback()
) {

    class ChatMessageViewHolder(private val binding: ListItemChatMessageBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.senderPhoto).into(binding.photoSender)
            binding.executePendingBindings()
        }
    }

    class ChatStickerViewHolder(private val binding: ListItemChatStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.senderPhoto).into(binding.photoSender)
            //Picasso.get().load(item.sticker).into(binding.photoStickerLeft)
            binding.executePendingBindings()
        }
    }

    class ChatAttachmentViewHolder(private val binding: ListItemChatAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.senderPhoto).into(binding.photoSender)
            binding.executePendingBindings()
        }
    }

    class ChatActionViewHolder(private val binding: ListItemChatActionBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class ChatOutMessageViewHolder(private val binding: ListItemChatOutMessageBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class ChatOutStickerViewHolder(private val binding: ListItemChatOutStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.senderPhoto).into(binding.photoStickerRight)
            binding.executePendingBindings()
        }
    }

    class ChatOutAttachmentViewHolder(private val binding: ListItemChatOutAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogMessageViewHolder(private val binding: ListItemDialogMessageBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogStickerViewHolder(private val binding: ListItemDialogStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.sticker).into(binding.photoStickerLeft)
            binding.executePendingBindings()
        }
    }

    class DialogAttachmentViewHolder(private val binding: ListItemDialogAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogOutMessageViewHolder(private val binding: ListItemDialogOutMessageBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogOutStickerViewHolder(private val binding: ListItemDialogOutStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.senderPhoto).into(binding.photoStickerRight)
            binding.executePendingBindings()
        }
    }

    class DialogOutAttachmentViewHolder(private val binding: ListItemDialogOutAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            Message.MESSAGE_OUT -> {
                val binding = ListItemChatOutMessageBinding.inflate(layoutInflater, parent, false)
                ChatOutMessageViewHolder(binding)
            }

            Message.STICKER_OUT -> {
                val binding = ListItemChatOutStickerBinding.inflate(layoutInflater, parent, false)
                ChatOutStickerViewHolder(binding)
            }

            Message.ATTACHMENT_OUT -> {
                val binding = ListItemChatOutAttachmentBinding.inflate(layoutInflater, parent, false)
                ChatOutAttachmentViewHolder(binding)
            }

            Message.CHAT_MESSAGE -> {
                val binding = ListItemChatMessageBinding.inflate(layoutInflater, parent, false)
                ChatMessageViewHolder(binding)
            }

            Message.CHAT_STICKER -> {
                val binding = ListItemChatStickerBinding.inflate(layoutInflater, parent, false)
                ChatStickerViewHolder(binding)
            }

            Message.CHAT_ATTACHMENT -> {
                val binding = ListItemChatAttachmentBinding.inflate(layoutInflater, parent, false)
                ChatAttachmentViewHolder(binding)
            }

            Message.CHAT_ACTION -> {
                val binding = ListItemChatActionBinding.inflate(layoutInflater, parent, false)
                ChatActionViewHolder(binding)
            }

            Message.DIALOG_MESSAGE -> {
                val binding = ListItemDialogMessageBinding.inflate(layoutInflater, parent, false)
                DialogMessageViewHolder(binding)
            }

            Message.DIALOG_STICKER -> {
                val binding = ListItemDialogStickerBinding.inflate(layoutInflater, parent, false)
                DialogStickerViewHolder(binding)
            }

            Message.DIALOG_ATTACHMENT -> {
                val binding = ListItemDialogAttachmentBinding.inflate(layoutInflater, parent, false)
                DialogAttachmentViewHolder(binding)
            }

            else -> {
                Log.e("MessagesListAdapter", "Wrong type: $viewType")
                throw Exception("Error reading holder type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            Message.MESSAGE_OUT -> (holder as ChatOutMessageViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.STICKER_OUT -> (holder as ChatOutStickerViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.ATTACHMENT_OUT -> (holder as ChatOutAttachmentViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.CHAT_MESSAGE -> (holder as ChatMessageViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.CHAT_STICKER -> (holder as ChatStickerViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.CHAT_ATTACHMENT -> (holder as ChatAttachmentViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.CHAT_ACTION -> (holder as ChatActionViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.DIALOG_MESSAGE -> (holder as DialogMessageViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.DIALOG_STICKER -> (holder as DialogStickerViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.DIALOG_ATTACHMENT -> (holder as DialogAttachmentViewHolder).bind(
                viewModel,
                getItem(position)
            )
            else -> {
                Log.e("MessagesListAdapter", "Wrong type: $holder.itemViewType")
                throw Exception("Error reading holder type")
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.text == newItem.text
    }
}