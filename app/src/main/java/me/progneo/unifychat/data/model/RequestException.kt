package me.progneo.unifychat.data.model

class RequestException(val code: Int, message: String) : Throwable(message)
