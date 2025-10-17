package com.example.personaldiary.ui.home

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.personaldiary.data.local.Note
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: NotesViewModel,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onExportAll: () -> Unit
    ) {
    val context = LocalContext.current
    val notes by vm.notes.collectAsState()
    var q by remember { mutableStateOf("") }
    
    // State for delete confirmation dialog
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    
    // Trigger refresh on every resume (return from settings)
    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "🔄 HomeScreen resumed - triggering refresh")
        vm.refreshAfterBackup()
    }
    
    // Log whenever notes state changes
    LaunchedEffect(notes) {
        Log.d("HomeScreen", "notes state updated: ${notes.size} items")
        notes.take(3).forEachIndexed { idx, note ->
            Log.d("HomeScreen", "  [$idx] id=${note.id}, title='${note.title}'")
        }
    }

    val timestamp = remember {
        SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val text = com.example.personaldiary.core.storage.ExportAllBuilder.build(notes)
            com.example.personaldiary.core.storage.SafIO.writeText(context, uri, text)
            Toast.makeText(context, "Đã xuất ghi chú vào file", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Diary") },
                actions = {
                    IconButton(onClick = onOpenSettings) { Text("⚙️") }
                    IconButton(onClick = {
                        exportLauncher.launch("DiaryExport_${timestamp}.txt")
                    }) {
                        Text("⇩") // hoặc dùng Icon nếu muốn
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("HomeScreen", "➕ ADD button clicked")
                onAdd()
            }) { Text("+") }
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = q,
                onValueChange = {
                    Log.d("HomeScreen", "🔍 Search query changed: '$it'")
                    q = it
                    vm.setQuery(it)
                },
                label = { Text("Tìm theo tiêu đề") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Chưa có ghi chú. Bấm + để bắt đầu.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notes, key = { it.id }) { n ->
                        NoteRow(
                            note = n,
                            onClick = { onOpen(n.id) },
                            onDelete = { 
                                Log.d("HomeScreen", "🗑️ DELETE button clicked for note ${n.id}")
                                noteToDelete = n
                            }
                        )
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        noteToDelete?.let { note ->
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Xác nhận xóa ghi chú này?\n\n\"${note.title}\"") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Log.d("HomeScreen", "✅ Delete confirmed for note ${note.id}")
                            vm.delete(note.id)
                            noteToDelete = null
                        }
                    ) {
                        Text("Xóa", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToDelete = null }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
private fun NoteRow(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
                Text(fmt.format(Date(note.updatedAt)), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                note.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
