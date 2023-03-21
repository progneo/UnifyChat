package me.progneo.unifychat.data.model.interfaces

import java.io.Serializable

interface ICompanion : Serializable {
    val id: Long
    var photo: String
    var messenger: String
    fun getName(): String
}