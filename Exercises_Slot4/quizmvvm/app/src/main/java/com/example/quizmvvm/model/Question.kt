package com.example.quizmvvm.model

data class Question(
    val text: String,
    val options: List<String>, // size = 4
    val correctIndex: Int      // 0..3
)
