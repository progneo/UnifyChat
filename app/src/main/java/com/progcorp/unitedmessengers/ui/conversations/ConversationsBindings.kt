@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversations

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.ui.bindConversationImageWithPicasso
import com.progcorp.unitedmessengers.util.Constants
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList

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


