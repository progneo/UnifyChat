/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.progcorp.unitedmessengers.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.ui.theme.UMTheme
import java.util.*

@Composable
fun UserAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { },
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    user: User?
) {
    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    val backgroundColor = backgroundColors.containerColor(
        scrollFraction = scrollBehavior?.scrollFraction ?: 0f
    ).value
    val foregroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent
    )
    Box(modifier = Modifier.background(backgroundColor)) {
        SmallTopAppBar(
            modifier = modifier,
            actions = actions,
            title = title,
            scrollBehavior = scrollBehavior,
            colors = foregroundColors,
            navigationIcon = {
                UserIcon(
                    user = user,
                    modifier = Modifier
                        .size(60.dp)
                )
            }
        )
    }
}

@Preview
@Composable
fun MainAppBarPreview() {
    UMTheme {
        UserAppBar(title = { Text("Войдите в профиль") }, user = null,
            actions = {
            })
    }
}

@Preview
@Composable
fun MainAppBarPreviewDark() {
    UMTheme(isDarkTheme = true) {
        UserAppBar(title = { Text("Войдите в профиль") }, user = null,
            actions = {
            })
    }
}

@Preview
@Composable
fun MainAppBarUnPreview() {
    UMTheme {
        UserAppBar(
            title = { Text("Слава Лис") },
            user = User(
                0,
                "Слава",
                "Лис",
                "https://sun4-17.userapi.com/s/v1/ig2/KYZfqE2ScHwWprJiyKjE_9Zbx0JwO1k_K2YAf95nDeX6tlon9gtUpvFs_jJnYH7qxp4KFgmWVW3VF8pSR0S7EoWq.jpg?size=50x50&quality=96&crop=129,365,821,821&ava=1",
                Date().time,
                true,
                false
            ),
            actions = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { })
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .height(24.dp),
                    contentDescription = null
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { })
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .height(24.dp),
                    contentDescription = null
                )
            })
    }
}

@Preview
@Composable
fun MainAppBarUnPreviewDark() {
    UMTheme(isDarkTheme = true) {
        UserAppBar(title = { Text("Слава Лис") }, user = User(
            0,
            "Slava",
            "Lis",
            "E:\\Develop\\Kotlin\\United_Messengers\\app\\src\\main\\res\\drawable\\avatar.jpg",
            Date().time,
            true,
            false
        ),
            actions = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { })
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .height(24.dp),
                    contentDescription = null
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { })
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .height(24.dp),
                    contentDescription = null
                )
            })
    }
}
