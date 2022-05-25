package com.progcorp.unitedmessengers.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.ui.theme.UMTheme
import com.progcorp.unitedmessengers.util.ConvertTime
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ChatTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        maxLines = 1,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    )
}

@Composable
fun ChatSummary(message: Message?, modifier: Modifier = Modifier) {
    message?.content.let {
        when (it) {
            is MessageText -> BasicChatSummary(
                text = it.text,
                modifier = modifier
            )
            is MessageVideo -> HighlightedChatSummary(
                "Видео",
                modifier = modifier
            )
            is MessageSticker -> HighlightedChatSummary(
                "Стикер",
                modifier = modifier
            )
            is MessageAnimation -> HighlightedChatSummary(
                "GIF",
                modifier = modifier
            )
            is MessageLocation -> {
                Row(modifier = modifier) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null
                    )
                    Text(
                        text = "Локация",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            is MessageVoiceNote -> {
                BasicChatSummary(
                    text = "Голосовое сообщение",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            is MessageVideoNote -> {
                Row(modifier = modifier) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null
                    )
                    Text(
                        text = "Видео-сообщение",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            is MessageChat -> {
                BasicChatSummary(
                    text = it.text,
                    modifier = modifier
                )
            }
            is MessageCollage -> {
                Row(modifier = modifier) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null
                    )
                    Text(
                        text = "${it.paths} фото",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            is MessageDocument -> {
                HighlightedChatSummary(
                    text = "Документ",
                    modifier = modifier
                )
            }
            is MessageExpiredVideo -> {
                BasicChatSummary(
                    text = it.text,
                    modifier = modifier
                )
            }
            is MessageExpiredPhoto -> {
                BasicChatSummary(
                    text = it.text,
                    modifier = modifier
                )
            }
            is MessagePoll -> {
                BasicChatSummary(
                    text = "Голосование",
                    modifier = modifier
                )
            }
            is MessageUnknown -> {
                BasicChatSummary(
                    text = it.text,
                    modifier = modifier
                )
            }
            else -> Text("Необработанное сообщение")
        }
    }
}

@Composable
fun BasicChatSummary(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .paddingFrom(LastBaseline, after = 8.dp, before = 8.dp) // Space to 1st bubble
    )
}

@Composable
fun HighlightedChatSummary(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primaryContainer,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .paddingFrom(LastBaseline, after = 8.dp, before = 8.dp)
    )
}

@Composable
fun ChatTime(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    modifier: Modifier = Modifier
) {
    when (conversation.companion) {
        is User -> {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(vertical = 3.dp)) {
                val borderColor = if (conversation.companion.isOnline) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.background
                }
                Image(
                    painter = rememberAsyncImagePainter(conversation.companion.photo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(shape = CircleShape)
                        .border(1.5.dp, borderColor, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
                Column (modifier = Modifier.padding(horizontal = 10.dp)){
                    Row {
                        ChatTitle(
                            text = "${conversation.companion.firstName} ${conversation.companion.lastName}",
                            modifier = Modifier.alignBy(LastBaseline)
                        )
                        Spacer(Modifier.weight(1f))
                        conversation.lastMessage?.timeStamp?.timeMsToDateWithDaysAgo()?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                modifier = Modifier.alignBy(LastBaseline),
                                color = Color.Gray
                            )
                        }
                    }
                    ChatSummary(conversation.lastMessage)
                }
            }
        }
    }
}

private fun Long.timeMsToDateWithDaysAgo(): String {
    val numOfDays = TimeUnit.MILLISECONDS.toDays(Date().time - this)

    return when {
        numOfDays >= 1.toLong() -> numOfDays.toString() + "d"
        else -> ConvertTime.toTime(this)
    }
}

@Preview
@Composable
fun ChatItemPreview() {
    UMTheme (isDarkTheme = true) {
        ConversationItem(
            Conversation(0,
                User(
                    0,
                    "Slava",
                    "Lis",
                    "https://sun4-17.userapi.com/s/v1/ig2/KYZfqE2ScHwWprJiyKjE_9Zbx0JwO1k_K2YAf95nDeX6tlon9gtUpvFs_jJnYH7qxp4KFgmWVW3VF8pSR0S7EoWq.jpg?size=50x50&quality=96&crop=129,365,821,821&ava=1",
                    Date().time,
                    true,
                    false
                ),
                Message(
                    0,
                    1653316110000,
                    User(
                        0,
                        "Slava",
                        "Lis",
                        "https://sun4-17.userapi.com/s/v1/ig2/KYZfqE2ScHwWprJiyKjE_9Zbx0JwO1k_K2YAf95nDeX6tlon9gtUpvFs_jJnYH7qxp4KFgmWVW3VF8pSR0S7EoWq.jpg?size=50x50&quality=96&crop=129,365,821,821&ava=1",
                        Date().time,
                        true,
                        false
                    ),
                    true,
                    0,
                    MessageVoiceNote("Text", "Voice")
                )
            )
        )
    }
}