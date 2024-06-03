package edu.put.voicememos

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private val MICROPHONE_PERMISSION_CODE = 200
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var fileNameEditText: EditText
    private lateinit var btnStop: Button
    private lateinit var btnResume: Button
    private var isRecordingStopped: Boolean = false
    private var currentFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileNameEditText = findViewById(R.id.editTextFileName)
        btnStop = findViewById(R.id.btnStop)
        btnResume = findViewById(R.id.btnResume)

        if (isMicrophonePresent()) {
            getMicrophonePermission()
        }
    }

    fun btnRecordPressed(v: View) {
        val fileName = fileNameEditText.text.toString()
        if (fileName.isEmpty()) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show()
            return
        }

        currentFileName = fileName

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(getRecordingFilePath(currentFileName!!)) // Save to the user-specified file
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            isRecordingStopped = false
            Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnStopPressed(v: View) {
        try {
            mediaRecorder?.apply {
                stop()
            }
            isRecordingStopped = true
            btnStop.visibility = View.GONE
            btnResume.visibility = View.VISIBLE
            Toast.makeText(this, "Recording stopped. You can resume or save it.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnResumePressed(v: View) {
        try {
            mediaRecorder?.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(getRecordingFilePath(currentFileName!!)) // Continue with the same file
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            isRecordingStopped = false
            btnResume.visibility = View.GONE
            btnStop.visibility = View.VISIBLE
            Toast.makeText(this, "Recording resumed", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnSavePressed(v: View) {
        if (!isRecordingStopped) {
            Toast.makeText(this, "Please stop the recording first", Toast.LENGTH_LONG).show()
            return
        }

        if (currentFileName.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show()
            return
        }

        try {
            mediaRecorder?.release()
            mediaRecorder = null
            Toast.makeText(this, "Recording saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnPlayPressed(v: View) {
        val fileName = fileNameEditText.text.toString()
        if (fileName.isEmpty()) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(getRecordingFilePath(fileName))
                prepare()
                start()
                Toast.makeText(this@MainActivity, "Playing recording", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isMicrophonePresent(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    private fun getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_CODE
            )
        }
    }

    private fun getRecordingFilePath(fileName: String): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(musicDirectory, "$fileName.mp3")
        return file.path
    }
}
