// com/example/personaldiary/ui/settings/SettingsScreen.kt
package com.example.personaldiary.ui.settings

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.personaldiary.core.prefs.SettingsManager
import com.example.personaldiary.core.storage.DbBackupHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsManager,
    onBack: () -> Unit,
    onActionCompleted: () -> Unit // Callback để khởi động lại HomeScreen sau backup/restore thành công
) {
    val theme by settings.theme.collectAsState()
    val font by settings.fontSize.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    
    // State for restore confirmation dialog
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val time = remember {
        SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
    }

    // CreateDocument: chọn nơi lưu file backup (.db)
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    DbBackupHelper.backupDbToUri(context = context, dest = uri)
                }.onSuccess {
                    launch(Dispatchers.Main) {
                        Log.d("SettingsScreen", "✅ WAL-SAFE BACKUP SUCCESS - Clearing repo cache")
                        com.example.personaldiary.core.storage.ServiceLocator.clearRepoCache()
                        Toast.makeText(context, "Backup thành công!", Toast.LENGTH_SHORT).show()
                        // Gọi callback để khởi động lại HomeScreen
                        onActionCompleted()
                    }
                }.onFailure { e ->
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Backup lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // OpenDocument: chọn file .db để restore
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Show confirmation dialog instead of restoring immediately
            selectedRestoreUri = uri
            showRestoreConfirmation = true
        }
    }
    
    // Function to perform actual restore operation
    fun performRestore(uri: Uri) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                DbBackupHelper.restoreDbFromUri(context = context, src = uri)
            }.onSuccess {
                launch(Dispatchers.Main) {
                    Log.d("SettingsScreen", "✅ WAL-SAFE RESTORE SUCCESS - Clearing repo cache")
                    com.example.personaldiary.core.storage.ServiceLocator.clearRepoCache()
                    Toast.makeText(context, "Restore thành công!", Toast.LENGTH_LONG).show()
                    // Gọi callback để khởi động lại HomeScreen
                    onActionCompleted()
                }
            }.onFailure { e ->
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Restore lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = theme == "light", onClick = { settings.setTheme("light") }, label = { Text("Light") })
                FilterChip(selected = theme == "dark", onClick = { settings.setTheme("dark") }, label = { Text("Dark") })
            }

            Text("Font size", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = font == "s", onClick = { settings.setFontSize("s") }, label = { Text("S") })
                FilterChip(selected = font == "m", onClick = { settings.setFontSize("m") }, label = { Text("M") })
                FilterChip(selected = font == "l", onClick = { settings.setFontSize("l") }, label = { Text("L") })
            }

            Divider()

            Text("Database", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { backupLauncher.launch("DiaryBackup_${time}.db") }) { Text("Backup DB") }
                OutlinedButton(onClick = { restoreLauncher.launch(arrayOf("*/*")) }) { Text("Restore DB") }
            }

            Text(
                text = "Khi Restore: dữ liệu hiện tại sẽ bị ghi đè.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Restore confirmation dialog
        if (showRestoreConfirmation) {
            AlertDialog(
                onDismissRequest = { 
                    showRestoreConfirmation = false
                    selectedRestoreUri = null
                },
                title = { Text("Xác nhận Restore") },
                text = { 
                    Text("Restore sẽ ghi đè dữ liệu hiện tại.\n\nTất cả ghi chú hiện tại sẽ bị mất và thay thế bằng dữ liệu từ file backup.\n\nTiếp tục?") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedRestoreUri?.let { uri ->
                                Log.d("SettingsScreen", "✅ Restore confirmed")
                                performRestore(uri)
                            }
                            showRestoreConfirmation = false
                            selectedRestoreUri = null
                        }
                    ) {
                        Text("Restore", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showRestoreConfirmation = false
                            selectedRestoreUri = null
                        }
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
