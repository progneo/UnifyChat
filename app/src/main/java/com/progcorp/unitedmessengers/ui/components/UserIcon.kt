package com.progcorp.unitedmessengers.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.progcorp.unitedmessengers.data.model.User

@Composable
fun UserIcon(
    user: User?,
    modifier: Modifier = Modifier
) {
    if (user != null) {
        Icon(
            painter = rememberAsyncImagePainter(user.photo),
            contentDescription = null,
            modifier = modifier
                .padding(10.dp)
                .clip(shape = CircleShape)
        )
    }
    else {
        Box (
            modifier.clickable(onClick = {})
        ) {
            Icon(
                imageVector = Icons.Outlined.Login,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewUserIcon() {
    UserIcon(user = null,modifier = Modifier.size(60.dp))
}