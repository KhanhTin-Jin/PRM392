package com.example.noteboard.domain.model

data class Note(
    val id: Long,
    val content: String,
    val isSelected: Boolean = false
)
