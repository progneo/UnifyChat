package com.progcorp.unitedmessengers.interfaces

import org.drinkless.td.libcore.telegram.TdApi
import java.io.Serializable

interface ICompanion : Serializable {
    val id: Long
    var photo: String
    var messenger: Int
    fun getName(): String
}