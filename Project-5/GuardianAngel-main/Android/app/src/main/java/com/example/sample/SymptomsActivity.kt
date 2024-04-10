package com.example.sample

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.example.sample.databinding.ActivitySymptomsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Calendar

class SymptomsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    var adapterPosition: Int = 0
    val spinMap: HashMap<Int, Float> = HashMap()
    private lateinit var binding1: ActivitySymptomsBinding

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
        binding1 = ActivitySymptomsBinding.inflate(layoutInflater)
        setContentView(binding1.root)
        val heartRate = intent.getStringExtra("HeartRate")
        val respRate = intent.getStringExtra("RespiratoryRate")
        val bloodSugar = intent.getStringExtra("BloodSugarLevel")

        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Constraints.symptoms)
        binding1.spinner.adapter = arrayAdapter

        binding1.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                adapterPosition = p2
                if (spinMap.containsKey(p2)) {
                    binding1.ratingBar.rating = spinMap[p2]!!
                } else {
                    binding1.ratingBar.rating = 0f
                }
                binding1.ratingBar.stepSize = 1f
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding1.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            spinMap[adapterPosition] = rating
        }

        binding1.btnUp2.setOnClickListener {
//            var i = 0
//            while (i < 11) {
//                if (spinMap[i] == null) spinMap[i] = 0.0f
//                i++
//            }
            val db = MedRecordDb(context = this)
            val id = 1

            val spinMap = (0 until 11).map { spinMap[it] ?: 0.0f }.toFloatArray()

            db.addrecord(1,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f)
//            db.addrecord(
//                id,
//                spinMap[0],
//                spinMap[1],
//                spinMap[2],
//                spinMap[3],
//                spinMap[4],
//                spinMap[5],
//                spinMap[6],
//                spinMap[7],
//                spinMap[8],
//                spinMap[9],
//                heartRate!!.toFloat(),
//                respRate!!.toFloat(),
//                bloodSugar!!.toFloat()
//            )

            Toast.makeText(
                this,
                "Values added to database: ${spinMap[0]} ${spinMap[1]} ${spinMap[2]} ${spinMap[3]} ${spinMap[4]} ${spinMap[5]} ${spinMap[6]} ${spinMap[7]} ${spinMap[8]} ${spinMap[9]} $heartRate $respRate $bloodSugar",
                Toast.LENGTH_SHORT
            ).show()

            Log.e("RESPONSE1","Values added to database: ${spinMap[0]} ${spinMap[1]} ${spinMap[2]} ${spinMap[3]} ${spinMap[4]} ${spinMap[5]} ${spinMap[6]} ${spinMap[7]} ${spinMap[8]} ${spinMap[9]} $heartRate $respRate $bloodSugar")
        }

        binding1.mapsBtn.setOnClickListener{
            val intent1 = Intent(this, MapsActivity::class.java)
            startActivity(intent1)
        }

        binding1.floatAdd.setOnClickListener{
            onAddClicked()
        }

        binding1.floatSchedule.setOnClickListener{
            addSchedule()
        }

        binding1.floatTravel.setOnClickListener{
            addTravel()
        }

        binding1.floatRecord.setOnClickListener{
            addRecord()
        }
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
        val sugg: TextView = dialog.findViewById(R.id.tvSugg)

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
            client.newCall(request).enqueue(object : Callback {
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
            binding1.floatRecord.visibility = View.VISIBLE
            binding1.floatTravel.visibility = View.VISIBLE
            binding1.floatSchedule.visibility = View.VISIBLE

            binding1.floatRecord.startAnimation(fromBottom)
            binding1.floatTravel.startAnimation(fromBottom)
            binding1.floatSchedule.startAnimation(fromBottom)
            binding1.floatAdd.startAnimation(rotateOpen)

        }else{
            binding1.floatRecord.visibility = View.GONE
            binding1.floatTravel.visibility = View.GONE
            binding1.floatSchedule.visibility = View.GONE

            binding1.floatRecord.startAnimation(toBottom)
            binding1.floatTravel.startAnimation(toBottom)
            binding1.floatSchedule.startAnimation(toBottom)
            binding1.floatAdd.startAnimation(rotateClose)
        }
        clicked = !clicked
    }
}
