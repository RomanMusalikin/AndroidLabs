package com.example.lab01

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        val textView: TextView = findViewById(R.id.textMain)
        val btnInsertText: Button = findViewById(R.id.buttonSetText)

        btnInsertText.setOnClickListener {
            textView.text ="Это моя первая программа в Андроид!"
        }
    }
}