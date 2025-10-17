package com.example.personaldiary

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.personaldiary.core.storage.ServiceLocator
import com.example.personaldiary.core.storage.FileExporter
import com.example.personaldiary.ui.edit.EditNoteScreen
import com.example.personaldiary.ui.edit.EditNoteViewModel
import com.example.personaldiary.ui.home.HomeScreen
import com.example.personaldiary.ui.home.NotesViewModel
import com.example.personaldiary.ui.viewer.ViewerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = ServiceLocator.settings(applicationContext)

        setContent {
            // ✨ Epoch để "đổi đời" repo/VM sau backup/restore
            var dbEpoch by remember { mutableStateOf(0) }
            
            // ⚠️ Lấy repo trong Compose + phụ thuộc epoch
            val repo = remember(dbEpoch) {
                ServiceLocator.notesRepo(applicationContext)
            }
            
            val nav = rememberNavController()
            com.example.personaldiary.ui.AppTheme(settings) {
                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        val vm: NotesViewModel = viewModel(factory = NotesViewModel.Factory(repo))
                        HomeScreen(
                            vm = vm,
                            onAdd = { nav.navigate("edit/-1") },
                            onOpen = { id -> nav.navigate("edit/$id") },
                            onOpenSettings = { nav.navigate("settings") },
                            onExportAll = { nav.navigate("export") }
                        )
                    }

                    composable(
                        route = "edit/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val id =
                            backStackEntry.arguments?.getLong("id") ?: -1L   // ✅ lấy id chắc chắn

                        val vm: EditNoteViewModel = viewModel(
                            factory = EditNoteViewModel.Factory(
                                repo,
                                id
                            )         // ✅ truyền id vào factory
                        )

                        EditNoteScreen(
                            vm = vm,
                            onDone = { nav.navigateUp() },
                            onExportInternal = { title, content ->
                                val idForFile =
                                    if (vm.isEditing) vm.currentId else System.currentTimeMillis()
                                val file = FileExporter.exportSingleNoteToInternal(
                                    applicationContext, idForFile, title, content
                                )
                                Toast.makeText(
                                    this@MainActivity,
                                    "Đã xuất: ${file.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val path = Uri.encode(file.absolutePath)
                                nav.navigate("viewer?path=$path")
                            }
                        )
                    }

                    composable(
                        route = "viewer?path={path}",
                        arguments = listOf(navArgument("path") {
                            type = NavType.StringType; nullable = true
                        })
                    ) { entry ->
                        val abs = entry.arguments?.getString("path")?.let { Uri.decode(it) }
                        ViewerScreen(
                            filePath = abs,
                            onBack = { nav.navigateUp() }
                        )
                    }

                    composable("settings") {
                        com.example.personaldiary.ui.settings.SettingsScreen(
                            settings = settings,
                            onBack = { nav.navigateUp() },
                            onActionCompleted = {
                                // 1) Clear cache (đã làm trong SettingsScreen)
                                // 2) Bump epoch để repo/VM re-create
                                dbEpoch += 1
                                
                                // 3) Quay về Home bằng popUpTo để làm mới backstack
                                nav.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                }
            }
        }
    }
}
