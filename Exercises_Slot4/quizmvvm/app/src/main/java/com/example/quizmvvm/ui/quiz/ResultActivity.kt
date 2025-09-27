package com.example.quizmvvm.ui.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.quizmvvm.R
import com.example.quizmvvm.ui.main.MainActivity

class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PLAYER_NAME = "RESULT_PLAYER_NAME"
        const val EXTRA_SCORE = "RESULT_SCORE"
        const val EXTRA_TOTAL = "RESULT_TOTAL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvCongrats = findViewById<TextView>(R.id.tvCongrats)
        val tvSummary = findViewById<TextView>(R.id.tvSummary)
        val btnTryAgain = findViewById<Button>(R.id.btnTryAgain)
        val btnBackHome = findViewById<Button>(R.id.btnBackHome)

        val name = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val total = intent.getIntExtra(EXTRA_TOTAL, 0)

        tvCongrats.text = "🎉 Congratulations, $name!"
        tvSummary.text = "You scored $score / $total"

        btnTryAgain.setOnClickListener {
            // Chơi lại ngay: mở lại QuizActivity với cùng tên
            val i = Intent(this, QuizActivity::class.java)
                .putExtra(QuizActivity.EXTRA_PLAYER_NAME, name)
            startActivity(i)
            finish()
        }

        btnBackHome.setOnClickListener {
            // Quay về màn nhập tên
            val i = Intent(this, MainActivity::class.java)
            // Xoá back stack để tránh quay lại Result/Quiz cũ
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        }
    }
}
