package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.interfaces.IMessageContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

data class Message(
    var id: Long = 0,
    var timeStamp: Long = 0,
    val sender: ICompanion? = null,
    val isOutgoing: Boolean = false,
    val replyToMessageId: Long = 0,
    var content: IMessageContent = MessageText()
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
                    if (group.getLong("id") == json.getLong("from_id")) {
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

            val isOutgoing: Boolean = json.getInt("out") == 1
            val replyToMessageId: Long = json.optJSONObject("reply_message")?.getLong("id") ?: 0

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

            val attachmentsObject = json.getJSONArray("attachments")
            if (attachmentsObject.length() == 1) {
                val at = attachmentsObject.getJSONObject(0)
                when (at.getString("type")) {
                    "sticker" -> {
                        val sticker = at.getJSONObject("sticker")
                            .getJSONArray("images")
                            .getJSONObject(3)
                            .getString("url")
                        messageContent = MessageSticker(sticker)
                    }
                    "photo" -> {
                        val photo = at.getJSONObject("photo")
                            .getJSONArray("sizes")
                            .getJSONObject(4)
                            .getString("url")
                        messageContent = MessagePhoto(text, photo)
                    }
                    "video" -> {
                        val video = at.getJSONObject("video")
                            .getJSONArray("image")
                            .getJSONObject(5)
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
            }
            else if (attachmentsObject.length() > 1) {
                var items = arrayListOf<String>()
                for (i in 0 until attachmentsObject.length()) {
                    val at = attachmentsObject.getJSONObject(i)
                    when (at.getString("type")) {
                        "photo" -> {
                            items.add(at.getJSONObject("photo")
                                .getJSONArray("sizes")
                                .getJSONObject(4)
                                .getString("url")
                            )
                        }
                        "video" -> {
                            items.add(at.getJSONObject("video")
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

            return Message(id, timeStamp, sender, isOutgoing, replyToMessageId, messageContent)
        }

        suspend fun tgParse(tgMessage: TdApi.Message): Message {
            val client = App.application.tgClient
            val repository = App.application.tgRepository

            val id = tgMessage.id
            val timeStamp: Long = (tgMessage.date).toLong() * 1000

            val sender: ICompanion = when (tgMessage.senderId.constructor) {
                TdApi.MessageSenderUser.CONSTRUCTOR -> {
                    repository.getUser((tgMessage.senderId as TdApi.MessageSenderUser).userId).first()
                }
                else -> {
                    repository.getConversation((tgMessage.senderId as TdApi.MessageSenderChat).chatId).first().companion!!
                }
            }

            val isOutgoing = tgMessage.isOutgoing

            val replyToMessageId = tgMessage.replyToMessageId

            var messageContent: IMessageContent = MessageText()

            when (tgMessage.content.constructor) {
                TdApi.MessageText.CONSTRUCTOR -> {
                    messageContent = MessageText((tgMessage.content as TdApi.MessageText).text.text)
                }
                TdApi.MessageAnimation.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAnimation
                    client.downloadableFile(content.animation.animation!!).mapNotNull {
                        messageContent = MessageAnimation(path = it!!)
                    }
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
                    client.downloadableFile(content.photo.sizes[0].photo).mapNotNull {
                        messageContent = MessagePhoto(content.caption.text, it!!)
                    }
                }
                TdApi.MessageExpiredPhoto.CONSTRUCTOR -> {
                    messageContent = MessageExpiredPhoto()
                }
                TdApi.MessageSticker.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageSticker
                    if (content.sticker.isAnimated) {
                        messageContent = MessageText("Анимированный стикер: ${content.sticker.emoji}")
                    }
                    else {
                        client.downloadableFile(content.sticker.sticker).mapNotNull {
                            messageContent = MessageSticker(it!!)
                        }
                    }
                }
                TdApi.MessageVideo.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVideo
                    content.video.thumbnail?.let {
                        client.downloadableFile(it.file).mapNotNull { path ->
                            messageContent = MessagePhoto(content.caption.text, path!!)
                        }
                    }
                }
                TdApi.MessageExpiredVideo.CONSTRUCTOR -> {
                    messageContent = MessageExpiredVideo()
                }
                TdApi.MessageVideoNote.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVideoNote
                    content.videoNote.thumbnail?.let {
                        client.downloadableFile(it.file).mapNotNull { path ->
                            messageContent = MessagePhoto(path!!)
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

            return Message(id, timeStamp, sender, isOutgoing, replyToMessageId, messageContent)
        }
    }
}