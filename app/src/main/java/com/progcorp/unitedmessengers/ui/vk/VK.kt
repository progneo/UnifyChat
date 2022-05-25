@file:OptIn(ExperimentalMaterial3Api::class)

package com.progcorp.unitedmessengers.ui.vk

import android.provider.Telephony
import androidx.compose.animation.core.DecayAnimation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageVoiceNote
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.ui.FunctionalityNotAvailablePopup
import com.progcorp.unitedmessengers.ui.components.ConversationItem
import com.progcorp.unitedmessengers.ui.components.UserAppBar
import com.progcorp.unitedmessengers.ui.theme.UMTheme
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun VK(
    modifier: Modifier = Modifier,
    viewModel: VKViewModel = viewModel(),
    navigateToConversation: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val scope = rememberCoroutineScope()

    val conversation = listOf(
        Conversation(0,
            User(
                0,
                "Слава",
                "Лис",
                "https://sun4-17.userapi.com/s/v1/ig2/KYZfqE2ScHwWprJiyKjE_9Zbx0JwO1k_K2YAf95nDeX6tlon9gtUpvFs_jJnYH7qxp4KFgmWVW3VF8pSR0S7EoWq.jpg?size=50x50&quality=96&crop=129,365,821,821&ava=1",
                Date().time,
                false,
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
        ),Conversation(0,
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
        ),Conversation(0,
            User(
                0,
                "Slava",
                "Lis",
                "https://sun4-17.userapi.com/s/v1/ig2/KYZfqE2ScHwWprJiyKjE_9Zbx0JwO1k_K2YAf95nDeX6tlon9gtUpvFs_jJnYH7qxp4KFgmWVW3VF8pSR0S7EoWq.jpg?size=50x50&quality=96&crop=129,365,821,821&ava=1",
                Date().time,
                false,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
            User(
                0,
                "Slava",
                "Lis",
                "https://sun4-17.userapi.com/s/v1/ig2/KYZfqE2ScHwWprJiyKjE_9Zbx0JwO1k_K2YAf95nDeX6tlon9gtUpvFs_jJnYH7qxp4KFgmWVW3VF8pSR0S7EoWq.jpg?size=50x50&quality=96&crop=129,365,821,821&ava=1",
                Date().time,
                false,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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
        ),Conversation(0,
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

    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Conversations(
                    conversations = conversation,
                    navigateToConversation = {},
                    scrollState = scrollState
                )
            }
            MainBar(
                scrollBehavior = scrollBehavior,
                user = null,
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}

@Composable
fun Conversations(
    conversations: List<Conversation>,
    navigateToConversation: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        LazyColumn(
            state = scrollState,
            contentPadding =
                WindowInsets.statusBars.add(WindowInsets(top = 64.dp, right = 10.dp, left = 10.dp)).asPaddingValues(),
            modifier = Modifier
                .testTag("conversationsTag")
                .fillMaxSize()
        ) {
            for (conversation in conversations) {
                item {
                    ConversationItem(
                        conversation = conversation
                    )
                }
            }
        }
    }
}

@Composable
fun MainBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    user: User?
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }
    UserAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onNavIconPressed,
        title = {
            Text(
                text = if (user == null) "Войдите в профиль" else "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            if (user != null) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { functionalityNotAvailablePopupShown = true })
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .height(24.dp),
                    contentDescription = null
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { functionalityNotAvailablePopupShown = true })
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .height(24.dp),
                    contentDescription = null
                )
            }
        },
        user = user
    )
}
