package com.example.noteboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteboard.presentation.NoteViewModel
import com.example.noteboard.ui.NoteBoardScreen
import com.example.noteboard.ui.theme.NoteBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteBoardTheme {
                val vm: NoteViewModel = viewModel()
                NoteBoardScreen(vm = vm)
            }
        }
    }
}
