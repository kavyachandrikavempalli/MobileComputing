package com.example.sample

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MedRecordDb(context:Context): SQLiteOpenHelper(context,db_name,null, db_version){
    companion object{
        private const val db_version=1
        private const val db_name="symptoms.db"
        private const val Symptom_Table="table_symptom"
        private const val sym_id="ID"
        private const val Nausea_rat="Nausea"
        private const val Headache_rat="Headache"
        private const val Diarrhea_rat="Diarrhea"
        private const val Soar_Throat_rat="SoarThroat"
        private const val Fever_rat="Fever"
        private const val Muscle_Ache_rat="MuscleAche"
        private const val Loss_Taste_rat="LossOfSenseOfTaste"
        private const val Cough_rat="Cough"
        private const val Short_Breath_rat="ShortnessOfBreath"
        private const val Feel_Tired_rat="FeelingTired"
        private const val heartRate="HeartRate"
        private const val respRate="RespiratoryRate"
        private const val bloodSugar="BloodSugarLevel"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSymptoms = (
                "CREATE TABLE " + Symptom_Table + "(" +
                        sym_id + "INTEGER PRIMARY KEY," +
                        Nausea_rat + "FLOAT," +
                        Headache_rat + "FLOAT," +
                        Diarrhea_rat + "FLOAT," +
                        Soar_Throat_rat + "FLOAT," +
                        Fever_rat + "FLOAT," +
                        Muscle_Ache_rat + "FLOAT," +
                        Loss_Taste_rat + "FLOAT," +
                        Cough_rat + "FLOAT," +
                        Short_Breath_rat + "FLOAT," +
                        Feel_Tired_rat + "FLOAT," +
                        heartRate + "FLOAT," +
                        respRate + "FLOAT," +
                        bloodSugar + "FLOAT" +
                        ")"
                )
        db?.execSQL(createTableSymptoms)
    }


    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $Symptom_Table")
        onCreate(db)
    }

    fun addrecord(id: Int, rating0: Float, rating1: Float, rating2: Float, rating3: Float, rating4: Float, rating5: Float, rating6: Float, rating7: Float, rating8: Float, rating9: Float, heRate: Float, respiratoryRate: Float, bloodSugarLevel: Float) {
        val values = ContentValues()
        values.put(sym_id, id)
        values.put(Nausea_rat, rating0)
        values.put(Headache_rat, rating1)
        values.put(Diarrhea_rat, rating2)
        values.put(Soar_Throat_rat, rating3)
        values.put(Fever_rat, rating4)
        values.put(Muscle_Ache_rat, rating5)
        values.put(Loss_Taste_rat, rating6)
        values.put(Cough_rat, rating7)
        values.put(Short_Breath_rat, rating8)
        values.put(Feel_Tired_rat, rating9)
        values.put(heartRate, heRate)
        values.put(respRate, respiratoryRate)
        values.put(bloodSugar, bloodSugarLevel)

        val db = this.writableDatabase
        db.insert(Symptom_Table, null, values)
        db.close()
    }

    fun getrecords(): ArrayList<Float> {
        val recordsList = ArrayList<Float>()
        val db = this.readableDatabase

        // Define the columns you want to retrieve
        val columns = arrayOf(
            Nausea_rat, Headache_rat, Diarrhea_rat, Soar_Throat_rat, Fever_rat,
            Muscle_Ache_rat, Loss_Taste_rat, Cough_rat, Short_Breath_rat,
            Feel_Tired_rat, heartRate, respRate, bloodSugar
        )

        // Enclose column names with spaces in backticks
        val columnsWithBackticks = columns.map { "`$it`" }.toTypedArray()

        // Query the database
        val cursor = db.query(Symptom_Table, columnsWithBackticks, null, null, null, null, null)

        // Check if there are records
        if (cursor.moveToFirst()) {
            do {
                // Iterate through the columns and add values to the list
                for (column in columns) {
                    val rating = cursor.getFloat(cursor.getColumnIndexOrThrow(column))
                    recordsList.add(rating)
                }
            } while (cursor.moveToNext())
        }

        // Close the cursor and database
        cursor.close()
        db.close()
        return recordsList
    }
}
