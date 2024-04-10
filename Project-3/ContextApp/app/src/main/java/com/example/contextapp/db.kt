package com.example.contextapp

import android.content.ContentValues
import android.content.Context;
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

public class database_connection(context:Context): SQLiteOpenHelper(context,db_name,null, db_version){
    companion object{
        private const val db_version=1
        private const val db_name="symptoms.db"
        private const val Symptom_Table="table_symptom"
        private const val Symptom_id="ID"
        private const val Nausea="Nausea"
        private const val Headache="Headache"
        private const val Diarrhea="Diarrhea"
        private const val Soar_Throat="Soar Throat"
        private const val Fever="Fever"
        private const val Muscle_Ache="Muscle Ache"
        private const val Loss_Taste="Loss of Sense of Taste"
        private const val Cough="Cough"
        private const val Short_Breath="Shortness of Breath"
        private const val Feel_Tired="Feeling Tired"
        private const val Heartrate="HeartRate"
        private const val Respiratoryrate="RespiratoryRate"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSymptoms= ("CREATE TABLE "+ Symptom_Table + "(" +Symptom_id + " INTEGER PRIMARY KEY,"+ Nausea + " FLOAT DEFAULT 0 ," +
                Headache + " FLOAT DEFAULT 0 ," + Diarrhea + " FLOAT DEFAULT 0 ," + Soar_Throat + " FLOAT DEFAULT 0 ," +
                Fever + " FLOAT DEFAULT 0 ," + Muscle_Ache + " FLOAT DEFAULT 0 ," + Loss_Taste + " FLOAT DEFAULT 0 ," +
                Cough + " FLOAT DEFAULT 0 ," + Short_Breath + " FLOAT DEFAULT 0 ," + Feel_Tired + " FLOAT DEFAULT 0 ," +
                Heartrate + " FLOAT DEFAULT 0," + Respiratoryrate + " FLOAT DEFAULT 0" + ")")
        db?.execSQL(createTableSymptoms)
    }

    override fun onUpgrade(db: SQLiteDatabase?, before_update: Int, after_update: Int) {
        val db=this.writableDatabase
        db!!.execSQL("UPDATE "+ Symptom_Table + " SET "+ Heartrate+" = "+" heartrate "+ Respiratoryrate+" = "+" respiratoryrate ")
        onCreate(db)
    }

    fun store_db_data(ID : Int, r0: Float, r1: Float, r2: Float, r3: Float, r4: Float, r5: Float, r6: Float, r7: Float, r8: Float, r9: Float, RespiratoryRate : Float, HeartRate : Float){

        val values = ContentValues()
        values.put(Symptom_id, ID)
        values.put(Nausea, r0)
        values.put(Headache, r1)
        values.put(Diarrhea, r2)
        values.put(Soar_Throat, r3)
        values.put(Fever, r4)
        values.put(Muscle_Ache, r5)
        values.put(Loss_Taste, r6)
        values.put(Cough, r7)
        values.put(Short_Breath, r8)
        values.put(Feel_Tired, r9)
        values.put(Respiratoryrate,RespiratoryRate)
        values.put(Heartrate,HeartRate)
        val db = this.writableDatabase
        db.insert(Symptom_Table, null, values)
        db.close()
    }
}