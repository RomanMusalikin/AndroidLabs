package com.example.lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class DetailActivity : ComponentActivity() {

    // Переменная для плеера, чтобы мы могли управлять им
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        val imageRes = intent.getIntExtra("image", 0)
        // Получаем ID звука. Если звука нет, придет 0.
        val soundRes = intent.getIntExtra("sound_res", 0)

        val img = findViewById<ImageView>(R.id.detail_image)
        val txtTitle = findViewById<TextView>(R.id.detail_title)
        val txtDesc = findViewById<TextView>(R.id.detail_desc)
        val btnSound = findViewById<Button>(R.id.btn_sound)

        txtTitle.text = title
        txtDesc.text = desc
        img.setImageResource(imageRes)

        // Кнопка видна только если передан ID звука (не 0)
        if (soundRes != 0) {
            btnSound.visibility = View.VISIBLE
            btnSound.setOnClickListener {
                // Останавливаем предыдущий звук, если он еще играет
                mediaPlayer?.stop()
                mediaPlayer?.release()

                // Создаем и запускаем новый звук
                mediaPlayer = MediaPlayer.create(this, soundRes)
                mediaPlayer?.start()

                // Чистим память, когда звук доиграет
                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
            }
        } else {
            btnSound.visibility = View.GONE
        }
    }

    // Хорошим тоном считается выключать звук, если пользователь закрыл экран
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}