package com.example.contextapp

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.contextapp.Constraints
import com.example.contextapp.databinding.ActivityMainBinding
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedReader
import java.lang.Math.abs
import java.lang.Math.sqrt
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var Binding: ActivityMainBinding
    private lateinit var Service: ExecutorService
    private var VidCapture: VideoCapture<Recorder>? = null
    private var Rec: Recording? = null
    private lateinit var path1: Uri
    private var heartResult: String = ""
    private lateinit var pp:String
    private var respResult: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(Binding.root)

        Binding.btnHeartRate.setOnClickListener {
            Toast.makeText(this,"Enter",Toast.LENGTH_SHORT).show()
            if (allPermissionsGranted()) {
                Toast.makeText(this,"Yes",Toast.LENGTH_SHORT).show()
                startCamera();

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    Constraints.REQUIRED_PERMISSION,
                    Constraints.REQUEST_CODE_PERMISSION
                )

            }
        }
        Binding.btnRecord.setOnClickListener{
                captureVideo()
                //Log.e(Constraints.TAG,"Entered after capture video")
        }
        Binding.btnRespiratoryrate.setOnClickListener {
            respResult = callRespiratoryCalculator().toString()
            Binding.tvRespiratoryrate.setText(respResult)
        }
        Binding.btnUpload.setOnClickListener {
                if (Binding.tvRespiratoryrate.visibility == View.VISIBLE) {
                    while (Binding.cameraView.visibility == View.VISIBLE && Binding.btnRecord.visibility == View.VISIBLE) {
                        Binding.cameraView.visibility = View.INVISIBLE
                        Binding.btnRecord.visibility = View.GONE
                        while (Binding.tvRespiratoryrate.visibility == View.VISIBLE || Binding.tvHeartrate.visibility == View.VISIBLE) {
                            Binding.tvRespiratoryrate.visibility = View.INVISIBLE
                            Binding.tvHeartrate.visibility = View.INVISIBLE
                        }
                    }
                    Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Enter All Values", Toast.LENGTH_SHORT).show()
                }
            }

            Binding.btnSymptoms.setOnClickListener {
                Log.e(this.toString(),"Entered Intent: First")
                val intent = Intent(this, MainActivity2::class.java)
                intent.putExtra("HeartRate", heartResult)
                intent.putExtra("RespiratoryRate", respResult)
                Log.e(this.toString(),"Entered Intent: Second")
                startActivity(intent)
            }

            Service = Executors.newSingleThreadExecutor();

    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            //Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(Binding.cameraView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val recorded=Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            VidCapture = VideoCapture.withOutput(recorded)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,VidCapture)

            } catch(exc: Exception) {
                Log.e(Constraints.TAG, "Use case binding failed", exc)
            }
            val cam = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview)
            cam.cameraControl.enableTorch(true)

        }, ContextCompat.getMainExecutor(this))
    }

    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo() {
        val VidCapture = this.VidCapture ?: return
        val currentrecord = Rec
        if (currentrecord != null) {
            // Stop the current recording session.
            currentrecord.stop()
            Rec = null
            return
        }
        // create and start a new recording session
        val name = SimpleDateFormat(Constraints.FILE_NAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        Rec = VidCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@MainActivity,
                        android.Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Binding.btnRecord.apply {
                            Binding.tvRec.visibility = View.VISIBLE
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            path1=recordEvent.outputResults.outputUri
                            pp=convertMediaUriToPath(path1)
                            Log.e(Constraints.TAG,"Entered after conversion")
                            heartResult=doInBackground(pp)
                            Log.e(Constraints.TAG,"Entered after heart rate calculation")
                            Binding.tvHeartrate.setText(heartResult)
                            Log.e(Constraints.TAG,"Binding done")
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(Constraints.TAG, msg)
                        } else {
                            Rec?.close()
                            Rec = null
                            Log.e(Constraints.TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        Binding.btnRecord.apply {
                            Binding.tvRec.visibility = View.INVISIBLE
                            isEnabled = true
                        }
                    }
                }
            }
    }
    private fun callRespiratoryCalculator():Int {
        var previousValue = 0f
        var currentValue = 0f
        previousValue = 10f
        val bufferReader=BufferedReader(assets.open("CSVBreath19.csv").reader())
        val csvParser= CSVParser.parse(bufferReader, CSVFormat.DEFAULT)
        var Respdata : Double
        val list= mutableListOf<Double>()
        csvParser.forEach {
            it?.let{
                Respdata=it.get(0).toDouble()
                list.add(Respdata)
            }
        }
        var k=0
        for (i in 0..1279) {
            currentValue = sqrt(
                Math.pow(list[i].toDouble(), 2.0) + Math.pow(
                    list[i].toDouble(),
                    2.0
                ) + Math.pow(list[i].toDouble(), 2.0)
            ).toFloat()
            if (abs(x = previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue=currentValue
        }
        val ret= (k/45.00)

        return (ret*30).toInt()
    }
    fun convertMediaUriToPath(uri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(column_index)
        cursor.close()
        return path
    }
fun doInBackground(vararg params: String?): String {
    var m_bitmap: Bitmap? = null
    var retriever = MediaMetadataRetriever()
    var frameList = ArrayList<Bitmap>()
    try {

        retriever.setDataSource(params[0])
        var duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
        var aduration = duration!!.toInt()
        var i = 10
        while (i < aduration) {
            val bitmap = retriever.getFrameAtIndex(i)
            frameList.add(bitmap!!)
            i += 5
        }
    } catch (m_e: Exception) {
    } finally {
        retriever?.release()
        var redBucket: Long = 0
        var pixelCount: Long = 0
        val a = mutableListOf<Long>()
        for (i in frameList) {
            redBucket = 0
            for (y in 550 until 650) {
                for (x in 550 until 650) {
                    val c: Int = i.getPixel(x, y)
                    pixelCount++
                    redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                }
            }
            a.add(redBucket)
        }
        val b = mutableListOf<Long>()
        for (i in 0 until a.lastIndex - 5) {
            var temp =
                (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2) + a.elementAt(
                    i + 3
                ) + a.elementAt(
                    i + 4
                )) / 4
            b.add(temp)
        }
        var x = b.elementAt(0)
        var count = 0
        for (i in 1 until b.lastIndex) {
            var p = b.elementAt(i.toInt())
            if ((p - x) > 3500) {
                count = count + 1
            }
            x = b.elementAt(i.toInt())
        }
        var rate = ((count.toFloat() / 45) * 60).toInt()
        return (rate / 2).toString()
    }
}
        private fun allPermissionsGranted() =
        Constraints.REQUIRED_PERMISSION.all{
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
}

