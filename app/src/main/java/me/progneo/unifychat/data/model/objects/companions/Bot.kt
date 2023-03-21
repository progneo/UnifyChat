package me.progneo.unifychat.data.model.objects.companions

import me.progneo.unifychat.data.model.interfaces.ICompanion

data class Bot(
    override val id: Long = 0,
    var title: String = "",
    override var photo: String = "",
    override var messenger: String = ""
) : ICompanion {
    override fun getName(): String {
        return title
    }
}