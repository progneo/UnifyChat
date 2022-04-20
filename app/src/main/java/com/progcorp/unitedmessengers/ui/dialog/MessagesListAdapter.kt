package com.progcorp.unitedmessengers.ui.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.databinding.*

class MessagesListAdapter(private val viewModel: DialogViewModel) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    class DialogMessageViewHolder(private val binding: ListItemDialogMessageBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: DialogViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogStickerViewHolder(private val binding: ListItemDialogStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: DialogViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.sticker).into(binding.photoStickerLeft)
            binding.executePendingBindings()
        }
    }

    class DialogAttachmentViewHolder(private val binding: ListItemDialogAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: DialogViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogOutMessageViewHolder(private val binding: ListItemDialogOutMessageBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: DialogViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class DialogOutStickerViewHolder(private val binding: ListItemDialogOutStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: DialogViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            //Picasso.get().load(item.senderPhoto).into(binding.photoStickerRight)
            binding.executePendingBindings()
        }
    }

    class DialogOutAttachmentViewHolder(private val binding: ListItemDialogOutAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: DialogViewModel, item: Message) {
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

            Message.DIALOG_MESSAGE_OUT -> {
                val binding = ListItemDialogOutMessageBinding.inflate(layoutInflater, parent, false)
                DialogOutMessageViewHolder(binding)
            }

            Message.DIALOG_STICKER_OUT -> {
                val binding = ListItemDialogOutStickerBinding.inflate(layoutInflater, parent, false)
                DialogOutStickerViewHolder(binding)
            }

            Message.DIALOG_ATTACHMENT_OUT -> {
                val binding = ListItemDialogOutAttachmentBinding.inflate(layoutInflater, parent, false)
                DialogOutAttachmentViewHolder(binding)
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
                throw Exception("Error reading holder type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            Message.DIALOG_MESSAGE_OUT -> (holder as DialogOutMessageViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.DIALOG_STICKER_OUT -> (holder as DialogOutStickerViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Message.DIALOG_ATTACHMENT_OUT -> (holder as DialogOutAttachmentViewHolder).bind(
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
                throw Exception("Error reading holder type")
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }
}