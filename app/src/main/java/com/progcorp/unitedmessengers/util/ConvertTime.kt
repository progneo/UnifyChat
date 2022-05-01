package com.progcorp.unitedmessengers.util

import java.text.SimpleDateFormat
import java.util.*

object ConvertTime {
    fun toTime(timeStamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm")
        val date = Date(timeStamp)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
    fun toDateTime(timeStamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM HH:mm")
        val date = Date(timeStamp)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
    fun toFullDate(timeStamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy")
        val date = Date(timeStamp)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
    fun toDateWithDayOfWeek(timeStamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM")
        val date = Date(timeStamp)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}