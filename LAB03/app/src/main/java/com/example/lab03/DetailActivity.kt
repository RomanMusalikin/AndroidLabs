package com.example.lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class DetailActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        val imageRes = intent.getIntExtra("image", 0)
        val soundRes = intent.getIntExtra("sound_res", 0)

        val img = findViewById<ImageView>(R.id.detail_image)
        val txtTitle = findViewById<TextView>(R.id.detail_title)
        val txtDesc = findViewById<TextView>(R.id.detail_desc)
        val btnSound = findViewById<Button>(R.id.btn_sound)

        txtTitle.text = title
        txtDesc.text = desc
        img.setImageResource(imageRes)

        if (soundRes != 0) {
            btnSound.visibility = View.VISIBLE
            btnSound.setOnClickListener {
                mediaPlayer?.stop()
                mediaPlayer?.release()

                mediaPlayer = MediaPlayer.create(this, soundRes)
                mediaPlayer?.start()

                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
            }
        } else {
            btnSound.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}