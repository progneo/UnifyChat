package com.progcorp.unitedmessengers.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
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
                    HighlightedChatSummary(
                        text = "Локация"
                    )
                }
            }
            is MessageVoiceNote -> {
                HighlightedChatSummary(
                    text = "Голосовое сообщение"
                )
            }
            is MessageVideoNote -> {
                Row(modifier = modifier) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null
                    )
                    BasicChatSummary(
                        text = "Видео-сообщение"
                    )
                }
            }
            is MessageChat -> {
                BasicChatSummary(
                    text = it.text
                )
            }
            is MessageCollage -> {
                Row(modifier = modifier) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null
                    )
                    HighlightedChatSummary(
                        text = "${it.paths.size} фото"
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
                HighlightedChatSummary(
                    text = it.text,
                    modifier = modifier
                )
            }
            is MessageExpiredPhoto -> {
                HighlightedChatSummary(
                    text = it.text,
                    modifier = modifier
                )
            }
            is MessagePoll -> {
                HighlightedChatSummary(
                    text = "Голосование",
                    modifier = modifier
                )
            }
            is MessageUnknown -> {
                HighlightedChatSummary(
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
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Composable
fun HighlightedChatSummary(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
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
fun OnlineAvatar(photo: String) {
    Image(
        painter = rememberAsyncImagePainter(photo),
        contentDescription = null,
        modifier = Modifier
            .size(56.dp)
            .clip(shape = CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Composable
fun OfflineAvatar(photo: String) {
    Image(
        painter = rememberAsyncImagePainter(photo),
        contentDescription = null,
        modifier = Modifier
            .size(56.dp)
            .clip(shape = CircleShape)
    )
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    modifier: Modifier = Modifier
) {
    when (conversation.companion) {
        is User -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .padding(vertical = 6.dp)
                    .height(56.dp),
            ) {
                if (conversation.companion.isOnline) {
                    OnlineAvatar(photo = conversation.companion.photo)
                } else {
                    OfflineAvatar(photo = conversation.companion.photo)
                }

                Column (modifier = Modifier.padding(horizontal = 12.dp)){
                    Row (Modifier.paddingFrom(LastBaseline, before = 23.dp)) {
                        ChatTitle(
                            text = "${conversation.companion.firstName} ${conversation.companion.lastName}",
                            modifier = Modifier.align(Alignment.Bottom)
                        )
                        Spacer(Modifier.weight(1f))
                        conversation.lastMessage?.timeStamp?.timeMsToDateWithDaysAgo()?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .width(32.dp),
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row (Modifier.padding(bottom = 10.dp)) {
                        ChatSummary(conversation.lastMessage)
                        Spacer(Modifier.weight(1f))
                        UnreadCountItem(count = conversation.unreadCount, modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }
    }
}

@Composable
fun UnreadCountItem(count: Int, modifier: Modifier = Modifier) {
    if (count > 0) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                .padding(1.dp)
        ) {
            Text(
                text = count.toString(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                modifier = Modifier
                    .defaultMinSize(20.dp)
                    .width(32.dp)
            )
        }
    }
    else if (count < 0) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(32.dp)
        ) {
            Box (
                modifier = modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                    .width(8.dp)
                    .aspectRatio(1f)
            )
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
fun UnreadCountPreview() {
    UMTheme() {
        UnreadCountItem(count = 100)
    }
}

@Preview
@Composable
fun ChatItemPreview() {
    UMTheme (isDarkTheme = false) {
        ConversationItem(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            conversation = Conversation(0,
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
                ),
                -1
            )
        )
    }
}


@Preview
@Composable
fun ChatItemDarkPreview() {
    UMTheme (isDarkTheme = true) {
        ConversationItem(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            conversation = Conversation(0,
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
                ),
                100
            )
        )
    }
}