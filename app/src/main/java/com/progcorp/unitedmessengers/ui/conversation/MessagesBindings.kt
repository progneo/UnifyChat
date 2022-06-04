@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversation

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.interfaces.IMessageContent
import com.progcorp.unitedmessengers.util.Constants
import com.progcorp.unitedmessengers.util.ConvertTime
import com.squareup.picasso.Picasso

@BindingAdapter("bind_messages_list")
fun bindMessagesList(listView: RecyclerView, items: List<Message>?) {
    items?.let {
        (listView.adapter as MessagesListAdapter).submitList(items)
    }
}

@BindingAdapter("bind_online")
fun TextView.bindOnlineText(conversation: Conversation) {
    val isOnline = conversation.getIsOnline()
    if (isOnline == null) {
        text = resources.getString(R.string.bot)
    }
    else {
        text = if (conversation.getIsOnline()!!) {
            resources.getString(R.string.online)
        } else {
            when (conversation.getLastOnline()) {
                Constants.LastSeen.unknown -> {
                    resources.getString(R.string.last_seen, resources.getString(R.string.unknown))
                }
                Constants.LastSeen.lastWeek -> {
                    resources.getString(R.string.last_seen, resources.getString(R.string.last_week))
                }
                Constants.LastSeen.lastMonth -> {
                    resources.getString(
                        R.string.last_seen,
                        resources.getString(R.string.last_month)
                    )
                }
                Constants.LastSeen.recently -> {
                    resources.getString(R.string.last_seen, resources.getString(R.string.recently))
                }
                else -> {
                    resources.getString(
                        R.string.last_seen,
                        conversation.getLastOnline()?.let { ConvertTime.toDateTime(it) }
                    )
                }
            }
        }
    }
}

@BindingAdapter("bind_conversation", "bind_image_sender")
fun ImageView.bindMessageSenderImage(conversation: Conversation, path: String?) {
    if (conversation.companion is Bot || conversation.companion is User) {
        this.visibility = View.GONE
    }
    else {
        this.visibility = View.VISIBLE
        when (path) {
            null -> Unit
            "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                .error(R.drawable.ic_baseline_account_circle_24).into(this)
            else -> {
                when (conversation.messenger) {
                    Constants.Messenger.TG -> {
                        val bitmap = BitmapFactory.decodeFile(path)
                        this.setImageBitmap(bitmap)
                    }
                    Constants.Messenger.VK -> {
                        Picasso.get().load(path).error(R.drawable.ic_baseline_account_circle_24)
                            .into(this)
                    }
                }
            }
        }
    }
}

@BindingAdapter("bind_name")
fun TextView.bindNameText(sender: ICompanion) {
    when(sender) {
        is User -> {
            this.text = "${sender.firstName} ${sender.lastName}"
        }
        is Bot -> {
            this.text = sender.title
        }
        is Chat -> {
            this.text = sender.title
        }
    }
}

@BindingAdapter("bind_name", "bind_conversation")
fun TextView.bindNameInChatText(message: Message, conversation: Conversation) {
    if (conversation.companion is Bot || conversation.companion is User || message.content.text == "") {
        this.visibility = View.GONE
    }
    else {
        this.visibility = View.VISIBLE
        when(message.sender) {
            is User -> {
                this.text = "${message.sender.firstName} ${message.sender.lastName}"
            }
            is Bot -> {
                this.text = message.sender.title
            }
            is Chat -> {
                this.text = message.sender.title
            }
        }
    }
}

@BindingAdapter("bind_photo")
fun ImageView.bindPhoto(message: Message) {
    when (message.content) {
        is MessageSticker -> {
            when((message.content as MessageSticker).path) {
                "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                    .error(R.drawable.ic_baseline_account_circle_24).into(this)
                else -> {
                    if (message.messenger == Constants.Messenger.VK) {
                        Picasso.get().load((message.content as MessageSticker).path)
                            .error(R.drawable.ic_baseline_account_circle_24).into(this)
                    }
                    else {
                        val bitmap = BitmapFactory.decodeFile((message.content as MessageSticker).path)
                        this.setImageBitmap(bitmap)
                    }
                }
            }
        }
        is MessagePhoto -> {
            when((message.content as MessagePhoto).path) {
                "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                    .error(R.drawable.ic_baseline_account_circle_24).into(this)
                else -> {
                    if (message.messenger == Constants.Messenger.VK) {
                        Picasso.get().load((message.content as MessagePhoto).path)
                            .error(R.drawable.ic_baseline_account_circle_24).into(this)
                    }
                    else {
                        val bitmap = BitmapFactory.decodeFile((message.content as MessagePhoto).path)
                        this.setImageBitmap(bitmap)
                    }
                }
            }
        }
        is MessageAnimation -> {
            when((message.content as MessageAnimation).path) {
                "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                    .error(R.drawable.ic_baseline_account_circle_24).into(this)
                else -> {
                    Picasso.get().load((message.content as MessageAnimation).path)
                        .error(R.drawable.ic_baseline_account_circle_24).into(this)
                }
            }
        }
        is MessageVideo -> {
            when((message.content as MessageVideo).video) {
                "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                    .error(R.drawable.ic_baseline_account_circle_24).into(this)
                else -> {
                    Picasso.get().load((message.content as MessageVideo).video)
                        .error(R.drawable.ic_baseline_account_circle_24).into(this)
                }
            }
        }
        is MessageVideoNote -> {
            when((message.content as MessageVideoNote).video) {
                "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                    .error(R.drawable.ic_baseline_account_circle_24).into(this)
                else -> {
                    Picasso.get().load((message.content as MessageVideoNote).video)
                        .error(R.drawable.ic_baseline_account_circle_24).into(this)
                }
            }
        }
    }
}

@BindingAdapter("bind_extra_info")
fun TextView.bindExtraInfo(companion: ICompanion) {
    this.text = when (companion) {
        is User -> {
            if (companion.isOnline) {
                resources.getString(R.string.online)
            } else {
                when (companion.lastSeen) {
                    Constants.LastSeen.unknown -> {
                        resources.getString(
                            R.string.last_seen,
                            resources.getString(R.string.unknown)
                        )
                    }
                    Constants.LastSeen.lastWeek -> {
                        resources.getString(
                            R.string.last_seen,
                            resources.getString(R.string.last_week)
                        )
                    }
                    Constants.LastSeen.lastMonth -> {
                        resources.getString(
                            R.string.last_seen,
                            resources.getString(R.string.last_month)
                        )
                    }
                    Constants.LastSeen.recently -> {
                        resources.getString(
                            R.string.last_seen,
                            resources.getString(R.string.recently)
                        )
                    }
                    else -> {
                        resources.getString(
                            R.string.last_seen, ConvertTime.toDateTime(companion.lastSeen)
                        )
                    }
                }
            }

        }
        is Chat -> {
            resources.getString(
                R.string.members,
                companion.membersCount.toString()
            )
        }
        is Bot -> {
            resources.getString(R.string.bot)
        }
        else -> {
            ""
        }
    }
}

@BindingAdapter("bind_message_time")
fun TextView.bindMessageTime(timeStamp: Long) {
    this.text = ConvertTime.toTime(timeStamp)
}

@BindingAdapter("bind_message_text")
fun TextView.bindMessageText(messageContent: IMessageContent) {
    if (messageContent.text == "") {
        this.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_primary40))
        when (messageContent) {
            is MessageSticker -> this.text = "Стикер"
            is MessagePoll -> this.text = "Голосование"
            is MessagePhoto -> this.text = "Фото"
            is MessageVideoNote -> this.text = "Видео-сообщение"
            is MessageVoiceNote -> this.text = "Голосовое сообщение"
            is MessageVideo -> this.text = "Видео"
            is MessageAnimation -> this.text = "GIF"
            is MessageAnimatedEmoji -> this.text = messageContent.emoji
            is MessageCollage -> this.text = "Коллаж"
            is MessageDocument -> this.text = "Документ"
            is MessageLocation -> this.text = "Местоположение"
            else -> {
                this.text = "Необработанное сообщение"
            }
        }
    }
    else {
        this.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_neutral0))
        this.text = messageContent.text
    }
}

@BindingAdapter("bind_message_text_messages")
fun TextView.bindMessageTextInMessages(messageContent: IMessageContent) {
    if (messageContent.text == "") {
        this.visibility = View.GONE
    }
    else {
        this.visibility = View.VISIBLE
        this.text = messageContent.text
    }
}

@BindingAdapter("bind_message", "bind_message_viewModel")
fun View.bindShouldMessageShowTimeText(message: Message, viewModel: ConversationViewModel) {
    val index = viewModel.messagesList.value!!.indexOf(message)

    if (index != viewModel.messagesList.value!!.size - 1) {
        val messageBefore = viewModel.messagesList.value!![index + 1]

        val dateBefore = ConvertTime.toDateWithDayOfWeek(messageBefore.timeStamp)
        val dateThis = ConvertTime.toDateWithDayOfWeek(message.timeStamp)

        if (dateThis == dateBefore) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }
    else {
        this.visibility = View.GONE
    }
}
