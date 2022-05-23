package com.progcorp.unitedmessengers.util

object Constants {
    object LastSeen {
        const val unknown: Long = 0
        const val lastMonth: Long = 1
        const val lastWeek: Long = 2
        const val recently: Long = 3
    }
    object Messenger {
        const val VK: Int = 0
        const val TG: Int = 1
    }
    object ConversationType {
        const val unsupported: Int = 0
        const val dialog: Int = 1
        const val supergroup: Int = 2
        const val basicGroup: Int = 3
        const val bot: Int = 4
    }
}