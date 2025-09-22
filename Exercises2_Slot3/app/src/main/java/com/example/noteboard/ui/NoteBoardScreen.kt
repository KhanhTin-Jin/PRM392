package com.example.noteboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteboard.domain.model.Note
import com.example.noteboard.presentation.NoteViewModel
import com.example.noteboard.ui.theme.DarkNote
import com.example.noteboard.ui.theme.LightNote
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteBoardScreen(vm: NoteViewModel) {
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Trash bounds (for hit-test)
    var trashTopLeft by remember { mutableStateOf(Offset.Zero) }
    var trashSize by remember { mutableStateOf(IntSize.Zero) }
    val expandPx = with(LocalDensity.current) { 24.dp.toPx() } // nới vùng trúng

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Note Board", fontWeight = FontWeight.Bold) },
                modifier = Modifier.shadow(6.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- Input row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = vm::onInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Enter note") }
                    )
                    Spacer(Modifier.width(12.dp))
                    val canAdd = state.input.isNotBlank()
                    Button(onClick = vm::add, enabled = canAdd) { Text("+ Add") }
                }

                Spacer(Modifier.height(12.dp))

                // --- Free-move board
                FreeMoveBoard(
                    notes = state.notes,
                    isDraggingGlobal = state.isDragging,
                    onDragStartGlobal = { vm.setDragging(true) },
                    onDragEndGlobal = { payload ->
                        vm.setDragging(false)
                        val hit = isInsideTrashExpanded(
                            dragRootOffset = payload.lastRootOffset,
                            trashTopLeft = trashTopLeft,
                            trashSize = trashSize,
                            expandPx = expandPx
                        )
                        if (hit) {
                            vm.delete(payload.noteId)
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "Note deleted.",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    vm.undoDelete()
                                }
                            }
                        }
                    },
                    onLongPress = { id, selected -> vm.toggleSelected(id, !selected) }
                )
            }

            // --- Trash bin
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp)
                    .onGloballyPositioned { co ->
                        trashTopLeft = Offset(
                            x = co.positionInRoot().x,
                            y = co.positionInRoot().y
                        )
                        trashSize = co.size
                    }
                    .background(
                        color = if (state.isDragging) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🗑️", fontSize = 22.sp)
            }

            // --- Bonus: overlay highlight khi dragging
            AnimatedVisibility(
                visible = state.isDragging,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize().padding(8.dp)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(96.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(24.dp)
                            )
                    )
                }
            }
        }
    }
}

/* ----------------------- Free-move board & Note item ----------------------- */

private data class DragEndPayload(val noteId: Long, val lastRootOffset: Offset)

@Composable
private fun FreeMoveBoard(
    notes: List<Note>,
    isDraggingGlobal: Boolean,
    onDragStartGlobal: () -> Unit,
    onDragEndGlobal: (DragEndPayload) -> Unit,
    onLongPress: (noteId: Long, isSelected: Boolean) -> Unit
) {
    val positions = remember { mutableStateMapOf<Long, Offset>() }

    fun defaultPosFor(index: Int): Offset {
        val base = 8f
        val step = 64f
        val k = index % 6
        return Offset(base + step * k, base + step * k)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 64.dp)
    ) {
        notes.forEachIndexed { index, note ->
            val pos = positions.getOrPut(note.id) { defaultPosFor(index) }

            BoardNoteItem(
                note = note,
                initialPos = pos,
                onLongPress = { onLongPress(note.id, note.isSelected) },
                onDragStart = onDragStartGlobal,
                onDrag = { delta ->
                    val current = positions[note.id] ?: pos
                    positions[note.id] = current + delta
                },
                onDragEnd = { lastRoot ->
                    onDragEndGlobal(DragEndPayload(note.id, lastRoot))
                }
            )
        }
    }
}

@Composable
private fun BoardNoteItem(
    note: Note,
    initialPos: Offset,
    onLongPress: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: (lastRootOffset: Offset) -> Unit
) {
    var coords: LayoutCoordinates? by remember { mutableStateOf(null) }
    var lastRoot by remember { mutableStateOf(initialPos) }
    var offset by remember { mutableStateOf(initialPos) }

    val bg = if (note.isSelected) {
        if (MaterialTheme.colorScheme.isLight()) LightNote else DarkNote
    } else MaterialTheme.colorScheme.surface

    Surface(
        tonalElevation = if (note.isSelected) 4.dp else 0.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        color = bg,
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .onGloballyPositioned { coords = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd(lastRoot) },
                    onDragCancel = { onDragEnd(lastRoot) }
                ) { change, dragAmount ->
                    change.consumeAllChanges()
                    offset += dragAmount
                    coords?.let { co ->
                        val local = change.position
                        lastRoot = co.positionInRoot() + local
                    }
                    onDrag(dragAmount)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            }
            .padding(12.dp)
    ) {
        Text(text = note.content, style = MaterialTheme.typography.bodyLarge)
    }
}

/* ----------------------------- Helpers & Utils ---------------------------- */

private fun isInsideTrashExpanded(
    dragRootOffset: Offset,
    trashTopLeft: Offset,
    trashSize: IntSize,
    expandPx: Float
): Boolean {
    val x = dragRootOffset.x
    val y = dragRootOffset.y
    val left = trashTopLeft.x - expandPx
    val top = trashTopLeft.y - expandPx
    val right = trashTopLeft.x + trashSize.width + expandPx
    val bottom = trashTopLeft.y + trashSize.height + expandPx
    return x in left..right && y in top..bottom
}

@Composable
private fun ColorScheme.isLight(): Boolean {
    return background.luminance() > surface.luminance()
}
