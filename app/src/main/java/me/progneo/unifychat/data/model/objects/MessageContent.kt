package me.progneo.unifychat.data.model.objects

import me.progneo.unifychat.data.model.interfaces.IMessageContent

data class MessageText(
    override var text: String = "",
) : IMessageContent

data class MessageUnknown(
    override var text: String = "",
    val info: String = "",
) : IMessageContent
