@file:Suppress("unused")

package com.progcorp.unitedmessengers.ui.conversation

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.util.Constants
import com.progcorp.unitedmessengers.util.ConvertTime
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_dialog.view.*

@BindingAdapter("bind_messages_list")
fun bindMessagesList(listView: RecyclerView, items: List<Message>?) {
    items?.let {
        (listView.adapter as MessagesListAdapter).submitList(items)
    }
}

@BindingAdapter("bind_online")
fun TextView.bindOnlineText(conversation: Conversation) {
    text = if (conversation.is_online) {
        resources.getString(R.string.online)
    }
    else {
        when (conversation.last_online) {
            Constants.LastSeen.unknown -> {
                resources.getString(R.string.last_seen, resources.getString(R.string.unknown))
            }
            Constants.LastSeen.lastWeek -> {
                resources.getString(R.string.last_seen, resources.getString(R.string.last_week))
            }
            Constants.LastSeen.lastMonth -> {
                resources.getString(R.string.last_seen, resources.getString(R.string.last_month))
            }
            Constants.LastSeen.recently -> {
                resources.getString(R.string.last_seen, resources.getString(R.string.recently))
            }
            else -> {
                resources.getString(R.string.last_seen, ConvertTime.toDateTime(conversation.last_online))
            }
        }
    }
}

@BindingAdapter("bind_message", "bind_message_viewModel")
fun View.bindShouldMessageShowTimeText(message: Message, viewModel: ConversationViewModel) {
    val index = viewModel.messagesList.value!!.indexOf(message)

    if (index != viewModel.messagesList.value!!.size - 1) {
        val messageBefore = viewModel.messagesList.value!![index + 1]

        val dateBefore = ConvertTime.toDateWithDayOfWeek(messageBefore.date)
        val dateThis = ConvertTime.toDateWithDayOfWeek(message.date)

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
