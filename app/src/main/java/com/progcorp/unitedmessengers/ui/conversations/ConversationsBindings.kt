@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversations

import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException

@BindingAdapter("bind_conversations_list")
fun bindConversationsList(listView: RecyclerView, items: List<Conversation>?) {
    items?.let { (listView.adapter as ConversationsListAdapter).submitList(items) }
}

@BindingAdapter("bind_read_state")
fun TextView.bindReadState(unreadCount: Int) {
    if (unreadCount == -1) {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.INVISIBLE
    }
}

@BindingAdapter("bind_unread_count")
fun TextView.bindUnreadCount(unreadCount: Int) {
    when {
        unreadCount == 0 -> {
            this.visibility = View.INVISIBLE
        }
        unreadCount == -1 -> {
            this.visibility = View.VISIBLE
            this.width = 7
            this.height = 7
        }
        unreadCount > 9999 -> {
            this.text = resources.getString(
                R.string.reduce_messages_count,
                (unreadCount / 1000).toString()
            )
            this.visibility = View.VISIBLE
            this.width = WindowManager.LayoutParams.WRAP_CONTENT
            this.width = WindowManager.LayoutParams.WRAP_CONTENT
        }
        else -> {
            this.text = unreadCount.toString()
            this.visibility = View.VISIBLE
            this.width = WindowManager.LayoutParams.WRAP_CONTENT
            this.width = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }
}

@BindingAdapter("bind_online")
fun View.bindOnline(companion: ICompanion) {
    when (companion) {
        is User -> {
            this.visibility = if (companion.isOnline) View.VISIBLE else View.INVISIBLE
        }
        else -> {
            this.visibility = View.INVISIBLE
        }
    }
}

@BindingAdapter("bind_appbar_name")
fun TextView.bindUser(user: User?) {
    this.text = if (user == null) {
        resources.getString(R.string.login_text)
    }
    else {
        "${user.firstName} ${user.lastName}"
    }
}

@BindingAdapter("bind_appbar_image")
fun ImageView.bindAppbarImage(user: User?) {
    when (user?.photo) {
        null -> Unit
        "" -> {
            this.setImageResource(R.drawable.user)
        }
        else -> {
            when (user.messenger) {
                Constants.Messenger.VK -> {
                    Glide.with(this.context)
                        .load(user.photo)
                        .into(this)
                }
                Constants.Messenger.TG -> {
                    if (!user.photo.isDigitsOnly()) {
                        val file = File(user.photo)
                        Glide.with(this.context)
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
                                Glide.with(this@bindAppbarImage.context)
                                    .load(file)
                                    .into(this@bindAppbarImage)
                            } catch (exception: Exception) {}
                        }
                    }
                }
            }
        }
    }
}

@BindingAdapter("bind_is_outgoing")
fun TextView.bindIsOutgoing(message: Message) {
    if (message.isOutgoing) {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.GONE
    }
}


@BindingAdapter("bind_conversation")
fun ImageView.bindConversationImage(conversation: Conversation) {
    when (conversation.getPhoto()) {
        null -> Unit
        "" -> Glide.with(this.context)
            .load(R.drawable.user)
            .into(this)
        else -> {
            when (conversation.messenger) {
                Constants.Messenger.TG -> {
                    if (!conversation.getPhoto()!!.isDigitsOnly()) {
                        val file = File(conversation.getPhoto()!!)
                        Glide.with(this.context)
                            .load(file)
                            .into(this)
                    }
                    else {
                        val client = App.application.tgClient

                        MainScope().launch {
                            try {
                                conversation.companion?.photo = client.download(conversation.getPhoto()!!.toInt())!!
                            } catch (exception: NumberFormatException) {}

                            val file = File(conversation.getPhoto()!!)
                            try {
                                Glide.with(this@bindConversationImage.context)
                                    .load(file)
                                    .into(this@bindConversationImage)
                            } catch (exception: Exception) {}
                        }
                    }
                }
                Constants.Messenger.VK -> {
                    Glide.with(this.context)
                        .load(conversation.getPhoto())
                        .into(this)
                }
            }
        }
    }
}

@BindingAdapter("bind_long_click_conversation", "bind_long_click_view_model")
fun View.bindLongClick(conversation: Conversation, viewModel: IConversationsViewModel) {
    this.setOnLongClickListener(View.OnLongClickListener {
        viewModel.longClickOnConversation(this, conversation)
        return@OnLongClickListener true
    })
}