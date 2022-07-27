package com.progcorp.unitedmessengers.ui.mailing

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation

@BindingAdapter("bind_mailing_list")
fun bindMailingList(listView: RecyclerView, items: List<Conversation>?) {
    items?.let {
        (listView.adapter as MailingListAdapter).submitList(items)
    }
}