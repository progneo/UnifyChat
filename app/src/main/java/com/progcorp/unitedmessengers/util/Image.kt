package com.progcorp.unitedmessengers.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.coil.rememberCoilPainter
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import kotlinx.coroutines.Dispatchers
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File

@Composable
fun TelegramImage(
    client: TelegramClient,
    file: TdApi.File?,
    modifier: Modifier = Modifier
) {
    val photo = file?.let {
        client.downloadableFile(file).collectAsState(file.local.path, Dispatchers.IO)
    } ?: remember { mutableStateOf(null) }
    photo.value?.let {
        Image(
            painter = rememberCoilPainter(
                request = File(it),
                shouldRefetchOnSizeChange = { _, _ -> false },
            ),
            contentDescription = null,
            modifier = modifier,
        )
    } ?: Box(modifier.background(Color.LightGray))
}
