package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import com.progcorp.unitedmessengers.util.ConvertTime
import kotlinx.coroutines.flow.first
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONArray
import org.json.JSONObject

data class Message(
    var id: Long = 0,
    val timeStamp: Long = 0,
    val sender: User? = null,
    val isOutgoing: Boolean = false,
    val content: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readTypedObject(User.CREATOR)!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(timeStamp)
        parcel.writeParcelable(sender, flags)
        parcel.writeByte(if (isOutgoing) 1 else 0)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {

        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }

        fun vkParse(json: JSONObject, profiles: JSONArray?): Message {
            val id = json.optLong("id")
            val timeStamp = json.optLong("date") * 1000
            val time = ConvertTime.toTime(timeStamp)
            val fromId = json.optLong("from_id")
            val peerId = json.optLong("peer_id")
            val out: Boolean = when (json.getInt("out")) {
                1 -> true
                else -> false
            }
            var text = json.getString("text")

            val isDialog = fromId == peerId

            var fromName = "User"
            var photo = ""
            var action = "message"
            var attachments = ""
            var sticker = ""
            var type = 0

            if (profiles != null) {
                for (i in 0 until profiles.length()) {
                    val profile = profiles.getJSONObject(i)
                    if (profile.getLong("id") == fromId) {
                        fromName =
                            profile.getString("first_name") + " " + profile.getString("last_name")
                        photo = profile.getString("photo_100")
                        break
                    }
                }
            }

            if (!isDialog) {
                val actionObject = json.optJSONObject("action")
                if (actionObject != null) {
                    action = actionObject.getString("type")
                    text = when (action) {
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
                    type = CHAT_ACTION
                }
            }

            if (type != CHAT_ACTION) {
                val attachmentsObject = json.getJSONArray("attachments")
                if (attachmentsObject.length() != 0) {
                    for (i in 0 until attachmentsObject.length()) {
                        val at = attachmentsObject.getJSONObject(i)
                        when (at.getString("type")) {
                            "sticker" -> {
                                sticker = at.getJSONObject("sticker")
                                    .getJSONArray("images")
                                    .getJSONObject(3)
                                    .getString("url")
                                break
                            }
                            "photo" -> {
                                attachments = "Фото"
                            }
                            "video" -> {
                                attachments = "Видео"
                            }
                            "audio" -> {
                                attachments = "Аудиозапись"
                            }
                            "audio_message" -> {
                                attachments = "Голосовое сообщение"
                                break
                            }
                            "doc" -> {
                                attachments = "Документ"
                            }
                            "link" -> {
                                continue
                            }
                            "market" -> {
                                attachments = "Товар"
                                break
                            }
                            "market_album" -> {
                                attachments = "Подборка товаров"
                                break
                            }
                            "wall" -> {
                                attachments = "Запись со стены"
                                break
                            }
                            "wall_reply" -> {
                                attachments = "Комментарий к записи"
                                break
                            }
                            "gift" -> {
                                attachments = "Подарок"
                                break
                            }
                        }
                    }
                }

                if (attachments == "") {
                    if (json.getJSONArray("fwd_messages").length() != 0) {
                        attachments = "Пересланное сообщение"
                    }
                    else if (json.optJSONObject("reply_message") != null) {
                        attachments = "Ответ на сообщение"
                    }
                }

                when {
                    sticker != "" -> {
                        type = when {
                            out -> {
                                STICKER_OUT
                            }
                            isDialog -> {
                                DIALOG_STICKER
                            }
                            else -> {
                                CHAT_STICKER
                            }
                        }
                    }

                    attachments != "" -> {
                        type = when {
                            out -> {
                                ATTACHMENT_OUT
                            }
                            isDialog -> {
                                DIALOG_ATTACHMENT
                            }
                            else -> {
                                CHAT_ATTACHMENT
                            }
                        }
                    }

                    else -> {
                        type = when {
                            out -> {
                                MESSAGE_OUT
                            }
                            isDialog -> {
                                DIALOG_MESSAGE
                            }
                            else -> {
                                CHAT_MESSAGE
                            }
                        }
                    }
                }
            }

            return Message(
                id,
                timeStamp,
                time,
                fromId,
                peerId,
                out,
                fromName,
                photo,
                action,
                attachments,
                sticker,
                text,
                type,
                "vk"
            )
        }

        suspend fun tgParse(tgMessage: TdApi.Message, tgConversation: TdApi.Chat): Message {
            val id = tgMessage.id
            val timeStamp: Long = (tgMessage.date).toLong() * 1000
            val time = ConvertTime.toTime(timeStamp)
            val peerId = tgMessage.chatId
            val fromId: Long
            val messageSender: TdApi.MessageSender
            val fromName: String
            when (tgMessage.senderId::class.simpleName) {
                "MessageSenderUser" -> {
                    messageSender = tgMessage.senderId as TdApi.MessageSenderUser
                    val sender = TgUserRepository().getUser(messageSender.userId).first()
                    fromId = sender.id
                    fromName = sender.firstName + " " + sender.lastName
                }
                else -> {
                    messageSender = tgMessage.senderId as TdApi.MessageSenderChat
                    val sender = TgConversationsRepository().getChat(messageSender.chatId).first()
                    fromId = sender.id
                    fromName = sender.title
                }
            }
            val isDialog = when(tgConversation.type.constructor) {
                TdApi.ChatTypePrivate.CONSTRUCTOR -> true
                TdApi.ChatTypeSecret.CONSTRUCTOR -> true
                else -> false
            }

            val out = tgMessage.isOutgoing

            val photo = ""
            val action = ""
            var attachments = ""
            val sticker = ""
            var text = ""
            var type = 0
            when (tgMessage.content::class.simpleName) {
                "MessageText" -> text = (tgMessage.content as TdApi.MessageText).text.text
                "MessageAnimation" -> attachments = "GIF"
                "MessageAudio" -> {
                    text = (tgMessage.content as TdApi.MessageAudio).caption.text
                    attachments = "Аудиозапись"
                }
                "MessageDocument" -> {
                    text = (tgMessage.content as TdApi.MessageDocument).caption.text
                    attachments = "Документ"
                }
                "MessagePhoto" -> {
                    text = (tgMessage.content as TdApi.MessagePhoto).caption.text
                    attachments = "Фото"
                }
                "MessageExpiredPhoto" -> attachments = "Удалённое фото"
                "MessageSticker" -> {
                    attachments = if (!(tgMessage.content as TdApi.MessageSticker).sticker.isAnimated) {
                        "Стикер"
                    } else {
                        "Анимированный стикер"
                    }
                }
                "MessageVideo" -> {
                    text = (tgMessage.content as TdApi.MessageVideo).caption.text
                    attachments = "Видео"
                }
                "MessageExpiredVideo" -> attachments = "Удалённое видео"
                "MessageVideoNote" -> attachments = "Видео-сообщение"
                "MessageVoiceNote" -> attachments = "Голосовое сообщение"
                "MessageLocation" -> attachments = "Место на карте"
                "MessageVenue" -> attachments = "Место встречи"
                "MessageContact" -> attachments = "Контакт"
                "MessageAnimatedEmoji" -> text = (tgMessage.content as TdApi.MessageAnimatedEmoji).emoji
                "MessageDice" -> attachments = "Кости"
                "MessageGame" -> attachments = "Игра"
                "MessagePoll" -> attachments = "Голосование"
                "MessageInvoice" -> attachments = "Счёт"
                "MessageCall" -> attachments = "Звонок"
                "MessageVideoChatScheduled" -> attachments = "Запланированный видеозвонок"
                "MessageVideoChatStarted" -> attachments = "Видеозвонок начат"
                "MessageVideoChatEnded" -> attachments = "Видеозвонок окончен"
                "MessageInviteVideoChatParticipants" -> attachments = "Приглашение в видеозвонок"
                "MessageChatChangeTitle" -> {
                    text = "Чат сменил название"
                    type = CHAT_ACTION
                }
                "MessageChatChangePhoto" -> {
                    text = "Чат сменил фото"
                    type = CHAT_ACTION
                }
                "MessageChatDeletePhoto" -> {
                    text = "Чат удалил фото"
                    type = CHAT_ACTION
                }
                "MessageChatAddMembers" -> {
                    text = "Новый участник"
                    type = CHAT_ACTION
                }
                "MessageChatJoinByLink" -> {
                    text = "Новый участник присоеденился по ссылке"
                    type = CHAT_ACTION
                }
                "MessageChatJoinByRequest" -> {
                    text = "Новый участник"
                    type = CHAT_ACTION
                }
                "MessageChatDeleteMember" -> {
                    text = "Участник покинул чат"
                    type = CHAT_ACTION
                }
                "MessageChatUpgradeTo" -> {
                    text = "Группа стала супергруппой (?)"
                    type = CHAT_ACTION
                }
                "MessageChatUpgradeFrom" -> {
                    text = "Группа стала супергруппой (?)"
                    type = CHAT_ACTION
                }
                "MessagePinMessage" -> {
                    text = "Закреплено сообщение"
                    type = CHAT_ACTION
                }
                "MessageScreenshotTaken" -> {
                    text = "Был сделан скриншот чата"
                    type = CHAT_ACTION
                }
                "MessageChatSetTheme" -> {
                    text = "Изменена тема чата"
                    type = CHAT_ACTION
                }
                "MessageChatSetTtl" -> {
                    text = "Время жизни сообщений изменено"
                    type = CHAT_ACTION
                }
                "MessageCustomServiceAction" -> {
                    text = "Что-то произошло"
                    type = CHAT_ACTION
                }
                "MessageGameScore" -> {
                    text = "В игре побит рекорд"
                    type = CHAT_ACTION
                }
                "MessagePaymentSuccessful" -> {
                    text = "Успешная оплата"
                    type = CHAT_ACTION
                }
                "MessagePaymentSuccessfulBot" -> {
                    text = "Успешная оплата"
                    type = CHAT_ACTION
                }
                "MessageContactRegistered" -> {
                    text = "Присоединился к Telegram"
                    type = CHAT_ACTION
                }
                else -> attachments = "Какое-то действие"
            }
            if (type != CHAT_ACTION) {
                when {
                    attachments != "" -> {
                        type = when {
                            out -> {
                                ATTACHMENT_OUT
                            }
                            isDialog -> {
                                DIALOG_ATTACHMENT
                            }
                            else -> {
                                CHAT_ATTACHMENT
                            }
                        }
                    }

                    else -> {
                        type = when {
                            out -> {
                                MESSAGE_OUT
                            }
                            isDialog -> {
                                DIALOG_MESSAGE
                            }
                            else -> {
                                CHAT_MESSAGE
                            }
                        }
                    }
                }
            }

            return Message(
                id,
                timeStamp,
                time,
                fromId,
                peerId,
                out,
                fromName,
                photo,
                action,
                attachments,
                sticker,
                text,
                type,
                "tg"
            )
        }
    }
}