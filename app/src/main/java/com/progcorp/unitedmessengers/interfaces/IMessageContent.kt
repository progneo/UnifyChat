package com.progcorp.unitedmessengers.interfaces

import java.io.Serializable

interface IMessageContent : Serializable {
    val text: String
}