package com.progcorp.unitedmessengers.interfaces

import java.io.Serializable

interface ICompanion : Serializable {
    val id: Long
    var photo: String
}