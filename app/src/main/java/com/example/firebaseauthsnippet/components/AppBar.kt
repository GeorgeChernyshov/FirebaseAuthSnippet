package com.example.firebaseauthsnippet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebaseauthsnippet.theme.FirebaseAuthSnippetTheme

@Composable
fun AppBar(name: String, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineMedium,
            text = name,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    FirebaseAuthSnippetTheme {
        AppBar("Title")
    }
}