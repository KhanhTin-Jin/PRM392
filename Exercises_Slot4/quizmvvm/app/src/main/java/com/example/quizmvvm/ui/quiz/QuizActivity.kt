package com.example.quizmvvm.ui.quiz

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quizmvvm.R

class QuizActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QuizActivity"
        const val EXTRA_PLAYER_NAME = "EXTRA_PLAYER_NAME"

        private const val KEY_INDEX = "KEY_INDEX"
        private const val KEY_SCORE = "KEY_SCORE"
        private const val KEY_IS_ANSWERED = "KEY_IS_ANSWERED"
        private const val KEY_SELECTED = "KEY_SELECTED"

        private const val AUTO_NEXT_DELAY_MS = 800L

        private lateinit var playerName: String
    }

    private val vm: QuizViewModel by viewModels()

    private lateinit var tvWelcome: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_quiz)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvScore   = findViewById(R.id.tvScore)
        tvQuestion= findViewById(R.id.tvQuestion)
        btnA = findViewById(R.id.btnA)
        btnB = findViewById(R.id.btnB)
        btnC = findViewById(R.id.btnC)
        btnD = findViewById(R.id.btnD)

        playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player" // CHANGE
        tvWelcome.text = "Welcome, $playerName"

        // val name = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"
        // tvWelcome.text = "Welcome, $name"

        // Khôi phục đầy đủ trạng thái khi xoay / process death
        if (savedInstanceState != null) {
            val idx = savedInstanceState.getInt(KEY_INDEX, 0)
            val sc  = savedInstanceState.getInt(KEY_SCORE, 0)
            val isAnswered = savedInstanceState.getBoolean(KEY_IS_ANSWERED, false)
            val selected = savedInstanceState.getInt(KEY_SELECTED, -1).let { if (it >= 0) it else null }
            vm.restoreState(idx, sc, isAnswered, selected)
        }

        vm.currentQuestion.observe(this) { q ->
            tvQuestion.text = q.text
            btnA.text = q.options[0]
            btnB.text = q.options[1]
            btnC.text = q.options[2]
            btnD.text = q.options[3]
            // Khi load câu mới: bật lại nút (VM đã reset trong nextQuestion)
            setAnswerButtonsEnabled(vm.isAnswered.value != true)
        }
        vm.score.observe(this) { sc ->
            tvScore.text = "Score: $sc"
        }
        vm.isAnswered.observe(this) { answered ->
            setAnswerButtonsEnabled(!answered)
        }

        val answerClick: (Int) -> Unit = { selected ->
            when (vm.submitAnswer(selected)) {
                AnswerResult.CORRECT -> {
                    showTopToast("✅ Correct!")
                    scheduleAutoNext()
                }
                AnswerResult.INCORRECT -> {
                    showTopToast("❌ Incorrect!")
                    scheduleAutoNext() // NEW: sai cũng auto next
                }
                AnswerResult.ALREADY_ANSWERED -> {
                    showTopToast("Bạn đã trả lời câu này rồi")
                }
            }
        }

        btnA.setOnClickListener { answerClick(0) }
        btnB.setOnClickListener { answerClick(1) }
        btnC.setOnClickListener { answerClick(2) }
        btnD.setOnClickListener { answerClick(3) }

    }

    private fun setAnswerButtonsEnabled(enabled: Boolean) {
        btnA.isEnabled = enabled
        btnB.isEnabled = enabled
        btnC.isEnabled = enabled
        btnD.isEnabled = enabled
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
        outState.putInt(KEY_INDEX, vm.currentIndex.value ?: 0)
        outState.putInt(KEY_SCORE, vm.score.value ?: 0)
        outState.putBoolean(KEY_IS_ANSWERED, vm.isAnswered.value ?: false)
        outState.putInt(KEY_SELECTED, vm.selectedIndex.value ?: -1)
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onRestart() { super.onRestart(); Log.d(TAG, "onRestart") }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mainHandler.removeCallbacksAndMessages(null) // tránh tự next khi Activity bị hủy
    }

    // QuizActivity.kt (thêm cuối file hoặc trên cùng trong class)
    private fun showTopToast(message: String) {
        val t = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        // Đẩy lên TOP | CENTER_HORIZONTAL, cách mép trên ~64dp
        val yOffsetPx = (64 * resources.displayMetrics.density).toInt()
        t.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, yOffsetPx)
        t.show()
    }

    private fun scheduleAutoNext() {
        mainHandler.removeCallbacksAndMessages(null) // tránh queue chồng
        mainHandler.postDelayed({
            val moved = vm.nextQuestion()
            if (!moved) {
                goToResultScreen() // NEW
            }
        }, AUTO_NEXT_DELAY_MS)
    }

    private fun goToResultScreen() {
        val intent = android.content.Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_PLAYER_NAME, playerName)
            .putExtra(ResultActivity.EXTRA_SCORE, vm.score.value ?: 0)
            .putExtra(ResultActivity.EXTRA_TOTAL, vm.totalCount())
        startActivity(intent)
        finish() // đóng quiz để back không quay lại màn đã kết thúc
    }

}
