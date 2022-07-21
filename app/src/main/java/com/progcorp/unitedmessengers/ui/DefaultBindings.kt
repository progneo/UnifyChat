package com.progcorp.unitedmessengers.ui

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.progcorp.unitedmessengers.util.ConvertTime
import java.util.*
import java.util.concurrent.TimeUnit

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