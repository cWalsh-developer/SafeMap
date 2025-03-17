package com.example.safemap.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun StreetlightLoadDialog(
    onDismiss: () -> Unit,
    loading: Boolean,
    haveStreetlightsLoaded: Boolean,
    onRetry: () -> Unit
) {
        LaunchedEffect(key1 = haveStreetlightsLoaded) {
            if (haveStreetlightsLoaded) {
                onDismiss()
            }
        }
    if (!haveStreetlightsLoaded) {

        Dialog(onDismissRequest = {
            // Do nothing when the dialog is dismissed by clicking outside
        }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading Streetlight Data")
                    CircularProgressIndicator(color = Color(0xff26662a))
                    onRetry()
                }
            }
        }
    }
}