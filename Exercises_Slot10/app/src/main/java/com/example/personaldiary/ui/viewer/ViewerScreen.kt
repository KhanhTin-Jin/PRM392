package com.example.personaldiary.ui.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    filePath: String?,
    onBack: () -> Unit
) {
    val text = remember(filePath) {
        if (filePath.isNullOrBlank()) "Không có file để đọc."
        else runCatching { File(filePath).readText(Charsets.UTF_8) }.getOrElse {
            "Không thể đọc file: ${it.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viewer") },
                navigationIcon = { /* có thể thêm IconButton Back nếu muốn */ }
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = text, fontFamily = FontFamily.Monospace)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
    }
}
