package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject

data class Conversation(
    val id: Int = 0,
    val type: String = "",
    val date: Long = 0,
    val unread_count: Int = 0,
    val can_write: Boolean = true,
    var title: String = "",
    var photo: String = "",
    val last_message: String = "",
    val members_count: Int = 2,
    val last_online: Long = 0,
    val is_online: Boolean = false) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(type)
        parcel.writeLong(date)
        parcel.writeInt(unread_count)
        parcel.writeByte(if (can_write) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(photo)
        parcel.writeString(last_message)
        parcel.writeInt(members_count)
        parcel.writeLong(last_online)
        parcel.writeByte(if (is_online) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject, profiles: JSONArray?, groups: JSONArray?): Conversation {
            var conversation = json.optJSONObject("conversation")
            if (conversation == null) {
                conversation = json
            }
            val peer = conversation.getJSONObject("peer")
           // val limitMessage = 30

            val id = peer.optInt("id")

            val type = peer.optString("type")

            var unreadCount = conversation.optInt("unread_count")

            val canWrite = conversation.getJSONObject("can_write").getBoolean("allowed")

            var title = "User"
            var photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            var lastOnline: Long = 0
            var isOnline = false

            var membersCount = 2
            val chatSettings = conversation.optJSONObject("chat_settings")
            when {
                type == "chat" && chatSettings != null -> {
                    title = chatSettings.optString("title")
                    //title = fixText(chatSettings.optString("title"), limitMessage - 5)
                    val photoObject = chatSettings.optJSONObject("photo")
                    if (photoObject != null) {
                        photo = photoObject.optString("photo_100").toString()
                    }
                    membersCount = chatSettings.optInt("members_count")
                }
                type == "user" && profiles != null -> {
                    for (i in 0 until profiles.length()) {
                        val profile = profiles.getJSONObject(i)
                        if (profile.getInt("id") == id) {
                            title = profile.getString("first_name") + " " + profile.getString("last_name")
                            photo = profile.getString("photo_100")
                            lastOnline = profile.getJSONObject("online_info").optLong("last_seen")
                            isOnline = profile.getJSONObject("online_info").optBoolean("is_online")
                            break
                        }
                    }
                }
                type == "group" && groups != null -> {
                    for (i in 0 until groups.length()) {
                        val group = groups.getJSONObject(i)
                        if (group.getInt("id") == -id) {
                            title = group.getString("name")
                            //title = fixText(group.getString("name"), limitMessage - 5)
                            photo = group.getString("photo_100")
                            break
                        }
                    }
                }
            }

            val lastMessageObject = json.getJSONObject("last_message")
            val date = lastMessageObject.optLong("date")
            val out: Boolean = when (lastMessageObject.getInt("out")) {
                1 -> true
                else -> false
            }

            if (conversation.getInt("in_read_cmid") > conversation.getInt("out_read_cmid")) {
                unreadCount = -1
            }

            var lastMessage = (if (out) "Вы: " else "") + lastMessageObject.getString("text")
                //fixText((if (out) "Вы: " else "") + lastMessageObject.getString("text"), limitMessage)
            if (lastMessage == "" || lastMessage == "Вы: ") {
                val attachments = lastMessageObject.getJSONArray("attachments")
                val action = lastMessageObject.optJSONObject("action")
                when {
                    attachments.length() != 0 -> {
                        lastMessage = (if (out) "Вы: " else "") + when (attachments.optJSONObject(0)?.optString("type")) {
                            "sticker" -> "Стикер"
                            "photo" -> "Фото"
                            "wall" -> "Запись со стены"
                            "video" -> "Видеозапись"
                            "doc" -> "Файл"
                            "link" -> "Сслыка"
                            "audio" -> "Аудиозапись"
                            "market" -> "Товар"
                            "market_album" -> "Подборка товаров"
                            "wall_reply" -> "Комментарий на стене"
                            "gift" -> "Подарок"
                            else -> "Вложение"
                        }
                    }
                    action != null -> {
                        lastMessage = when (action.optString("type")) {
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
                    }
                    else -> {
                        lastMessage = "Пересланное сообщение"
                    }
                }
            }

            return Conversation(id, type, date, unreadCount, canWrite, title, photo, lastMessage, membersCount, lastOnline, isOnline)
        }

        private fun fixText(text: String, limit: Int): String {
            val newText = if (text.length > limit) text.substring(0, limit) + "..." else text
            return newText.replace('\n', ' ', true)
        }
    }

}
