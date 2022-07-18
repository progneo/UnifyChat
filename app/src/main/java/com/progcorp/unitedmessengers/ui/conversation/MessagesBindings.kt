@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversation

import android.graphics.BitmapFactory
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
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
import com.squareup.picasso.Picasso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

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
fun ImageView.bindMessageSenderImage(conversation: Conversation, user: ICompanion) {
    if (conversation.companion is Bot || conversation.companion is User) {
        this.visibility = View.GONE
    }
    else {
        this.visibility = View.VISIBLE
        when (user.photo) {
            "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png")
                .error(R.drawable.ic_account_circle).into(this)
            else -> {
                when (user.messenger) {
                    Constants.Messenger.TG -> {
                        if (!user.photo.isDigitsOnly()) {
                            val bitmap = BitmapFactory.decodeFile(user.photo)
                            this.setImageBitmap(bitmap)
                        }
                        else {
                            val client = App.application.tgClient
                            val view = this

                            MainScope().launch {
                                val photo = client.download(user.photo.toInt())
                                user.photo = photo!!
                                val bitmap = BitmapFactory.decodeFile(photo)
                                view.setImageBitmap(bitmap)
                            }
                        }
                    }
                    Constants.Messenger.VK -> {
                        Picasso.get().load(user.photo).error(R.drawable.ic_account_circle)
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
    val param = this.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(0, if (message.content.text == "") 0 else 12, 0, 0)
    this.layoutParams = param
    when (message.content) {
        is MessageSticker -> {
            when((message.content as MessageSticker).path) {
                "" -> this.setImageDrawable(null)
                else -> {
                    when (message.messenger) {
                        Constants.Messenger.VK -> {
                            Picasso.get().load((message.content as MessageSticker).path)
                                .error(R.drawable.ic_account_circle).into(this)
                        }
                        Constants.Messenger.TG -> {
                            if (!(message.content as MessageSticker).path.isDigitsOnly()) {
                                val bitmap = BitmapFactory.decodeFile((message.content as MessageSticker).path)
                                this.setImageBitmap(bitmap)
                            }
                            else {
                                val client = App.application.tgClient
                                val view = this

                                MainScope().launch {
                                    val photo = client.download((message.content as MessageSticker).path.toInt())
                                    (message.content as MessageSticker).path = photo!!
                                    val bitmap = BitmapFactory.decodeFile(photo)
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                }
            }
        }
        is MessagePhoto -> {
            when((message.content as MessagePhoto).path) {
                "" -> this.setImageDrawable(null)
                else -> {
                    when (message.messenger) {
                        Constants.Messenger.VK -> {
                            Picasso.get().load((message.content as MessagePhoto).path)
                                .error(R.drawable.ic_account_circle).into(this)
                        }
                        Constants.Messenger.TG -> {
                            if (!(message.content as MessagePhoto).path.isDigitsOnly()) {
                                val bitmap = BitmapFactory.decodeFile((message.content as MessagePhoto).path)
                                this.setImageBitmap(bitmap)
                            }
                            else {
                                val client = App.application.tgClient
                                val view = this
                                this.setImageDrawable(null)
                                MainScope().launch {
                                    val photo = client.download((message.content as MessagePhoto).path.toInt())
                                    (message.content as MessagePhoto).path = photo!!
                                    val bitmap = BitmapFactory.decodeFile(photo)
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                }
            }
        }
        is MessageAnimation -> {
            when((message.content as MessageAnimation).path) {
                "" -> this.setImageDrawable(null)
                else -> {
                    when (message.messenger) {
                        Constants.Messenger.VK -> {
                            Picasso.get().load((message.content as MessageAnimation).path)
                                .error(R.drawable.ic_account_circle).into(this)
                        }
                        Constants.Messenger.TG -> {
                            if (!(message.content as MessageAnimation).path.isDigitsOnly()) {
                                val bitmap = BitmapFactory.decodeFile((message.content as MessageAnimation).path)
                                this.setImageBitmap(bitmap)
                            }
                            else {
                                val client = App.application.tgClient
                                val view = this

                                MainScope().launch {
                                    val photo = client.download((message.content as MessageAnimation).path.toInt())
                                    (message.content as MessageAnimation).path = photo!!
                                    val bitmap = BitmapFactory.decodeFile(photo)
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                }
            }
        }
        is MessageVideo -> {
            when((message.content as MessageVideo).video) {
                "" -> this.setImageDrawable(null)
                else -> {
                    when (message.messenger) {
                        Constants.Messenger.VK -> {
                            Picasso.get().load((message.content as MessageVideo).video)
                                .error(R.drawable.ic_account_circle).into(this)
                        }
                        Constants.Messenger.TG -> {
                            if (!(message.content as MessageVideo).video.isDigitsOnly()) {
                                val bitmap = BitmapFactory.decodeFile((message.content as MessageVideo).video)
                                this.setImageBitmap(bitmap)
                            }
                            else {
                                val client = App.application.tgClient
                                val view = this

                                MainScope().launch {
                                    val photo = client.download((message.content as MessageVideo).video.toInt())
                                    (message.content as MessageVideo).video = photo!!
                                    val bitmap = BitmapFactory.decodeFile(photo)
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                }
            }
        }
        is MessageVideoNote -> {
            when((message.content as MessageVideoNote).video) {
                "" -> this.setImageDrawable(null)
                else -> {
                    when (message.messenger) {
                        Constants.Messenger.VK -> {
                            Picasso.get().load((message.content as MessageVideoNote).video)
                                .error(R.drawable.ic_account_circle).into(this)
                        }
                        Constants.Messenger.TG -> {
                            if (!(message.content as MessageVideoNote).video.isDigitsOnly()) {
                                val bitmap = BitmapFactory.decodeFile((message.content as MessageVideoNote).video)
                                this.setImageBitmap(bitmap)
                            }
                            else {
                                val client = App.application.tgClient
                                val view = this

                                MainScope().launch {
                                    val photo = client.download((message.content as MessageVideoNote).video.toInt())
                                    (message.content as MessageVideoNote).video = photo!!
                                    val bitmap = BitmapFactory.decodeFile(photo)
                                    view.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
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
