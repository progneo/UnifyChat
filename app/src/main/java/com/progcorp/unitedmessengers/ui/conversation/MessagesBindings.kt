@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversation

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.data.model.companions.Bot
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.interfaces.IMessageContent
import com.progcorp.unitedmessengers.util.Constants
import com.progcorp.unitedmessengers.util.ConvertTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
import java.lang.NumberFormatException

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
fun ImageView.bindSenderImage(conversation: Conversation, user: ICompanion) {
    if (conversation.companion is Bot || conversation.companion is User) {
        this.visibility = View.GONE
    }
    else {
        this.visibility = View.VISIBLE
        if (user.photo != "") {
            when (user.messenger) {
                Constants.Messenger.VK -> {
                    Glide.with(context)
                        .load(user.photo)
                        .into(this)
                }
                Constants.Messenger.TG -> {
                    if (!user.photo.isDigitsOnly()) {
                        val file = File(user.photo)
                        Glide.with(context)
                            .load(file)
                            .into(this)
                    }
                    else {
                        val client = App.application.tgClient

                        MainScope().launch {
                            try {
                                user.photo = client.download(user.photo.toInt())!!
                            } catch (exception: NumberFormatException) {}

                            try {
                                val file = File(user.photo)
                                Glide.with(context)
                                    .load(file)
                                    .into(this@bindSenderImage)
                            } catch (exception: Exception) {}
                        }
                    }
                }
            }
        }
        else {
            this.setImageResource(R.drawable.ic_account_circle)
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

@BindingAdapter("bind_reply_name")
fun TextView.bindReplyName(message: Message?) {
    if (message != null) {
        this.text = message.sender?.getName() ?: "Отправитель"
    }
}

@BindingAdapter("bind_replied_message")
fun View.bindRepliedMessage(message: Message) {
    this.visibility = if (message.replyToMessage != null) View.VISIBLE else View.GONE
}

@BindingAdapter("bind_reply_text")
fun TextView.bindReplyText(message: Message?) {
    if (message != null) {
        val messageContent = message.content
        if (messageContent.text == "") {
            val color = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, color, true)
            this.setTextColor(color.data)

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
            this.text = messageContent.text
        }
    }
}

@BindingAdapter("bind_photo")
fun ImageView.bindPhoto(message: Message) {
    val param = this.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(0, if (message.content.text == "") 0 else 12, 0, 0)
    this.layoutParams = param

    when (message.content) {
        is MessageSticker -> {
            val content = message.content as MessageSticker
            this.setImageDrawable(null)
            if (content.path != "") {
                when (message.messenger) {
                    Constants.Messenger.VK -> {
                        Glide.with(this.context)
                            .load(content.path)
                            .into(this)
                    }
                    Constants.Messenger.TG -> {
                        if (!content.path.isDigitsOnly()) {
                            val file = File(content.path)
                            Glide.with(this.context)
                                .load(file)
                                .placeholder(null)
                                .into(this)
                        }
                        else {
                            val client = App.application.tgClient
                            MainScope().launch {
                                try {
                                    content.path = client.download(content.path.toInt())!!
                                } catch (exception: NumberFormatException) {}

                                try {
                                    val file = File(content.path)
                                    Glide.with(this@bindPhoto.context)
                                        .load(file)
                                        .placeholder(null)
                                        .into(this@bindPhoto)
                                } catch (exception: Exception) {}
                            }
                        }
                    }
                }
            }
        }
        is MessagePhoto -> {
            val content = message.content as MessagePhoto
            this.setImageResource(R.drawable.ic_image)
            if (content.photo.path != "") {
                when (message.messenger) {
                    Constants.Messenger.VK -> {
                        content.photo.let {
                            Glide.with(this.context)
                                .load(it.path)
                                .placeholder(R.drawable.ic_image)
                                .override(it.width, it.height)
                                .centerCrop()
                                .into(this)
                        }
                    }
                    Constants.Messenger.TG -> {
                        if (!content.photo.path.isDigitsOnly()) {
                            content.photo.let {
                                val file = File(it.path)
                                Glide.with(this.context)
                                    .load(file)
                                    .placeholder(R.drawable.ic_image)
                                    .override(it.width, it.height)
                                    .centerCrop()
                                    .into(this)
                            }
                        }
                        else {
                            val client = App.application.tgClient
                            MainScope().launch {
                                try {
                                    content.photo.path = client.download(content.photo.path.toInt())!!
                                } catch (exception: NumberFormatException) {}

                                content.photo.let {
                                    try {
                                        val file = File(it.path)
                                        Glide.with(this@bindPhoto.context)
                                            .load(file)
                                            .placeholder(R.drawable.ic_image)
                                            .override(it.width, it.height)
                                            .centerCrop()
                                            .into(this@bindPhoto)
                                    } catch (exception: Exception) {}
                                }
                            }
                        }
                    }
                }
            }

        }
        else -> {
            this.setImageDrawable(null)
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
        val color = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, color, true)
        this.setTextColor(color.data)

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

@BindingAdapter("bind_message_copy_viewmodel")
fun Button.bindMessageCopyViewModel(viewModel: ConversationViewModel) {
    val message = viewModel.selectedMessage.value
    if (message != null) {
        this.visibility = if (message.content.text == "") View.GONE else View.VISIBLE
    }
}

@BindingAdapter("bind_message_reply_viewmodel")
fun Button.bindMessageReplyViewModel(viewModel: ConversationViewModel) {
    val chat = viewModel.chat.value
    if (chat != null) {
        this.visibility = if (chat.canWrite) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("bind_message_edit_viewmodel")
fun Button.bindMessageEditViewModel(viewModel: ConversationViewModel) {
    val message = viewModel.selectedMessage.value
    if (message != null) {
        if (!message.isOutgoing) {
            this.visibility = View.GONE
        }
        else {
            val messageTimestamp = message.timeStamp
            val currentTimestamp = System.currentTimeMillis()
            this.visibility = if (currentTimestamp - messageTimestamp > 86400000) View.GONE else View.VISIBLE
        }
    }
}