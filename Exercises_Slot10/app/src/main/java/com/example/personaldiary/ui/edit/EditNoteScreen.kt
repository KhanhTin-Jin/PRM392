package com.example.personaldiary.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    vm: EditNoteViewModel,
    onDone: () -> Unit,
    onExportInternal: ((title: String, content: String) -> Unit)? = null // sẽ cắm FileExporter sau
) {
    val title by vm.title.collectAsState()
    val content by vm.content.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (vm.isEditing) "Edit Note" else "New Note") }) }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title, onValueChange = vm::updateTitle,
                label = { Text("Title") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content, onValueChange = vm::updateContent,
                label = { Text("Content") }, minLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = title.isNotBlank() && content.isNotBlank(),
                    onClick = { vm.save(onDone) }
                ) { Text("Save") }

                OutlinedButton(onClick = onDone) { Text("Back") }

                if (vm.isEditing) {
                    OutlinedButton(onClick = { vm.delete(onDone) }) { Text("Delete") }
                }

                if (onExportInternal != null && title.isNotBlank()) {
                    OutlinedButton(onClick = { onExportInternal(title, content) }) {
                        Text("Export Internal")
                    }
                }
            }
        }
    }
}
