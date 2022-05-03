package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateUtils
import com.progcorp.unitedmessengers.data.db.telegram.TgConversationsRepository
import com.progcorp.unitedmessengers.data.db.telegram.TgMessagesRepository
import com.progcorp.unitedmessengers.data.db.telegram.TgUserRepository
import kotlinx.coroutines.flow.first
import org.drinkless.td.libcore.telegram.TdApi.*
import org.drinkless.td.libcore.telegram.TdApi.Date
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Math.round
import java.util.*

data class Conversation(
    val id: Long = 0,
    val type: String = "",
    val date: Long = 0,
    val unread_count: Int = 0,
    val can_write: Boolean = true,
    var title: String = "",
    var photo: String = "",
    val last_message: String = "",
    val members_count: Int = 2,
    val last_online: Long = 0,
    val is_online: Boolean = false,
    val from: String = "") : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
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
        parcel.writeLong(id)
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

        fun vkParse(json: JSONObject, profiles: JSONArray?, groups: JSONArray?): Conversation {
            var conversation = json.optJSONObject("conversation")
            if (conversation == null) {
                conversation = json
            }
            val peer = conversation.getJSONObject("peer")
            val id = peer.optLong("id")

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
                    val photoObject = chatSettings.optJSONObject("photo")
                    if (photoObject != null) {
                        photo = photoObject.optString("photo_100").toString()
                    }
                    membersCount = chatSettings.optInt("members_count")
                }
                type == "user" && profiles != null -> {
                    for (i in 0 until profiles.length()) {
                        val profile = profiles.getJSONObject(i)
                        if (profile.getLong("id") == id) {
                            title = profile.getString("first_name") + " " + profile.getString("last_name")
                            photo = profile.getString("photo_100")
                            lastOnline = profile.getJSONObject("online_info").optLong("last_seen") * 1000
                            isOnline = profile.getJSONObject("online_info").optBoolean("is_online")
                            break
                        }
                    }
                }
                type == "group" && groups != null -> {
                    for (i in 0 until groups.length()) {
                        val group = groups.getJSONObject(i)
                        if (group.getLong("id") == -id) {
                            title = group.getString("name")
                            photo = group.getString("photo_100")
                            break
                        }
                    }
                }
            }

            val lastMessageObject = json.getJSONObject("last_message")
            val date = lastMessageObject.optLong("date") * 1000
            val out: Boolean = when (lastMessageObject.getInt("out")) {
                1 -> true
                else -> false
            }

            if (conversation.getInt("in_read_cmid") > conversation.getInt("out_read_cmid")) {
                unreadCount = -1
            }

            var lastMessage = (if (out) "Вы: " else "") + lastMessageObject.getString("text")
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

            return Conversation(id, type, date, unreadCount, canWrite, title, photo, lastMessage, membersCount, lastOnline, isOnline, "vk")
        }

        suspend fun tgParse(conversation: Chat): Conversation {
            val id = conversation.id
            val type = when(conversation.type.constructor) {
                ChatTypePrivate.CONSTRUCTOR -> "user"
                ChatTypeBasicGroup.CONSTRUCTOR -> "basicgroup"
                ChatTypeSupergroup.CONSTRUCTOR -> "supergroup"
                ChatTypeSecret.CONSTRUCTOR -> "secret"
                else -> "group"
            }

            val extraId: Long = when (type) {
                "supergroup" -> (conversation.type as ChatTypeSupergroup).supergroupId
                "basicgroup" -> (conversation.type as ChatTypeBasicGroup).basicGroupId
                else -> 0
            }

            val date: Long =
            if (conversation.lastMessage != null) conversation.lastMessage!!.date.toLong() * 1000
            else 0

            val unreadCount = conversation.unreadCount
            val canWrite = conversation.permissions.canSendMessages
            val title = conversation.title
            val photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            var lastMessage = ""

            if (conversation.lastMessage != null) {
                val tgMessage = TgMessagesRepository().getMessage(id, conversation.lastMessage!!.id).first()
                val message = Message.tgParse(tgMessage)
                lastMessage = message.text
            }

            val membersCount: Int = when (type) {
                "supergroup" -> {
                    val group = TgConversationsRepository().getSupergroup(extraId).first()
                    group.memberCount
                }
                "basicgroup" -> {
                    val group = TgConversationsRepository().getBasicGroup(extraId).first()
                     group.memberCount
                }
                else -> 2
            }

            var lastOnline: Long = 0
            var isOnline = false
            if (type == "user") {
                val cal: Calendar = Calendar.getInstance()
                val user = TgUserRepository().getUser(id).first()
                when (user.status.constructor) {
                    UserStatusEmpty.CONSTRUCTOR -> {
                        lastOnline = 0
                    }
                    UserStatusLastMonth.CONSTRUCTOR -> {
                        lastOnline = 1
                    }
                    UserStatusLastWeek.CONSTRUCTOR -> {
                        cal.add(Calendar.DAY_OF_YEAR, -7)
                        lastOnline = 2
                    }
                    UserStatusOffline.CONSTRUCTOR -> {
                        lastOnline = ((user.status as UserStatusOffline).wasOnline * 1000).toLong()
                    }
                    UserStatusOnline.CONSTRUCTOR -> {
                        lastOnline = 0
                        isOnline = true
                    }
                    UserStatusRecently.CONSTRUCTOR -> {
                        lastOnline = 3
                    }
                }
                //TODO: Создать класс с типами скрытых онлайнов
            }


            return Conversation(id, type, date, unreadCount, canWrite, title, photo, lastMessage, membersCount, lastOnline, isOnline, "tg")
        }
    }

}
