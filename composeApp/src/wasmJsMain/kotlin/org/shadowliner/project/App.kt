package org.shadowliner.project

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.browser.window

fun decodeBase64(base64: String): String {
    return window.atob(base64)
}

fun convertOutlineKeyToJson(outlineVpnKey: String): String {
    return try {
        val cleanedOutlineKey = outlineVpnKey.removeSuffix("/?outline=1")
        val outlineUrl = cleanedOutlineKey.removePrefix("ss://")
        val splitIndex = outlineUrl.indexOf('@')

        if (splitIndex == -1) throw IllegalArgumentException("Invalid key format")

        val base64Part = outlineUrl.substring(0, splitIndex)
        val decodedPart = decodeBase64(base64Part)

        val (cipher, password) = decodedPart.split(':')
        val hostPort = outlineUrl.substring(splitIndex + 1).split(':')
        val host = hostPort[0]
        val port = hostPort[1].toInt()

        """
        {
            "server": "$host",
            "server_port": $port,
            "password": "$password",
            "method": "$cipher",
            "local_address": "127.0.0.1",
            "local_port": 1080,
            "timeout": 300,
            "mode": "tcp_and_udp",
            "fast_open": false
        }
        """.trimIndent()
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

@Composable
fun AnimatedPulsatingBackground() {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedColor1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1E3A8A),
        targetValue = Color(0xFF8B5CF6),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor2 by infiniteTransition.animateColor(
        initialValue = Color(0xFFFF004D),
        targetValue = Color(0xFF8B0000),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF4500FF),
        targetValue = Color(0xFF0000FF),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(animatedColor1, animatedColor2, animatedColor3),
                    radius = 1500f,
                    center = Offset(0.5f, 0.5f)
                )
            )
            .blur(50.dp)
    )
}

@Composable
fun App() {
    var outlineKey by remember { mutableStateOf("") }
    var jsonOutput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedPulsatingBackground()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .background(Color.White.copy(alpha = 0.4f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = outlineKey,
                    onValueChange = { outlineKey = it },
                    label = { Text("Outline Key") },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 16.dp),
                    isError = errorMessage != null
                )

                Button(
                    onClick = {
                        jsonOutput = convertOutlineKeyToJson(outlineKey)
                        errorMessage = if (jsonOutput.startsWith("Error")) jsonOutput else null
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text("Convert")
                }

                TextField(
                    value = jsonOutput,
                    onValueChange = {},
                    label = { Text("JSON Output") },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 16.dp),
                    readOnly = true
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
