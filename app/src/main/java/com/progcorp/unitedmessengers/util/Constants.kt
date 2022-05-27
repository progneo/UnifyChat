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
    object MessageType {
        const val text: Int = 0
        const val textOut: Int = 1

        const val sticker: Int = 2
        const val stickerOut: Int = 3

        const val chat: Int = 4

        const val animatedEmoji: Int = 5
        const val animatedEmojiOut: Int = 6

        const val animation: Int = 7
        const val animationOut: Int = 8

        const val collage: Int = 9
        const val collageOut: Int = 10

        const val document: Int = 11
        const val documentOut: Int = 12

        const val documents: Int = 13
        const val documentsOut: Int = 14

        const val expiredPhoto: Int = 15
        const val expiredPhotoOut: Int = 16

        const val expiredVideo: Int = 17
        const val expiredVideoOut: Int = 18

        const val photo: Int = 19
        const val photoOut: Int = 20

        const val poll: Int = 21
        const val pollOut: Int = 22

        const val video: Int = 23
        const val videoOut: Int = 24

        const val videoNote: Int = 25
        const val videoNoteOut: Int = 26

        const val voiceNote: Int = 27
        const val voiceNoteOut: Int = 28

        const val unknown: Int = 29
        const val unknownOut: Int = 30
    }
}