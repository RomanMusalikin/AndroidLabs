package com.example.lab01

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val phrases = listOf(
            "Привет, Андроид!",
            "Котлин — это круто!",
            "Случайная фраза номер три",
            "Улыбнитесь, вас снимает эмулятор! ХА-ХА-ХА-ХА-ХА (нет)",
            "Программа работает исправно"
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        val textMain: EditText = findViewById(R.id.textMain)
        val btnInsertText: Button = findViewById(R.id.buttonSetText)
        val btnClear: Button = findViewById(R.id.buttonClear)

        btnInsertText.setOnClickListener {
            textMain.setText(phrases.random())
        }

        btnClear.setOnClickListener {
            textMain.setText("")
        }
    }
}