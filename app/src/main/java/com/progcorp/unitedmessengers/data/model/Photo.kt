package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.util.dipToPx
import java.io.Serializable

data class Photo(
    val id: Long = 0,
    var width: Int = 0,
    var height: Int = 0,
    var path: String = ""
) : Serializable {
    fun adaptToChatSize() {
        val newWidth = 266.dipToPx
        val diff: Float = newWidth.toFloat() / width.toFloat()
        val newHeight = (height * diff).toInt()

        width = newWidth
        height = newHeight
    }
}