package org.mozilla.fenix.sunsetmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
            .height(200.dp)
            .fillMaxWidth()
            .padding(24.dp),
    ) {
        Row(modifier = Modifier
            .background(Color.DarkGray)
            .padding(24.dp)
        ) {
            Text(
                color = Color.White,
                text = warning,
                style = MaterialTheme.typography.headlineMedium
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