package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.interfaces.IMessageContent

data class MessageText(
    override val text: String = ""
) : IMessageContent

data class MessageSticker(
    override val text: String = "",
    var path: String = ""
) : IMessageContent

data class MessageChat(
    override val text: String = ""
) : IMessageContent

data class MessageAnimatedEmoji(
    override val text: String = "",
    val sticker: MessageSticker = MessageSticker(),
    val emoji: String = ""
) : IMessageContent

data class MessageAnimation(
    override val text: String = "",
    var path: String = ""
) : IMessageContent

data class MessageCollage(
    override val text: String = "",
    val paths: ArrayList<String> = arrayListOf()
) : IMessageContent

data class MessageDocument(
    override val text: String = "",
    val file: String = ""
) : IMessageContent
//TODO: File class

data class MessageDocuments(
    override val text: String = "",
    val files: ArrayList<String> = arrayListOf()
) : IMessageContent

data class MessageExpiredPhoto(
    override val text: String = "Удалённое фото"
) : IMessageContent

data class MessageExpiredVideo(
    override val text: String = "Удалённое видео"
) : IMessageContent

data class MessageLocation(
    override val text: String = "",
    val location: String = "Локация"
) : IMessageContent

data class MessagePhoto(
    override val text: String = "",
    var path: String = ""
) : IMessageContent

data class MessagePoll(
    override val text: String = "",
    val poll: String = ""
) : IMessageContent

data class MessageVideo(
    override val text: String = "",
    var video: String = ""
) : IMessageContent

data class MessageVideoNote(
    override val text: String = "",
    var video: String = ""
) : IMessageContent

data class MessageVoiceNote(
    override val text: String = "",
    val voice: String = ""
) : IMessageContent

data class MessageUnknown(
    override val text: String = "",
    val info: String = ""
) : IMessageContent