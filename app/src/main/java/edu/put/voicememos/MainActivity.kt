package edu.put.voicememos

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val MICROPHONE_PERMISSION_CODE = 200
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var fileNameEditText: EditText
    private lateinit var btnStop: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var btnList: ImageButton
    private lateinit var btnRecord: ImageButton
    private var isRecordingStopped: Boolean = false
    private var currentFileName: String? = null
    private var tempFilePath: String? = null

    // Timer variables
    private lateinit var tvTimer: TextView
    private lateinit var waveformView: WaveformView
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var handler = Handler()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetBG: View
    private lateinit var btnCancel: Button
    private lateinit var btnOk: Button
    private lateinit var filenameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.tvTimer)
        waveformView = findViewById(R.id.waveformView)

        fileNameEditText = findViewById(R.id.editTextFileName)
        btnStop = findViewById(R.id.btnStop)
        btnResume = findViewById(R.id.btnResume)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        btnList = findViewById(R.id.btnList)
        btnRecord = findViewById(R.id.btnRecord)
        val bottomSheet: LinearLayout = findViewById(R.id.bottomSheet)
        bottomSheetBG = findViewById(R.id.bottomSheetBG)


        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        filenameInput = findViewById(R.id.filenameInput)
        btnCancel = findViewById(R.id.btnCancel)
        btnOk = findViewById(R.id.btnOk)

        fileNameEditText.visibility = View.GONE // Initially hide the file name input field

        if (isMicrophonePresent()) {
            getMicrophonePermission()
        }

        btnCancel.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBG.visibility = View.GONE
            fileNameEditText.visibility = View.GONE

            // Hide the keyboard
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(filenameInput.windowToken, 0)
        }

        btnOk.setOnClickListener {
            val fileName = filenameInput.text.toString()
            if (fileName.isEmpty()) {
                Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            saveRecording(fileName)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBG.visibility = View.GONE
            fileNameEditText.visibility = View.GONE

            btnStop.visibility = View.GONE
            btnResume.visibility = View.GONE
            btnSave.visibility = View.GONE
            btnRecord.visibility = View.VISIBLE
            btnList.visibility = View.VISIBLE
            tvTimer.text = "00:00:00"
            waveformView.clearAmplitudes()

            // Hide the keyboard
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(filenameInput.windowToken, 0)
        }

        filenameInput.setOnClickListener {
            filenameInput.selectAll()
        }

    }

    private val amplitudeRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mediaRecorder != null) {
                val maxAmplitude = mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                waveformView.addAmplitude(maxAmplitude)
            }
            handler.postDelayed(this, 100)
        }
    }


    fun btnRecordPressed(v: View) {
        tempFilePath = getTempRecordingFilePath()
        btnList.visibility = View.GONE
        btnSave.visibility = View.VISIBLE
        btnRecord.visibility = View.GONE
        btnStop.visibility = View.VISIBLE
        btnStop.setImageResource(R.drawable.ic_pause)
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(tempFilePath) // Save to a temporary file
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            isRecordingStopped = false
            startTime = System.currentTimeMillis()
            elapsedTime = 0L
            handler.post(timerRunnable)
            handler.post(amplitudeRunnable)
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
            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(amplitudeRunnable)
            elapsedTime += System.currentTimeMillis() - startTime
            btnStop.visibility = View.GONE
            btnResume.visibility = View.VISIBLE
            btnResume.setImageResource(R.drawable.ic_resume)
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
                setOutputFile(tempFilePath) // Continue with the same temporary file
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            isRecordingStopped = false
            startTime = System.currentTimeMillis()
            handler.post(timerRunnable)
            handler.post(amplitudeRunnable)
            btnResume.visibility = View.GONE
            btnStop.visibility = View.VISIBLE
            Toast.makeText(this, "Recording resumed", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnDeletePressed(v: View) {
        try {
            // Check if mediaRecorder is initialized and is recording
            if (mediaRecorder != null && !isRecordingStopped) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
            mediaRecorder = null
            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(amplitudeRunnable)

            // Delete the temporary file
            val tempFile = File(tempFilePath ?: return)
            if (tempFile.exists()) {
                tempFile.delete()
            }

            // Reset variables
            isRecordingStopped = false
            currentFileName = null
            tempFilePath = null
            elapsedTime = 0L
            tvTimer.text = "00:00:00"
            waveformView.clearAmplitudes()

            // Update UI
            btnStop.visibility = View.GONE
            btnResume.visibility = View.GONE
            btnSave.visibility = View.GONE
            btnRecord.visibility = View.VISIBLE
            btnList.visibility = View.VISIBLE
            fileNameEditText.visibility = View.GONE

            Toast.makeText(this, "Recording deleted", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnSavePressed(v: View) {
        if (!isRecordingStopped) {
            Toast.makeText(this, "Please stop the recording first", Toast.LENGTH_LONG).show()
            return
        }

        // Generate a default file name based on the current date and time
        val defaultFileName = generateDefaultFileName()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBG.visibility = View.VISIBLE
        filenameInput.setText(defaultFileName) // Set the default file name
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

    private fun getTempRecordingFilePath(): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val tempFile = File(musicDirectory, "tempRecording.mp3")
        return tempFile.path
    }

    private fun saveRecording(fileName: String) {
        currentFileName = fileName
        try {
            val finalFilePath = getRecordingFilePath(currentFileName!!)
            val tempFile = File(tempFilePath!!)
            val finalFile = File(finalFilePath)

            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete() // Remove the temporary file

            mediaRecorder?.release()
            mediaRecorder = null
            Toast.makeText(this, "Recording saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            val millis = elapsedTime + (currentTime - startTime)
            val minutes = (millis / 60000).toInt() % 60
            val seconds = (millis / 1000).toInt() % 60
            val milliseconds = (millis % 1000) / 10
            tvTimer.text = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
            handler.postDelayed(this, 10)
        }
    }

    private fun generateDefaultFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = Date()
        return "recording_${dateFormat.format(date)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        val tempFile = File(tempFilePath ?: return)
        if (tempFile.exists()) {
            tempFile.delete() // Delete the temporary file if it exists
        }
    }
}
