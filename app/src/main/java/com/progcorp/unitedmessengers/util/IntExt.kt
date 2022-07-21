package com.progcorp.unitedmessengers.util

import android.content.res.Resources.getSystem

val Int.pxToDip: Int get() = (this.toFloat() / getSystem().displayMetrics.density).toInt()
val Int.dipToPx: Int get() = (this.toFloat() * getSystem().displayMetrics.density).toInt()
