package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.progcorp.unitedmessengers.data.db.telegram.TgUserRepository
import com.progcorp.unitedmessengers.util.ConvertTime
import kotlinx.coroutines.flow.first
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONArray
import org.json.JSONObject

data class Message(
    var id: Long = 0,
    val date: Long = 0,
    val time: String = "",
    val peerId: Long = 0,
    val fromId: Long = 0,
    val out: Boolean = false,
    val senderName: String = "",
    val senderPhoto: String = "",
    val action: String = "",
    val attachments: String = "",
    val sticker: String = "",
    val text: String = "",
    val type: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(date)
        parcel.writeString(time)
        parcel.writeLong(fromId)
        parcel.writeLong(peerId)
        parcel.writeByte(if (out) 1 else 0)
        parcel.writeString(senderName)
        parcel.writeString(senderPhoto)
        parcel.writeString(action)
        parcel.writeString(attachments)
        parcel.writeString(sticker)
        parcel.writeString(text)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        const val MESSAGE_OUT = 0
        const val STICKER_OUT = 1
        const val ATTACHMENT_OUT = 2
        const val CHAT_MESSAGE = 3
        const val CHAT_STICKER = 4
        const val CHAT_ATTACHMENT = 5
        const val CHAT_ACTION = 6
        const val DIALOG_MESSAGE = 10
        const val DIALOG_STICKER = 11
        const val DIALOG_ATTACHMENT = 12

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
            var photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
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
                type
            )
        }

        suspend fun tgParse(tgMessage: TdApi.Message): Message {
            val id = tgMessage.id
            val timeStamp: Long = (tgMessage.date * 1000).toLong()
            val time = ConvertTime.toTime(timeStamp)
            val peerId = tgMessage.chatId
            val sender: TdApi.User
            var fromId: Long = 0
            var fromName = ""
            when (tgMessage.senderId::class.simpleName) {
                "MessageSenderUser" -> {
                    val senderId = tgMessage.senderId as TdApi.MessageSenderUser
                    sender = TgUserRepository().getUser(senderId.userId).first()
                    fromId = sender.id
                    fromName = sender.firstName + " " + sender.lastName
                }
                else -> {
                    val senderId = tgMessage.senderId as TdApi.MessageSenderChat
                }
            }

            //val out: Boolean = (fromId == TgUserRepository().getUser(null).first().id)

            val out = false

            val photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            val action = ""
            val attachments = ""
            val sticker = ""
            var text = ""

            when (tgMessage.content::class.simpleName) {
                "MessageText" -> text = (tgMessage.content as TdApi.MessageText).text.text
                "MessagePhoto" -> text = "Photo"
                else -> text = ""
            }
            val type: Int = when {
                out -> {
                    MESSAGE_OUT
                }
                else -> {
                    CHAT_MESSAGE
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
                type
            )
        }
    }
}