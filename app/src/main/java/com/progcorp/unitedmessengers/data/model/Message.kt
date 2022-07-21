package com.progcorp.unitedmessengers.data.model

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.model.companions.Bot
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.interfaces.IMessageContent
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

data class Message(
    var id: Long = 0,
    var timeStamp: Long = 0,
    val sender: ICompanion? = null,
    val isOutgoing: Boolean = false,
    val replyToMessage: Message? = null,
    val forwardedMessages: List<Message>? = null,
    var content: IMessageContent = MessageText(),
    var messenger: Int = 0
) : Serializable {

    companion object {
        suspend fun vkParse(json: JSONObject, profiles: JSONArray?, groups: JSONArray?): Message {
            val id = json.optLong("id")
            val timeStamp = json.optLong("date") * 1000

            var sender: ICompanion? = null
            if (profiles != null) {
                for (i in 0 until profiles.length()) {
                    val profile = profiles.getJSONObject(i)
                    if (profile.getLong("id") == json.getLong("from_id")) {
                        sender = User.vkParse(profile)
                        break
                    }
                }
            }
            if (sender == null && groups != null) {
                for (i in 0 until groups.length()) {
                    val group = groups.getJSONObject(i)
                    if (group.getLong("id") == -json.getLong("from_id")) {
                        sender = Bot.vkParse(group)
                        break
                    }
                }
            }
            if (sender == null) {
                sender = json.optJSONObject("chat_settings")?.let {
                    Chat.vkParse(it, id)
                }
            }

            val isOutgoing: Boolean = json.optInt("out") == 1
            val replyToMessage: Message? =
                json.optJSONObject("reply_message")?.let { vkParse(it, profiles, groups) }

            val text = json.getString("text")
            var messageContent: IMessageContent = MessageText(text)

            if (sender is Chat) {
                val actionObject = json.optJSONObject("action")
                if (actionObject != null) {
                    val action = when (actionObject.getString("type")) {
                        "chat_photo_update" -> "Обновлена фотография беседы"
                        "chat_photo_remove " -> "Удалена фотография беседы"
                        "chat_create" -> "Создана беседа"
                        "chat_title_update" -> "Обновлено название беседы"
                        "chat_invite_user" -> "Приглашён пользователь"
                        "chat_kick_user" -> "Исключён пользователь"
                        "chat_pin_message" -> "Закреплено сообщение"
                        "chat_unpin_message" -> "Откреплено сообщение"
                        "chat_invite_user_by_link" -> "Пользователь присоединился"
                        else -> "Действие в беседе"
                    }
                    messageContent = MessageChat(action)
                }
            }

            try {
                val attachmentsObject = json.getJSONArray("attachments")
                if (attachmentsObject.length() == 1) {
                    val at = attachmentsObject.getJSONObject(0)
                    when (at.getString("type")) {
                        "sticker" -> {
                            val stickers = at.getJSONObject("sticker")
                                .getJSONArray("images")
                            val sticker: String = if (stickers.length() == 5) {
                                stickers
                                    .getJSONObject(4)
                                    .getString("url")
                            } else {
                                stickers
                                    .getJSONObject(1)
                                    .getString("url")
                            }
                            messageContent = MessageSticker(path = sticker)
                        }
                        "photo" -> {
                            val photoArray = at.getJSONObject("photo").getJSONArray("sizes")
                            for (i in 0 until photoArray.length()) {
                                val photo = photoArray.getJSONObject(i)
                                if (photo.getString("type") == "y" || photo.getString("type") == "x") {
                                    val photoObj = Photo(
                                        id = at.getJSONObject("photo").getLong("id"),
                                        width = photo.getInt("width"),
                                        height = photo.getInt("height"),
                                        path = photo.getString("url")
                                    )
                                    photoObj.adaptToChatSize()
                                    messageContent = MessagePhoto(text, photoObj)
                                    break
                                }
                            }
                        }
                        "video" -> {
                            val video = at.getJSONObject("video")
                                .getJSONArray("image")
                                .getJSONObject(3)
                                .getString("url")
                            messageContent = MessageVideo(text, video)
                        }
                        "audio" -> {
                            val audioObj = at.getJSONObject("audio")
                            val name = audioObj.getString("artist") +
                                    " " + audioObj.getString("title")
                            messageContent = MessageUnknown(text, name)
                        }
                        "audio_message" -> {
                            val audio = at.getJSONObject("audio_message")
                                .getString("link_mp3")
                            messageContent = MessageVoiceNote(text, audio)

                        }
                        "doc" -> {
                            val doc = at.getJSONObject("doc")
                                .getString("title")
                            messageContent = MessageDocument(text, doc)
                        }
                        "link" -> {}
                        "market" -> {
                            messageContent = MessageUnknown(text, "Товар")
                        }
                        "market_album" -> {
                            messageContent = MessageUnknown(text, "Подборка товаров")
                        }
                        "wall" -> {
                            messageContent = MessageUnknown(text, "Запись со стены")
                        }
                        "wall_reply" -> {
                            messageContent = MessageUnknown(text, "Комментарий к записи")
                        }
                        "gift" -> {
                            messageContent = MessageUnknown(text, "Подарок")
                        }
                    }
                } else if (attachmentsObject.length() > 1) {
                    var items = arrayListOf<String>()
                    for (i in 0 until attachmentsObject.length()) {
                        val at = attachmentsObject.getJSONObject(i)
                        when (at.getString("type")) {
                            "photo" -> {
                                items.add(
                                    at.getJSONObject("photo")
                                        .getJSONArray("sizes")
                                        .getJSONObject(4)
                                        .getString("url")
                                )
                            }
                            "video" -> {
                                items.add(
                                    at.getJSONObject("video")
                                        .getJSONArray("image")
                                        .getJSONObject(5)
                                        .getString("url")
                                )
                            }
                            else -> {
                                items = arrayListOf()
                                break
                            }
                        }
                    }
                    messageContent = if (items.size > 0) {
                        MessageCollage(text, items)
                    } else {
                        MessageUnknown(text, "Вложения")
                    }
                }
            } catch (ex: JSONException) {
                Log.e("Message.vkParse", ex.stackTraceToString())
                messageContent = MessageUnknown("Необработанное сообщение")
            }

            return Message(
                id = id,
                timeStamp = timeStamp,
                sender = sender,
                isOutgoing = isOutgoing,
                replyToMessage = replyToMessage,
                forwardedMessages = null,
                content = messageContent,
                messenger = Constants.Messenger.VK
            )
        }

        suspend fun tgParse(tgMessage: TdApi.Message): Message {
            val repository = App.application.tgClient.repository

            val id = tgMessage.id
            val timeStamp: Long = (tgMessage.date).toLong() * 1000

            val sender: ICompanion = when (tgMessage.senderId.constructor) {
                TdApi.MessageSenderUser.CONSTRUCTOR -> {
                    User.tgParse(repository.getUser((tgMessage.senderId as TdApi.MessageSenderUser).userId).first())
                }
                else -> {
                    val conversation = repository.getConversation((tgMessage.senderId as TdApi.MessageSenderChat).chatId).first()
                    when(conversation.type.constructor) {
                        TdApi.ChatTypeBasicGroup.CONSTRUCTOR -> {
                            Chat.tgParseBasicGroup(conversation, repository.getBasicGroup((conversation.type as TdApi.ChatTypeBasicGroup).basicGroupId).first())
                        }
                        TdApi.ChatTypeSupergroup.CONSTRUCTOR -> {
                            Chat.tgParseSupergroup(conversation, repository.getSupergroup((conversation.type as TdApi.ChatTypeSupergroup).supergroupId).first())
                        }
                        else -> {
                            Chat()
                        }
                    }
                }
            }

            val isOutgoing = tgMessage.isOutgoing

            val replyToMessage: Message? = if (tgMessage.replyToMessageId != 0.toLong()) {
                repository.getMessage(tgMessage.chatId, tgMessage.replyToMessageId).first()
                    ?.let { tgParse(it) }
            } else {
                null
            }


            var messageContent: IMessageContent = MessageText()

            when (tgMessage.content.constructor) {
                TdApi.MessageText.CONSTRUCTOR -> {
                    messageContent = MessageText((tgMessage.content as TdApi.MessageText).text.text)
                }
                TdApi.MessageAnimation.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAnimation
                    val photo = content.animation.animation!!
                    messageContent = MessageAnimation()
                }
                TdApi.MessageAudio.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAudio
                    messageContent = MessageUnknown(content.caption.text, content.audio.fileName)
                }
                TdApi.MessageDocument.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageDocument
                    messageContent = MessageUnknown(content.caption.text, content.document.fileName)
                }
                TdApi.MessagePhoto.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessagePhoto
                    val path: String
                    val photoId: Int
                    val photoWidth: Int
                    val photoHeight: Int
                    content.photo.sizes[content.photo.sizes.size - 1].let {
                        photoId = it.photo.id
                        photoHeight = it.height
                        photoWidth = it.width
                        path = if (it.photo.local.isDownloadingCompleted){
                            it.photo.local.path
                        } else {
                            it.photo.id.toString()
                        }
                    }
                    val photoObj = Photo(photoId.toLong(), photoWidth, photoHeight, path)
                    photoObj.adaptToChatSize()
                    messageContent = MessagePhoto(content.caption.text, photoObj)
                }
                TdApi.MessageExpiredPhoto.CONSTRUCTOR -> {
                    messageContent = MessageExpiredPhoto()
                }
                TdApi.MessageSticker.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageSticker
                    messageContent = if (content.sticker.isAnimated) {
                        MessageText(text = content.sticker.emoji)
                    } else {
                        if (content.sticker.sticker.local.isDownloadingCompleted) {
                            MessageSticker(path = content.sticker.sticker.local.path)
                        }
                        else {
                            MessageSticker(path = content.sticker.sticker.id.toString())
                        }
                    }
                }
                TdApi.MessageVideo.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVideo
                    content.video.thumbnail?.let {
                        val photoObj = Photo(
                            it.file.id.toLong(),
                            it.width,
                            it.height
                        )
                        photoObj.adaptToChatSize()
                        messageContent = if (it.file.local.isDownloadingCompleted) {
                            photoObj.path = it.file.local.path
                            MessagePhoto(
                                text = content.caption.text,
                                photo = photoObj
                            )
                        }
                        else {
                            photoObj.path = it.file.id.toString()
                            MessagePhoto(
                                text = content.caption.text,
                                photo = photoObj
                            )
                        }
                    }
                }
                TdApi.MessageExpiredVideo.CONSTRUCTOR -> {
                    messageContent = MessageExpiredVideo()
                }
                TdApi.MessageVideoNote.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVideoNote
                    content.videoNote.thumbnail?.let {
                        val photoObj = Photo(
                            it.file.id.toLong(),
                            it.width,
                            it.height
                        )
                        photoObj.adaptToChatSize()
                        messageContent = if (it.file.local.isDownloadingCompleted) {
                            photoObj.path = it.file.local.path
                            MessagePhoto(
                                photo = photoObj
                            )
                        }
                        else {
                            photoObj.path = it.file.id.toString()
                            MessagePhoto(
                                photo = photoObj
                            )
                        }
                    }
                }
                TdApi.MessageVoiceNote.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVoiceNote
                    messageContent = MessageVoiceNote(content.caption.text)
                }
                TdApi.MessageLocation.CONSTRUCTOR -> {
                    messageContent = MessageLocation()
                }
                TdApi.MessageVenue.CONSTRUCTOR -> {
                    messageContent = MessageLocation()
                }
                TdApi.MessageContact.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Контакт")
                }
                TdApi.MessageAnimatedEmoji.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAnimatedEmoji
                    messageContent = MessageText(content.emoji)
                }
                TdApi.MessageDice.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageDice
                    messageContent = MessageUnknown(content.emoji, content.value.toString())
                }
                TdApi.MessageGame.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Игра")
                }
                TdApi.MessagePoll.CONSTRUCTOR -> {
                    messageContent = MessagePoll()
                }
                TdApi.MessageInvoice.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Счёт")
                }
                TdApi.MessageCall.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Звонок")
                }
                TdApi.MessageVideoChatScheduled.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Запланированный видеозвонок")
                }
                TdApi.MessageVideoChatStarted.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Видеозвонок начат")
                }
                TdApi.MessageVideoChatEnded.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Видеозвонок окончен")
                }
                TdApi.MessageInviteVideoChatParticipants.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "Приглашение в видеозвонок")
                }
                TdApi.MessageChatChangeTitle.CONSTRUCTOR -> {
                    messageContent = MessageChat("Чат сменил название")
                }
                TdApi.MessageChatChangePhoto.CONSTRUCTOR -> {
                    messageContent = MessageChat("Чат сменил фото")
                }
                TdApi.MessageChatDeletePhoto.CONSTRUCTOR -> {
                    messageContent = MessageChat("Чат удалил фото")
                }
                TdApi.MessageChatAddMembers.CONSTRUCTOR -> {
                    messageContent = MessageChat("Новый участник")
                }
                TdApi.MessageChatJoinByLink.CONSTRUCTOR -> {
                    messageContent = MessageChat("Новый участник присоеденился по ссылке")
                }
                TdApi.MessageChatJoinByRequest.CONSTRUCTOR -> {
                    messageContent = MessageChat("Новый участник")
                }
                TdApi.MessageChatDeleteMember.CONSTRUCTOR -> {
                    messageContent = MessageChat("Участник покинул чат")
                }
                TdApi.MessageChatUpgradeTo.CONSTRUCTOR -> {
                    messageContent = MessageChat("Группа стала супергруппой (?)")
                }
                TdApi.MessageChatUpgradeFrom.CONSTRUCTOR -> {
                    messageContent = MessageChat("Группа стала супергруппой (?)")
                }
                TdApi.MessagePinMessage.CONSTRUCTOR -> {
                    messageContent = MessageChat("Закреплено сообщение")
                }
                TdApi.MessageScreenshotTaken.CONSTRUCTOR -> {
                    messageContent = MessageChat("Был сделан скриншот чата")
                }
                TdApi.MessageChatSetTheme.CONSTRUCTOR -> {
                    messageContent = MessageChat("Изменена тема чата")
                }
                TdApi.MessageChatSetTtl.CONSTRUCTOR -> {
                    messageContent = MessageChat("Время жизни сообщений изменено")
                }
                TdApi.MessageCustomServiceAction.CONSTRUCTOR -> {
                    messageContent = MessageChat("Что-то произошло")
                }
                TdApi.MessageGameScore.CONSTRUCTOR -> {
                    messageContent = MessageChat("В игре побит рекорд")
                }
                TdApi.MessagePaymentSuccessful.CONSTRUCTOR -> {
                    messageContent = MessageChat("Успешная оплата")
                }
                TdApi.MessagePaymentSuccessfulBot.CONSTRUCTOR -> {
                    messageContent = MessageChat("Успешная оплата")
                }
                TdApi.MessageContactRegistered.CONSTRUCTOR -> {
                    messageContent = MessageChat("Присоединился к Telegram")
                }
                else -> {
                    messageContent = MessageUnknown(info = "Какое-то действие")
                }
            }

            return Message(
                id = id,
                timeStamp = timeStamp,
                sender = sender,
                isOutgoing = isOutgoing,
                replyToMessage = replyToMessage,
                forwardedMessages = null,
                content = messageContent,
                messenger = Constants.Messenger.TG
            )
        }
    }
}