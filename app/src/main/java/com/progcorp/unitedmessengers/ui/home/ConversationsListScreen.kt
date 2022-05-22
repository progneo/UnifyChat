package com.progcorp.unitedmessengers.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import com.progcorp.unitedmessengers.data.model.Conversation

@Composable
fun ChatsLoaded(
    client: TelegramClient,
    chats: List<Conversation>,
    modifier: Modifier = Modifier,
    onChatClicked: (Long) -> Unit,
    showSnackBar: (String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        if (chats.isEmpty()) {
            item {
                LoadingChats()
            }
        }
        itemsIndexed(chats) { index, item ->
            item.let { chat ->
                ChatItem(
                    client,
                    chat,
                    modifier = Modifier.clickable(onClick = {
                        onChatClicked(item.id)
                    })
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    startIndent = 64.dp
                )
            }
        }
    }
}

@Composable
fun LoadingChats(modifier: Modifier = Modifier) {
    LinearProgressIndicator(modifier = modifier.fillMaxWidth())
}

