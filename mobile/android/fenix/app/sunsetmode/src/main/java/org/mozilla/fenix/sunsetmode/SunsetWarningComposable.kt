package org.mozilla.fenix.sunsetmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SunsetWarningComposable(warning: String) {
    Column(
        modifier = Modifier
            .background(Color.Yellow)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .background(Color.DarkGray)
                .padding(24.dp)
        ) {
            Text(
                color = Color.White,
                text = warning,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun SunsetWarningComposablePreview() {
    MaterialTheme {
        SunsetWarningComposable("Your phone is old and you should throw it in the sea")
    }
}