package me.progneo.unifychat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.progneo.unifychat.data.model.clients.VKClient
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var vkClient: VKClient
}
