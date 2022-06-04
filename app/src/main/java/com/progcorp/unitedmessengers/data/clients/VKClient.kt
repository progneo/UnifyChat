package com.progcorp.unitedmessengers.data.clients

import android.content.SharedPreferences
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.model.VKLongPollServer
import com.progcorp.unitedmessengers.interfaces.IAccountService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class VKClient (private val sharedPreference: SharedPreferences) : IAccountService {
    private val _dataSource = App.application.vkDataSource
    private val _repository = App.application.vkRepository

    private lateinit var _longPollRetrofit: Retrofit
    private lateinit var _longPollServer: VKLongPollServer

    init {
        setupClient()
    }

    private fun setupClient() {
        MainScope().launch {
            _longPollServer = _repository.getLongPollServer().first()
            _longPollRetrofit = Retrofit.Builder()
                .baseUrl("https://${_longPollServer.server}?act=a_check")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
    }

    override var token: String?
        get() {
            return sharedPreference.getString(TOKEN, null)
        }
        set(value) {
            with(sharedPreference.edit()) {
                if (value == null) {
                    remove(TOKEN)
                }
                else {
                    putString(TOKEN, value)
                }
                apply()
            }
        }

    override var userId: String?
        get() {
            return sharedPreference.getString(USER_ID, null)
        }
        set(value) {
            with(sharedPreference.edit()) {
                if (value == null) {
                    remove(USER_ID)
                }
                else {
                    putString(USER_ID, value)
                }
                apply()
            }
        }

    companion object {
        const val SCOPE = "1073737727"
        const val TOKEN = "token"
        const val USER_ID = "userId"
    }
}