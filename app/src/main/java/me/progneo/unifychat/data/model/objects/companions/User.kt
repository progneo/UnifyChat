package me.progneo.unifychat.data.model.objects.companions

import me.progneo.unifychat.data.model.interfaces.ICompanion

data class User(
    override val id: Long = 0,
    var firstName: String = "",
    var lastName: String = "",
    override var photo: String = "",
    var lastSeen: Long = 0,
    var isOnline: Boolean = false,
    var deactivated: Boolean = false,
    override var messenger: String = "",
) : ICompanion {
    override fun getName(): String {
        return "$firstName $lastName"
    }
}
