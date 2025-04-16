package com.miniproject.fraudcalldetectionapp
import com.miniproject.R

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recorder: MediaRecorder
    private lateinit var audioFilePath: String
    private val PERMISSION_REQUEST_CODE = 101
    private val RECORD_DURATION = 5000L  // 5 seconds
    private val backendUrl = "http://10.0.2.2:5000/analyze_speech"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uploadButton: Button = findViewById(R.id.btnUploadAudio)

        // Ask for permissions
        if (!checkPermissions()) {
            requestPermissions()
        }

        uploadButton.setOnClickListener {
            if (checkPermissions()) {
                startRecording()
            } else {
                Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show()
                requestPermissions()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return recordPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED &&
                readPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun startRecording() {
        audioFilePath = "${externalCacheDir?.absolutePath}/recorded_audio.3gp"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Recording failed: ${e.message}", Toast.LENGTH_LONG).show()
                return
            }
        }

        Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                recorder.stop()
                recorder.release()
                Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show()
                uploadAudio(audioFilePath)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to stop recording: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, RECORD_DURATION)
    }

    private fun uploadAudio(filePath: String) {
        val file = File(filePath)

        if (!file.exists()) {
            Toast.makeText(this, "Audio file not found!", Toast.LENGTH_LONG).show()
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio",
                "recorded_audio.wav",  // Backend expects .wav, rename even if it's .3gp
                RequestBody.create("audio/wav".toMediaTypeOrNull(), file)
            )
            .build()

        val request = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Upload failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "No response from server"
                runOnUiThread {
                    Toast.makeText(applicationContext, "Detection Result: $responseBody", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions are required to record audio", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
