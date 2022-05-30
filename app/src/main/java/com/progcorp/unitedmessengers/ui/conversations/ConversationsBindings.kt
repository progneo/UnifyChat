@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversations

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
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
        unreadCount <= 0 -> {
            this.visibility = View.INVISIBLE
        }
        unreadCount > 9999 -> {
            this.text = resources.getString(
                R.string.reduce_messages_count,
                (unreadCount / 1000).toString()
            )
            this.visibility = View.VISIBLE
        }
        else -> {
            this.text = unreadCount.toString()
            this.visibility = View.VISIBLE
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
fun ImageView.bindAppbarImage(photo: String?) {
    when (photo) {
        null -> Unit
        "" -> Picasso.get().load("https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg").error(
            R.drawable.ic_baseline_account_circle_24).into(this)
        else -> {
            Picasso.get().load(photo).error(R.drawable.ic_baseline_account_circle_24).into(this)
        }
    }
}


