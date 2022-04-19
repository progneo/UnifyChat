package com.progcorp.unitedmessengers.data.db.vk

import android.util.Log
import com.progcorp.unitedmessengers.data.db.vk.requests.VKLongPollServer
import com.progcorp.unitedmessengers.data.model.vk.LongPollVK

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

class VKLongPoll(private val onLongPollFetched: OnLongPollFetched) {
    fun getLongPoll() {
        VK.execute(VKLongPollServer(), object : VKApiCallback<LongPollVK> {
            override fun success(result: LongPollVK) {
                onLongPollFetched.startListener(result)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
            }
        })
    }

    interface OnLongPollFetched {
        fun startListener(longPoll: LongPollVK)
    }

    companion object {
        const val TAG = "VKLongPoll"
    }
}