package com.example.quizmvvm.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quizmvvm.data.QuizRepository
import com.example.quizmvvm.model.Question

enum class AnswerResult { CORRECT, INCORRECT, ALREADY_ANSWERED }

class QuizViewModel(
    private val repo: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _isAnswered = MutableLiveData(false)
    val isAnswered: LiveData<Boolean> = _isAnswered

    private val _selectedIndex = MutableLiveData<Int?>(null)
    val selectedIndex: LiveData<Int?> = _selectedIndex

    init {
        _currentQuestion.value = repo.getQuestions()[_currentIndex.value!!]
    }

    fun submitAnswer(selected: Int): AnswerResult {
        if (_isAnswered.value == true) return AnswerResult.ALREADY_ANSWERED

        val q = _currentQuestion.value ?: return AnswerResult.INCORRECT
        _selectedIndex.value = selected
        _isAnswered.value = true

        return if (selected == q.correctIndex) {
            _score.value = (_score.value ?: 0) + 1
            AnswerResult.CORRECT
        } else {
            AnswerResult.INCORRECT
        }
    }

    fun nextQuestion(): Boolean {
        val next = (_currentIndex.value ?: 0) + 1
        return if (next < repo.size) {
            _currentIndex.value = next
            _currentQuestion.value = repo.getQuestions()[next]
            // reset trạng thái cho câu mới
            _isAnswered.value = false
            _selectedIndex.value = null
            true
        } else {
            false
        }
    }

    fun restoreState(index: Int, score: Int, isAnswered: Boolean, selectedIndex: Int?) {
        _currentIndex.value = index
        _score.value = score
        _currentQuestion.value = repo.getQuestions()[index]
        _isAnswered.value = isAnswered
        _selectedIndex.value = selectedIndex
    }

    // QuizViewModel.kt
    fun totalCount(): Int = repo.size

}
