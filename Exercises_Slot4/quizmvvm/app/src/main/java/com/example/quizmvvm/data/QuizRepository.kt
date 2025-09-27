package com.example.quizmvvm.data

import com.example.quizmvvm.model.Question

class QuizRepository {
    private val questions = listOf(
        Question(
            text = "Android ViewModel dùng để làm gì?",
            options = listOf("Vẽ UI", "Quản lý dữ liệu UI", "Xử lý mạng", "Lưu file"),
            correctIndex = 1
        ),
        Question(
            text = "Hàm nào được gọi khi Activity hiển thị lên màn hình?",
            options = listOf("onStart", "onResume", "onPause", "onStop"),
            correctIndex = 1
        ),
        Question(
            text = "Bundle onSaveInstanceState dùng khi nào?",
            options = listOf("Lưu trạng thái tạm", "Gọi API", "Tạo Intent", "Đổi theme"),
            correctIndex = 0
        ),
        Question(
            text = "Intent dùng để làm gì trong Android?",
            options = listOf("Tạo layout", "Gọi API", "Giao tiếp giữa các thành phần", "Lưu dữ liệu"),
            correctIndex = 2
        ),
        Question(
            text = "Hàm nào được gọi khi Activity bị ẩn nhưng chưa bị huỷ?",
            options = listOf("onPause", "onStop", "onDestroy", "onRestart"),
            correctIndex = 1
        ),
        Question(
            text = "LiveData trong Android dùng để làm gì?",
            options = listOf("Lưu trạng thái UI", "Quan sát dữ liệu thay đổi", "Tạo View", "Gọi API"),
            correctIndex = 1
        ),
        Question(
            text = "Hàm nào được gọi khi Activity quay lại sau khi bị ẩn?",
            options = listOf("onRestart", "onResume", "onStart", "onCreate"),
            correctIndex = 0
        ),
        Question(
            text = "MVVM là viết tắt của?",
            options = listOf("Model-View-ViewModel", "Main-View-Variable-Model", "Multi-View-Visual-Model", "Model-Variable-ViewModel"),
            correctIndex = 0
        ),
        Question(
            text = "Hàm nào KHÔNG thuộc vòng đời của Activity?",
            options = listOf("onCreate", "onDestroy", "onExecute", "onPause"),
            correctIndex = 2
        ),
        Question(
            text = "Để truyền dữ liệu giữa các Activity, ta dùng?",
            options = listOf("Bundle", "Intent Extra", "SharedPreferences", "ViewModel"),
            correctIndex = 1
        )
    )

    fun getQuestions(): List<Question> = questions
    val size: Int get() = questions.size
}
