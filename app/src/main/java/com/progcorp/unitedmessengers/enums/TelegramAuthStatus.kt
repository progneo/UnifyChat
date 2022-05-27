package com.progcorp.unitedmessengers.enums

enum class TelegramAuthStatus {
    UNAUTHENTICATED,
    WAIT_FOR_NUMBER,
    WAIT_FOR_CODE,
    WAIT_FOR_PASSWORD,
    AUTHENTICATED,
    UNKNOWN
}