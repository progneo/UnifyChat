package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.interfaces.IMessageContent

data class MessageText(
    override var text: String = ""
) : IMessageContent

data class MessageSticker(
    override var text: String = "",
    var path: String = ""
) : IMessageContent

data class MessageChat(
    override var text: String = ""
) : IMessageContent

data class MessageAnimatedEmoji(
    override var text: String = "",
    val sticker: MessageSticker = MessageSticker(),
    val emoji: String = ""
) : IMessageContent

data class MessageAnimation(
    override var text: String = "",
    var path: String = ""
) : IMessageContent

data class MessageCollage(
    override var text: String = "",
    val paths: ArrayList<String> = arrayListOf()
) : IMessageContent

data class MessageDocument(
    override var text: String = "",
    val file: String = ""
) : IMessageContent
//TODO: File class

data class MessageDocuments(
    override var text: String = "",
    val files: ArrayList<String> = arrayListOf()
) : IMessageContent

data class MessageExpiredPhoto(
    override var text: String = "Удалённое фото"
) : IMessageContent

data class MessageExpiredVideo(
    override var text: String = "Удалённое видео"
) : IMessageContent

data class MessageLocation(
    override var text: String = "",
    val location: String = "Локация"
) : IMessageContent

data class MessagePhoto(
    override var text: String = "",
    var photo: Photo = Photo()
) : IMessageContent

data class MessagePoll(
    override var text: String = "",
    val poll: String = ""
) : IMessageContent

data class MessageVideo(
    override var text: String = "",
    var video: String = ""
) : IMessageContent

data class MessageVideoNote(
    override var text: String = "",
    var video: String = ""
) : IMessageContent

data class MessageVoiceNote(
    override var text: String = "",
    val voice: String = ""
) : IMessageContent

data class MessageUnknown(
    override var text: String = "",
    val info: String = ""
) : IMessageContent