package com.example.quizmvvm.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.quizmvvm.R
import com.example.quizmvvm.ui.quiz.QuizActivity

class MainActivity : AppCompatActivity() {

    companion object { private const val TAG = "MainActivity" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.etName)
        val btnStart = findViewById<Button>(R.id.btnStart)

        btnStart.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val i = Intent(this, QuizActivity::class.java)
                .putExtra(QuizActivity.Companion.EXTRA_PLAYER_NAME, name.ifEmpty { "Player" })
            startActivity(i)
        }
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onRestart() { super.onRestart(); Log.d(TAG, "onRestart") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
}