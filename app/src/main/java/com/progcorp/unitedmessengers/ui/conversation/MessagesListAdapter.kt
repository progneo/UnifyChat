package com.progcorp.unitedmessengers.ui.conversation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.databinding.*
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first

class MessagesListAdapter(private val viewModel: ConversationViewModel) : ListAdapter<Message, RecyclerView.ViewHolder>(
    MessageDiffCallback()
) {

    class MessageViewHolder(private val binding: ListItemMessageTextBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class StickerViewHolder(private val binding: ListItemMessageStickerBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class AttachmentViewHolder(private val binding: ListItemMessageAttachmentBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class PhotoViewHolder(private val binding: ListItemMessagePhotoBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class ChatViewHolder(private val binding: ListItemMessageChatBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class OutMessageViewHolder(private val binding: ListItemMessageTextOutBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class OutStickerViewHolder(private val binding: ListItemMessageStickerOutBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class OutAttachmentViewHolder(private val binding: ListItemMessageAttachmentOutBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    class OutPhotoViewHolder(private val binding: ListItemMessagePhotoOutBinding)  :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ConversationViewModel, item: Message) {
            binding.viewmodel = viewModel
            binding.message = item
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        val type: Int = when (message.content) {
            is MessageText -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.text
                }
                else {
                    Constants.MessageType.textOut
                }
            }
            is MessageSticker -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.sticker
                }
                else {
                    Constants.MessageType.stickerOut
                }
            }
            is MessageChat -> {
                Constants.MessageType.chat
            }
            is MessageAnimatedEmoji -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.animatedEmoji
                }
                else {
                    Constants.MessageType.animatedEmojiOut
                }
            }
            is MessageAnimation -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.animation
                }
                else {
                    Constants.MessageType.animationOut
                }
            }
            is MessageCollage -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.collage
                }
                else {
                    Constants.MessageType.collageOut
                }
            }
            is MessageDocument -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.document
                }
                else {
                    Constants.MessageType.documentOut
                }
            }
            is MessageDocuments -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.documents
                }
                else {
                    Constants.MessageType.documentsOut
                }
            }
            is MessageExpiredPhoto -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.expiredPhoto
                }
                else {
                    Constants.MessageType.expiredPhotoOut
                }
            }
            is MessageExpiredVideo -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.expiredVideo
                }
                else {
                    Constants.MessageType.expiredVideoOut
                }
            }
            is MessagePhoto -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.photo
                }
                else {
                    Constants.MessageType.photoOut
                }
            }
            is MessagePoll -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.poll
                }
                else {
                    Constants.MessageType.pollOut
                }
            }
            is MessageVideo -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.video
                }
                else {
                    Constants.MessageType.videoOut
                }
            }
            is MessageVideoNote -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.videoNote
                }
                else {
                    Constants.MessageType.videoNoteOut
                }
            }
            is MessageVoiceNote -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.voiceNote
                }
                else {
                    Constants.MessageType.voiceNoteOut
                }
            }
            else -> {
                if (!message.isOutgoing) {
                    Constants.MessageType.unknown
                }
                else {
                    Constants.MessageType.unknownOut
                }
            }
        }
        return type
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            Constants.MessageType.text -> {
                val binding = ListItemMessageTextBinding.inflate(layoutInflater, parent, false)
                MessageViewHolder(binding)
            }
            Constants.MessageType.textOut -> {
                val binding = ListItemMessageTextOutBinding.inflate(layoutInflater, parent, false)
                OutMessageViewHolder(binding)
            }
            Constants.MessageType.sticker -> {
                val binding = ListItemMessageStickerBinding.inflate(layoutInflater, parent, false)
                StickerViewHolder(binding)
            }
            Constants.MessageType.stickerOut -> {
                val binding = ListItemMessageStickerOutBinding.inflate(layoutInflater, parent, false)
                OutStickerViewHolder(binding)
            }
            Constants.MessageType.chat -> {
                val binding = ListItemMessageChatBinding.inflate(layoutInflater, parent, false)
                ChatViewHolder(binding)
            }
            Constants.MessageType.photo -> {
                val binding = ListItemMessagePhotoBinding.inflate(layoutInflater, parent, false)
                PhotoViewHolder(binding)
            }
            Constants.MessageType.photoOut -> {
                val binding = ListItemMessagePhotoOutBinding.inflate(layoutInflater, parent, false)
                OutPhotoViewHolder(binding)
            }
            else -> {
                val binding = ListItemMessageAttachmentBinding.inflate(layoutInflater, parent, false)
                AttachmentViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            Constants.MessageType.text -> (holder as MessageViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Constants.MessageType.textOut -> (holder as OutMessageViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Constants.MessageType.sticker -> (holder as StickerViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Constants.MessageType.stickerOut -> (holder as OutStickerViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Constants.MessageType.chat -> (holder as ChatViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Constants.MessageType.photo -> (holder as PhotoViewHolder).bind(
                viewModel,
                getItem(position)
            )
            Constants.MessageType.photoOut -> (holder as OutPhotoViewHolder).bind(
                viewModel,
                getItem(position)
            )
            else -> {
                (holder as AttachmentViewHolder).bind(
                    viewModel,
                    getItem(position)
                )
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        val isFileSame: Boolean = when (newItem.content) {
            is MessageSticker -> {
                (oldItem.content as MessageSticker).path == (newItem.content as MessageSticker).path
            }
            is MessagePhoto -> {
                (oldItem.content as MessagePhoto).path == (newItem.content as MessagePhoto).path
            }
            is MessageAnimation -> {
                (oldItem.content as MessageAnimation).path == (newItem.content as MessageAnimation).path
            }
            is MessageVideo -> {
                (oldItem.content as MessageVideo).video == (newItem.content as MessageVideo).video
            }
            is MessageVideoNote -> {
                (oldItem.content as MessageVideoNote).video == (newItem.content as MessageVideoNote).video
            }
            else -> {
                true
            }
        }
        return oldItem.content.text == newItem.content.text &&
                oldItem.sender?.photo == newItem.sender?.photo &&
                isFileSame
    }
}