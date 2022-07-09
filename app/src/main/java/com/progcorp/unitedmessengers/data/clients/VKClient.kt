package com.progcorp.unitedmessengers.data.clients

import android.content.SharedPreferences
import com.progcorp.unitedmessengers.data.db.VKDataSource
import com.progcorp.unitedmessengers.data.db.VKRepository
import com.progcorp.unitedmessengers.data.model.VKLongPollServer
import com.progcorp.unitedmessengers.interfaces.IAccountService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class VKClient (private val _sharedPreference: SharedPreferences) : IAccountService {
    private var _dataSource: VKDataSource = VKDataSource(this)
    var repository: VKRepository = VKRepository(_dataSource)

    private var _lpServer: VKLongPollServer? = null
    var lpRetrofit: Retrofit? = null

    init {
        MainScope().launch {
            _lpServer = repository.getLongPollServer().first()
            lpRetrofit = Retrofit.Builder()
                .baseUrl("https://${_lpServer!!.server}?act=a_check&key=${_lpServer!!.key}&ts=${_lpServer!!.ts}&pts=${_lpServer!!.pts}&wait=25&mode=32&version=3&extended=true/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
    }

    fun updateLpServer(ts: Long) {
        _lpServer!!.ts = ts
    }

    override var token: String?
        get() {
            return _sharedPreference.getString(TOKEN, null)
        }
        set(value) {
            with(_sharedPreference.edit()) {
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
            return _sharedPreference.getString(USER_ID, null)
        }
        set(value) {
            with(_sharedPreference.edit()) {
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