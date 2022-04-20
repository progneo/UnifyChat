package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import com.progcorp.unitedmessengers.util.ConvertTime
import org.json.JSONArray
import org.json.JSONObject

data class Message(
    val id: Int = 0,
    val date: Long = 0,
    val time: String = "",
    val fromId: Int = 0,
    val peerId: Int = 0,
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
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
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
        parcel.writeInt(id)
        parcel.writeLong(date)
        parcel.writeString(time)
        parcel.writeInt(fromId)
        parcel.writeInt(peerId)
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
        const val CHAT_MESSAGE_OUT = 0
        const val CHAT_STICKER_OUT = 1
        const val CHAT_ATTACHMENT_OUT = 2
        const val CHAT_MESSAGE = 3
        const val CHAT_STICKER = 4
        const val CHAT_ATTACHMENT = 5
        const val CHAT_ACTION = 6
        const val DIALOG_MESSAGE_OUT = 7
        const val DIALOG_STICKER_OUT = 8
        const val DIALOG_ATTACHMENT_OUT = 9
        const val DIALOG_MESSAGE = 10
        const val DIALOG_STICKER = 11
        const val DIALOG_ATTACHMENT = 12

        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }

        fun parseVK(json: JSONObject, profiles: JSONArray?): Message {
            val id = json.optInt("id")
            val timeStamp = json.optLong("date")
            val time = ConvertTime.toDateTime(timeStamp)
            val fromId = json.optInt("from_id")
            val peerId = json.optInt("peer_id")
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
                    if (profile.getInt("id") == fromId) {
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
                                if (isDialog) {
                                    DIALOG_STICKER_OUT
                                }
                                else {
                                    CHAT_STICKER_OUT
                                }
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
                                if (isDialog) {
                                    DIALOG_ATTACHMENT_OUT
                                }
                                else {
                                    CHAT_ATTACHMENT_OUT
                                }
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
                                if (isDialog) {
                                    DIALOG_MESSAGE_OUT
                                }
                                else {
                                    CHAT_MESSAGE_OUT
                                }
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
    }
}