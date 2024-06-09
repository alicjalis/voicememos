package edu.put.voicememos

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var btnPlay : ImageButton
    private lateinit var btnBackward : ImageButton
    private lateinit var btnForward : ImageButton
    private lateinit var speedChip : Chip
    private lateinit var seekBar : SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        var filePath = intent.getStringExtra("filepath")
        var fileName = intent.getStringExtra("filename")

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        btnBackward = findViewById(R.id.btnBackward)
        btnForward = findViewById(R.id.btnForward)
        btnPlay = findViewById(R.id.btnPlay)
        speedChip = findViewById(R.id.chip)
        seekBar = findViewById(R.id.seekBar)

        btnPlay.setOnClickListener{
            playPausePlayer()
        }
    }

    private fun playPausePlayer(){
        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)
        }else{
            mediaPlayer.pause()
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)

        }
    }
}