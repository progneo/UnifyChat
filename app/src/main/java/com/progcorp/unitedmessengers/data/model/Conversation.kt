package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.flow.first
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONArray
import org.json.JSONObject

data class Conversation(
    val id: Long = 0,
    val companion: ICompanion? = null,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val canWrite: Boolean = true,
    val messenger: Int = 0
) {
    companion object {

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
            var photo = ""
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

            return Conversation()
        }

        suspend fun tgParse(conversation: TdApi.Chat): Conversation? {
            if (conversation.positions.isEmpty()) {
                return null
            }
            val id = conversation.id
            var userId: Long = 0
            val type = when(conversation.type.constructor) {
                TdApi.ChatTypePrivate.CONSTRUCTOR -> {
                    userId = (conversation.type as TdApi.ChatTypePrivate).userId
                    "user"
                }
                TdApi.ChatTypeBasicGroup.CONSTRUCTOR -> "basicgroup"
                TdApi.ChatTypeSupergroup.CONSTRUCTOR -> {
                    "supergroup"
                }
                TdApi.ChatTypeSecret.CONSTRUCTOR -> "secret"
                else -> "group"
            }

            val extraId: Long = when (type) {
                "supergroup" -> (conversation.type as TdApi.ChatTypeSupergroup).supergroupId
                "basicgroup" -> (conversation.type as TdApi.ChatTypeBasicGroup).basicGroupId
                else -> 0
            }

            val date: Long =
            if (conversation.lastMessage != null) conversation.lastMessage!!.date.toLong() * 1000
            else 0

            val unreadCount = conversation.unreadCount
            val canWrite = conversation.permissions.canSendMessages
            val title = conversation.title
            val photo = ""
            var lastMessage = ""

            if (conversation.lastMessage != null) {
                val tgMessage = TgMessagesRepository().getMessage(id, conversation.lastMessage!!.id).first()
                val message = Message.tgParse(tgMessage, conversation)
                lastMessage = if (message.text == "") {
                    message.attachments
                } else {
                    message.text
                }
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
                val user = App.application.tgRepository.getUser(id).first()

            }
            var data: Any? = null
            if (conversation.photo != null) {
                data = conversation.photo!!.small
            }

            return Conversation(id, type, date, unreadCount, canWrite, title, photo, lastMessage, membersCount, lastOnline, isOnline, "tg", userId, "main", data)
        }

        suspend fun tgParseLastMessage(conversation: Conversation, update: TdApi.UpdateChatLastMessage) {
            if (update.lastMessage != null) {
                val tgMessage = TgMessagesRepository().getMessage(
                    update.lastMessage!!.chatId, update.lastMessage!!.id
                ).first()
                val tgConversation = TgConversationsRepository().getChat(
                    update.lastMessage!!.chatId
                ).first()
                val message = Message.tgParse(tgMessage, tgConversation)

                conversation.last_message = if (message.text == "") {
                    message.attachments
                } else {
                    message.text
                }

                conversation.date = message.date
                conversation.unread_count = tgConversation.unreadCount
            }
        }

        suspend fun tgParseNewMessage(conversation: Conversation, update: TdApi.UpdateNewMessage) {
            if (update.message != null) {
                val tgMessage = TgMessagesRepository().getMessage(
                    update.message!!.chatId, update.message!!.id
                ).first()
                val tgConversation = TgConversationsRepository().getChat(
                    update.message!!.chatId
                ).first()
                val message = Message.tgParse(tgMessage, tgConversation)

                conversation.last_message = if (message.text == "") {
                    message.attachments
                } else {
                    message.text
                }

                conversation.date = message.date
                conversation.unread_count = tgConversation.unreadCount
            }
        }

        suspend fun tgParseOnlineStatus(
            conversation: Conversation,
            update: TdApi.UpdateUserStatus
        ) {
            val user = TgUserRepository().getUser(update.userId).first()
            conversation.is_online = false
            when (user.status.constructor) {
                TdApi.UserStatusEmpty.CONSTRUCTOR -> {
                    conversation.last_online = Constants.LastSeen.unknown
                }
                TdApi.UserStatusLastMonth.CONSTRUCTOR -> {
                    conversation.last_online = Constants.LastSeen.lastMonth
                }
                TdApi.UserStatusLastWeek.CONSTRUCTOR -> {
                    conversation.last_online = Constants.LastSeen.lastWeek
                }
                TdApi.UserStatusOffline.CONSTRUCTOR -> {
                    conversation.last_online =
                        ((user.status as TdApi.UserStatusOffline).wasOnline).toLong() * 1000
                }
                TdApi.UserStatusOnline.CONSTRUCTOR -> {
                    conversation.last_online = Constants.LastSeen.unknown
                    conversation.is_online = true
                }
                TdApi.UserStatusRecently.CONSTRUCTOR -> {
                    conversation.last_online = Constants.LastSeen.recently
                }
            }
        }
    }
}
