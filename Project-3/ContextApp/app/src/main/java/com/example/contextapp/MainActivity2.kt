package com.example.contextapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.contextapp.databinding.ActivityMainBinding
import com.example.contextapp.databinding.ActivityMain2Binding
import java.util.Dictionary

class MainActivity2 : AppCompatActivity() {
    var position: Int = 0
    val symptomHash: HashMap<Int, Float> = HashMap()
    private lateinit var Binding1: ActivityMain2Binding
    val symptoms = arrayOf("Nausea", "Headache", "diarrhea", "Soar Throat", "Fever", "Muscle Ache", "Loss of Smell or Taste", "Cough", "Shortness of Breath", "Feeling tired")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Binding1 = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(Binding1.root)
        Log.e(this.toString(),"Entered Intent: Three")
        val heartrate = intent.getStringExtra("HeartRate")
        val resprate = intent.getStringExtra("RespiratoryRate")
        val spinner = findViewById<Spinner>(R.id.Symptom_select)
        val arrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, symptoms)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                position = pos
                Toast.makeText(applicationContext, "Please Enter rating", Toast.LENGTH_SHORT)
                    .show()
                if (symptomHash.containsKey(pos)) {
                    Binding1.ratingBar1.rating = symptomHash[pos]!!
                } else {
                    Binding1.ratingBar1.rating = 0f
                }
                Binding1.ratingBar1.stepSize = 0.5f
            }


            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        Binding1.ratingBar1.setOnRatingBarChangeListener { _, rating, _ ->
            symptomHash[position] = rating
        }
        Binding1.btnSymptoms.setOnClickListener {
            var j=0
            while(j<=9){
                if(symptomHash[j]==null){
                    symptomHash[j]=0.0f
                }
                j++
            }
            val db = database_connection(this)
            val pkey=1
            db.store_db_data(pkey, symptomHash[0]!!, symptomHash[1]!!, symptomHash[2]!!, symptomHash[3]!!, symptomHash[4]!!, symptomHash[5]!!, symptomHash[6]!!, symptomHash[7]!!, symptomHash[8]!!, symptomHash[9]!!, heartrate!!.toFloat(), resprate!!.toFloat())
            Toast.makeText(this, "Values added to database: ${symptomHash[0]} ${symptomHash[1]} ${symptomHash[2]} ${symptomHash[3]} ${symptomHash[4]} ${symptomHash[5]} ${symptomHash[6]} ${symptomHash[7]} ${symptomHash[8]} ${symptomHash[9]} $heartrate $resprate", Toast.LENGTH_SHORT).show()
            Log.e(this.toString(),"Reached here 1")
        }
        Binding1.nextBtn.setOnClickListener {
            Log.e("check: ","Reached here 2")
            val intent1 = Intent(this, MainActivity3::class.java)
            startActivity(intent1)
        }
    }
}