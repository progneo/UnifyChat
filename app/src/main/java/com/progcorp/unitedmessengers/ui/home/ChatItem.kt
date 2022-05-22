package com.progcorp.unitedmessengers.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.util.TelegramImage
import org.drinkless.td.libcore.telegram.TdApi

@Composable
fun ChatTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        maxLines = 1,
        style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.W500)
    )
}

@Composable
fun ChatSummary(conversation: Conversation, modifier: Modifier = Modifier) {
     when (conversation.last_message) {
        "Видео" -> HighlightedChatSummary("Видео", modifier = modifier)
        "Звонок" -> HighlightedChatSummary("Звонок", modifier = modifier)
        "Аудиозапись" -> {
            Row(modifier = modifier) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null
                )
                Text(
                    text = conversation.last_message,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        "GIF" -> HighlightedChatSummary("GIF", modifier = modifier)
        "Место на карте" -> HighlightedChatSummary(
            text = conversation.last_message,
            modifier = modifier
        )
        "Голосовое сообщение" -> {
            Row(modifier = modifier) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null
                )
                Text(
                    text = conversation.last_message,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        "Видео-сообщение" -> {
            Row(modifier = modifier) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null
                )
                Text(
                    text = conversation.last_message,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        "Присоединился к Telegram" -> HighlightedChatSummary(
            text = conversation.last_message,
            modifier = modifier
        )
        "Участник покинул чат" -> HighlightedChatSummary(
            text = conversation.last_message,
            modifier = modifier
        )
        else -> BasicChatSummary(
            text = conversation.last_message,
            modifier = modifier
        )
    }
}

@Composable
fun BasicChatSummary(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.subtitle1,
        maxLines = 2,
        modifier = modifier
    )
}

@Composable
fun HighlightedChatSummary(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.subtitle1,
        color = MaterialTheme.colors.primaryVariant,
        maxLines = 2,
        modifier = modifier
    )
}

@Composable
fun ChatTime(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.caption,
        maxLines = 1,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatItem(client: TelegramClient, conversation: Conversation, modifier: Modifier = Modifier) {
    ListItem(modifier,
        icon = {
            TelegramImage(
                client = client,
                file = (conversation.data) as TdApi.File,
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .size(48.dp)
            )
        },
        secondaryText = {
            ChatSummary(conversation)
        }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChatTitle(conversation.title, modifier = Modifier.weight(1.0f))
            ChatTime((conversation.date * 1000).toRelativeTimeSpan(), modifier = Modifier.alpha(0.6f))
        }
    }
}

private fun Long.toRelativeTimeSpan(): String =
    DateUtils.getRelativeTimeSpanString(
        this,
        System.currentTimeMillis(),
        DateUtils.SECOND_IN_MILLIS
    ).toString()