package com.progcorp.unitedmessengers.ui

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.util.ConvertTime
import com.squareup.picasso.Picasso
import java.util.*
import java.util.concurrent.TimeUnit


@BindingAdapter("bind_image_url")
fun bindImageWithPicasso(imageView: ImageView, url: String?) {
    when (url) {
        null -> Unit
        "" -> imageView.setBackgroundResource(R.drawable.ic_baseline_account_circle_24)
        else -> Picasso.get().load(url).error(R.drawable.ic_baseline_account_circle_24).into(imageView)
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

