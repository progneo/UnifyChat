package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.interfaces.IMessageContent

data class MessageText(
    val text: String = ""
) : IMessageContent

data class MessageSticker(
    val path: String = ""
) : IMessageContent

data class MessageChat(
    val text: String = ""
) : IMessageContent

data class MessageAnimatedEmoji(
    val sticker: MessageSticker = MessageSticker(),
    val emoji: String = ""
) : IMessageContent

data class MessageAnimation(
    val text: String = "",
    val path: String = ""
) : IMessageContent

data class MessageCollage(
    val text: String = "",
    val paths: ArrayList<String> = arrayListOf()
) : IMessageContent

data class MessageDocument(
    val text: String = "",
    val file: String = ""
) : IMessageContent
//TODO: File class

data class MessageDocuments(
    val text: String = "",
    val files: ArrayList<String> = arrayListOf()
) : IMessageContent

data class MessageExpiredPhoto(
    val text: String = "Удалённое фото"
) : IMessageContent

data class MessageExpiredVideo(
    val text: String = "Удалённое видео"
) : IMessageContent

data class MessageLocation(
    val location: String = "Локация"
) : IMessageContent

data class MessagePhoto(
    val text: String = "",
    val path: String = ""
) : IMessageContent

data class MessagePoll(
    val poll: String = ""
) : IMessageContent

data class MessageVideo(
    val text: String = "",
    val video: String = ""
) : IMessageContent

data class MessageVideoNote(
    val video: String = ""
) : IMessageContent

data class MessageVoiceNote(
    val text: String = "",
    val voice: String = ""
) : IMessageContent

data class MessageUnknown(
    val text: String = "",
    val info: String = ""
) : IMessageContent