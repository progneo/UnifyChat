@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversations

import android.graphics.BitmapFactory
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import com.squareup.picasso.Picasso

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
        "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png").error(
            R.drawable.ic_baseline_account_circle_24).into(this)
        else -> {
            when (user.messenger) {
                Constants.Messenger.TG -> {
                    val bitmap = BitmapFactory.decodeFile(user.photo)
                    this.setImageBitmap(bitmap)
                }
                Constants.Messenger.VK -> {
                    Picasso.get().load(user.photo).error(R.drawable.ic_baseline_account_circle_24).into(this)
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

