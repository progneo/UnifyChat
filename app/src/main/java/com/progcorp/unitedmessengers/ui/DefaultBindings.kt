package com.progcorp.unitedmessengers.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.setPadding
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.ui.conversations.bindAppbarImage
import com.progcorp.unitedmessengers.util.Constants
import com.progcorp.unitedmessengers.util.ConvertTime
import com.squareup.picasso.Picasso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


@BindingAdapter("bind_image_url")
fun ImageView.bindImageWithPicasso(url: String?) {
    when (url) {
        null -> Unit
        "" -> this.setBackgroundResource(R.drawable.ic_baseline_account_circle_24)
        else -> Picasso.get().load(url).error(R.drawable.ic_baseline_account_circle_24).into(this)
    }
}

@BindingAdapter("bind_conversation")
fun ImageView.bindConversationImage(conversation: Conversation) {
    when (conversation.getPhoto()) {
        null -> Unit
        "" -> Picasso.get().load("https://connect2id.com/assets/learn/oauth-2/user.png").error(R.drawable.ic_baseline_account_circle_24).into(this)
        else -> {
            when (conversation.messenger) {
                Constants.Messenger.TG -> {
                    if (!conversation.getPhoto()!!.isDigitsOnly()) {
                        val bitmap = BitmapFactory.decodeFile(conversation.getPhoto()!!)
                        this.setImageBitmap(bitmap)
                    }
                    else {
                        val client = App.application.tgClient
                        val view = this

                        MainScope().launch {
                            val file = conversation.getPhoto()!!.toInt().let {
                                client.resositrory.getFile(it).first()
                            }
                            val photo = client.downloadableFile(file).first()
                            conversation.companion!!.photo = file.local.path
                            val bitmap = BitmapFactory.decodeFile(photo)
                            view.setImageBitmap(bitmap)
                        }
                    }
                }
                Constants.Messenger.VK -> {
                    Picasso.get().load(conversation.getPhoto()).error(R.drawable.ic_baseline_account_circle_24).into(this)
                }
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("bind_epochTimeMsToDate_with_days_ago")
fun TextView.bindEpochTimeMsToDateWithDaysAgo(epochTimeMs: Long) {
    val numOfDays = TimeUnit.MILLISECONDS.toDays(Date().time - epochTimeMs)

    this.text = when {
        numOfDays >= 1.toLong() -> numOfDays.toString() + "d"
        else -> ConvertTime.toTime(epochTimeMs)
    }
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("bind_epochTimeMsToDate")
fun TextView.bindEpochTimeMsToDate(epochTimeMs: Long) {
    if (epochTimeMs > 0) {
        this.text = ConvertTime.toDateWithDayOfWeek(epochTimeMs)
    }
}

@BindingAdapter("bind_textview_visibility")
fun TextView.bindVisibility(text: String?) {
    if (text != null && text != "") {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.GONE
    }
}

@BindingAdapter("bind_disable_item_animator")
fun bindDisableRecyclerViewItemAnimator(recyclerView: RecyclerView, disable: Boolean) {
    if (disable) {
        recyclerView.itemAnimator = null
    }
}

