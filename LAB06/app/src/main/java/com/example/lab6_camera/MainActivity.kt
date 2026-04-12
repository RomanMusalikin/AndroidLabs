package com.example.lab6_camera

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var ivPhoto: ImageView
    private var imageUri: Uri? = null

    // 1. Контракт для создания снимка
    // Он берет Uri (путь к пустому файлу в галерее) и камера записывает туда фото
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Если фото сделано, показываем его в ImageView
            ivPhoto.setImageURI(imageUri)
            Toast.makeText(this, "Фото сохранено в галерею!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Снимок отменен", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Контракт для запроса разрешения (Runtime Permission)
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Разрешение дали -> запускаем камеру
            launchCamera()
        } else {
            // Разрешение не дали -> обрабатываем отказ (по заданию)
            Toast.makeText(this, "Отказ! Для работы приложения необходим доступ к камере.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivPhoto = findViewById(R.id.ivPhoto)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)

        btnTakePhoto.setOnClickListener {
            // При нажатии кнопки запрашиваем разрешение
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Метод подготовки файла и запуска камеры
    private fun launchCamera() {
        // Создаем метаданные для нового фото в Галерее
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Photo")
            put(MediaStore.Images.Media.DESCRIPTION, "Снимок из Лабораторной 6")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        // Сообщаем системе создать пустой файл в Галерее и получаем его путь (Uri)
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        // Передаем этот путь камере
        imageUri?.let { takePictureLauncher.launch(it) }
    }
}