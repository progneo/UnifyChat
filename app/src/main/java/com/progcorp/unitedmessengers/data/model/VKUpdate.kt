package com.progcorp.unitedmessengers.data.model

abstract class VKUpdate

class VKUpdateNewMessages(val messages: List<Message>) : VKUpdate()
class VKUpdateUserStatus(val userId: Long, val isOnline: Boolean, val timeStamp: Long) : VKUpdate()
class VKUpdateDeleteMessage(val messageId: Long, val chatId: Long) : VKUpdate()
class VKUpdateMessagesContent(val messages: List<Message>) : VKUpdate()
class VKUpdateUnreadCount(val count: Int) : VKUpdate()