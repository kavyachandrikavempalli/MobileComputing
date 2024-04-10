package com.example.sample

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import com.example.sample.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.slider.Slider
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.pow

class MainActivity : ComponentActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var service: ExecutorService
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var uriOut: Uri
    private lateinit var pathVid: String
    private var result1: Float = 0.0f
    private var result2: Float = 0.0f
    private var result3: Float = 0.0f

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.anim_open) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.anim_close) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.anim_from) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.anim_to) }
    private var clicked = false

    private var day = 0
    private var month = 0
    private var year =0
    private var hour = 0
    private var minute = 0

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0

    private val datePic: IntArray = intArrayOf(0,0,0,0,0,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiKey = getString(R.string.google_maps_api_key)
        Places.initialize(applicationContext, apiKey)

        binding.floatAdd.setOnClickListener{
            onAddClicked()
        }

        binding.floatSchedule.setOnClickListener{
            addSchedule()
        }

        binding.floatTravel.setOnClickListener{
            addTravel()
        }

        binding.floatRecord.setOnClickListener{
            addRecord()
        }

        binding.btnHr.setOnClickListener {
            if (allPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constraints.REQUEST_CODE_PERMISSION
                )
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    Constraints.REQUIRED_PERMISSION,
                    Constraints.REQUEST_CODE_PERMISSION
                )
            }

            while (binding.backCam.visibility != View.VISIBLE && binding.btnRec.visibility != View.VISIBLE) {
                binding.backCam.visibility = View.VISIBLE
                binding.btnRec.visibility = View.VISIBLE
            }
            binding.tvHr.visibility = View.VISIBLE
        }

        binding.btnRec.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(Constraints.TAG, "Started")
                captureVideo()
                Log.e(Constraints.TAG, "Exited")
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constraints.REQUEST_CODE_PERMISSION
                )
                Toast.makeText(this, "No Storage Permission", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRes.setOnClickListener {
            while (binding.tvRes.visibility != View.VISIBLE) {
                binding.tvRes.visibility = View.VISIBLE
                result2 = respRateSense()
                binding.tvRes.text = result2.toString()
            }
        }

        binding.bslSlider.addOnChangeListener{ slider, value, fromUser ->
            result3 = value
        }

        binding.bslSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                var sval = slider.value
                if (sval <= 100)
                    binding.tvBslVal.text = " Normal (${sval.toInt()})"
                else if (sval > 100 && sval <= 125)
                    binding.tvBslVal.text = " Pre-Diabeties (${sval.toInt()})"
                else
                    binding.tvBslVal.text = " Diabeties (${sval.toInt()})"
            }
        })

        binding.btnUpS.setOnClickListener {
            if (binding.tvRes.visibility == View.VISIBLE) {
                while (binding.backCam.visibility == View.VISIBLE && binding.btnRec.visibility == View.VISIBLE) {
                    binding.backCam.visibility = View.INVISIBLE
                    binding.btnRec.visibility = View.GONE
                    while (binding.tvRes.visibility == View.VISIBLE || binding.tvHr.visibility == View.VISIBLE) {
                        binding.tvRes.visibility = View.INVISIBLE
                        binding.tvHr.visibility = View.INVISIBLE
                    }
                }
                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
//                TODO("Not yet implemented") // get data here or in symptoms button
            } else {
                Toast.makeText(this, "Measure All Values", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSym.setOnClickListener {
            val intent = Intent(this, SymptomsActivity::class.java)
            intent.putExtra("HeartRate", result1.toString())
            intent.putExtra("RespiratoryRate", result2.toString())
            intent.putExtra("BloodSugarLevel", result3.toString())
            startActivity(intent)
        }
        service = Executors.newSingleThreadExecutor()
    }

    private fun addRecord() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_record)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val closebtn: Button = dialog.findViewById(R.id.btnClose)
        val db = MedRecordDb(context = this)
        val RecList: ArrayList<Float> = db.getrecords()
        Log.e("Database:", RecList.toString())

        closebtn.setOnClickListener{
            dialog.cancel()
        }
    }

    private fun addTravel() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_travel)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val fromLoc: TextView = dialog.findViewById(R.id.etFrom)
        val toLoc: TextView = dialog.findViewById(R.id.etTo)
        val datebtn2: Button = dialog.findViewById(R.id.btnDate1)
        val savebtn2: Button = dialog.findViewById(R.id.btnSave1)
        val sugg: TextView= dialog.findViewById(R.id.tvSugg)

        datebtn2.setOnClickListener{
            pickDate()
        }
        savebtn2.setOnClickListener{

        }
        sugg.setOnClickListener{

        }

    }

    private fun addSchedule() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_schedule)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val datebtn: Button = dialog.findViewById(R.id.btnDate)
        val savebtn1: Button = dialog.findViewById(R.id.btnSave)

        datebtn.setOnClickListener{
            pickDate()
        }
        savebtn1.setOnClickListener{
            val URL: String = "https://api.thingspeak.com/update?api_key=VU3IXS40QEL7RENH&field1=55&field2=8&field3=25&field4=12&field5=1"
            val client = OkHttpClient()
            val request: Request = Request.Builder().url(URL).build()
            client.newCall(request).enqueue(object :Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace();
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.i("Response", "Revieved Response")
                    response.use {
                        if (!response.isSuccessful){
                            Log.e("response", "ERROR")
                        }
                        else{
                            Log.e("response","SUCCESS")
                        }
                    }
                }
            })
        }
    }

    private fun getDateTimeCalender() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        getDateTimeCalender()
        DatePickerDialog(this,this, year,month,day).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateTimeCalender()
        TimePickerDialog(this,this,hour,minute,true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minutes: Int) {
        savedHour = hourOfDay
        savedMinute = minutes

        datePic[1] = day
        datePic[2] = month
        datePic[3] = year
        datePic[4] = hour
        datePic[5] = minute

        Log.e("Response1: ", datePic[1].toString())
    }

    private fun onAddClicked() {
        if(!clicked){
            binding.floatRecord.visibility = View.VISIBLE
            binding.floatTravel.visibility = View.VISIBLE
            binding.floatSchedule.visibility = View.VISIBLE

            binding.floatRecord.startAnimation(fromBottom)
            binding.floatTravel.startAnimation(fromBottom)
            binding.floatSchedule.startAnimation(fromBottom)
            binding.floatAdd.startAnimation(rotateOpen)

        }else{
            binding.floatRecord.visibility = View.GONE
            binding.floatTravel.visibility = View.GONE
            binding.floatSchedule.visibility = View.GONE

            binding.floatRecord.startAnimation(toBottom)
            binding.floatTravel.startAnimation(toBottom)
            binding.floatSchedule.startAnimation(toBottom)
            binding.floatAdd.startAnimation(rotateClose)
        }
        clicked = !clicked
    }

    private fun startCamera() {
        Toast.makeText(this, "Place Your Finger on the Camera", Toast.LENGTH_SHORT).show()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.backCam.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

            } catch (exc: Exception) {
                Log.e(Constraints.TAG, "Use case binding failed", exc)
            }
            val cam = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview
            )
            cam.cameraControl.enableTorch(true)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return
        binding.btnRec.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

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
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        binding.btnRec.apply {
                            binding.tvRec.visibility = View.VISIBLE
                            isEnabled = true
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            uriOut = recordEvent.outputResults.outputUri
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(Constraints.TAG, msg)
                            pathVid = convertMediaUriToPath(uriOut)
                            binding.backCam.visibility = View.INVISIBLE
                            binding.btnRec.visibility = View.GONE
                            result1 = doInBackground(pathVid).toFloat()
                            binding.tvHr.text = result1.toString()
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(
                                Constraints.TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}"
                            )
                            // STOP CAMERA Code needed
                            binding.backCam.visibility = View.INVISIBLE
                            binding.btnRec.visibility = View.GONE
                        }
                        binding.btnRec.apply {
                            binding.tvRec.visibility = View.INVISIBLE
                            isEnabled = true
                        }
                    }
                }
            }
    }

    private fun convertMediaUriToPath(uri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, proj, null, null, null)
        val columnIndex =
            cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

//    open class SlowTask
//            : AsyncTask<String, String, String?>() {
//            override
    private fun doInBackground(vararg params: String?): String {
        var m_bitmap: Bitmap? = null
        var retriever = MediaMetadataRetriever()
        var frameList = ArrayList<Bitmap>()
        try {
            retriever.setDataSource(params[0])
            var duration =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
                )
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
                        redBucket += Color.red(c) + Color.blue(c) +
                                Color.green(c)
                    }
                }
                a.add(redBucket)
            }
            val b = mutableListOf<Long>()
            for (i in 0 until a.lastIndex - 5) {
                var temp =
                    (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2)
                            + a.elementAt(
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
                    count += 1
                }
                x = b.elementAt(i.toInt())
            }
            var rate = ((count.toFloat() / 45) * 60).toInt()
            return (rate / 2).toString()
        }
    }

    private fun respRateSense(): Float {
        val bufferReader = BufferedReader(assets.open("Breathe.csv").reader())
        val csvParser = CSVParser.parse(bufferReader, CSVFormat.DEFAULT)

        val resList = mutableListOf<Double>()
        csvParser.forEach {
            it?.let {
                resList.add(it.get(0).toDouble())
            }
        }

        var previousValue = 0f
        var currentValue = 0f
        previousValue = 10f
        var k = 0
        for (i in 0..1279) {
            currentValue = kotlin.math.sqrt(
                3 * resList[i].pow(2.0)
            ).toFloat()
            if (abs(x = previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }
        val ret = (k / 45.00)
        return (ret * 30).toFloat()
    }

    private fun allPermissionsGranted() =
        Constraints.REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
}