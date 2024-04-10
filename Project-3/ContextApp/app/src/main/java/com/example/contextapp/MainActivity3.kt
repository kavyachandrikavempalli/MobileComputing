package com.example.contextapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.contextapp.databinding.ActivityMain3Binding
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.IOException
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.contextapp.R.string.calc
import com.example.contextapp.R.string.from
import com.example.contextapp.R.string.google_maps_api_key
import com.example.contextapp.R.string.normalRoad
import com.example.contextapp.R.string.poorRoad
import com.example.contextapp.R.string.toLoc
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity3 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var Binding2: ActivityMain3Binding

    private var mGoogleMap:GoogleMap? = null
    private lateinit var autocompleteStartFragment: AutocompleteSupportFragment
    private lateinit var autocompleteEndFragment: AutocompleteSupportFragment
    private lateinit var startCord: LatLng
    private lateinit var startAddress: String
    private lateinit var endAddress: String
    private lateinit var endCord: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Binding2 = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(Binding2.root)

        val apiKey = getString(google_maps_api_key)
        Places.initialize(applicationContext, apiKey)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        autocompleteStartFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_start) as AutocompleteSupportFragment
        autocompleteEndFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_end) as AutocompleteSupportFragment
        autocompleteStartFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
        autocompleteEndFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
        autocompleteStartFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {Toast.makeText(this@MainActivity3, "Error in Search", Toast.LENGTH_SHORT).show()}
            override fun onPlaceSelected(p0: Place) {
                //val address = p0.address
                //val id = p0.id
                //val latLng = p0.latLng
                startAddress = p0.address!!
                startCord = addressToCord(startAddress)!!
                zoomOnMap(startCord)
                addMarker(startCord, "Start")
                findViewById<TextView>(R.id.startTv).text = getString(toLoc)
                findViewById<View>(R.id.autocomplete_fragment_end).visibility = View.VISIBLE
                findViewById<View>(R.id.autocomplete_fragment_start).visibility = View.INVISIBLE
            }
        })

        autocompleteEndFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {Toast.makeText(this@MainActivity3, "Error in Search", Toast.LENGTH_SHORT).show()}
            override fun onPlaceSelected(p1: Place) {
                findViewById<TextView>(R.id.startTv).text = getString(calc)
                //val address = p1.address
                //val id = p0.id
                //val latLng = p0.latLng
                endAddress = p1.address!!
                endCord = addressToCord(endAddress)!!
                zoomOnMap(endCord)
                addMarker(endCord, "End")
                findViewById<View>(R.id.startCV).visibility = View.GONE
                startAddress = startAddress.replace(", ", "%2C")
                endAddress = endAddress.replace(", ","%2C")
                startAddress = startAddress.replace(" ", "%2C")
                endAddress = endAddress.replace(" ","%2C")
                Log.e("response:", "StartAddress: $startAddress ,EndAddress: $endAddress")
                val diff = getDistanceMatrix(startAddress,endAddress,apiKey)
                if (diff > 5){
                    Toast.makeText(this@MainActivity3, poorRoad,Toast.LENGTH_SHORT).show()
                    findViewById<TextView>(R.id.resultTv).text = getString(poorRoad)
                } else {
                    Toast.makeText(this@MainActivity3, normalRoad,Toast.LENGTH_SHORT).show()
                    findViewById<TextView>(R.id.resultTv).text = getString(normalRoad)
                }
                findViewById<TextView>(R.id.startTv).text = getString(from)
                findViewById<View>(R.id.autocomplete_fragment_start).visibility = View.VISIBLE
                findViewById<View>(R.id.autocomplete_fragment_end).visibility = View.INVISIBLE
            }
        })
    }

    private fun addressToCord(address: String): LatLng? {
        val geocoder = Geocoder(this)
        try {
            val results: MutableList<Address>? = geocoder.getFromLocationName(address, 1)
            if (results != null) {
                val location = results[0]
                return LatLng(location.latitude, location.longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun zoomOnMap(latLng:LatLng){
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng,12f)
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    private fun addMarker(latLng: LatLng,flag:String){
        mGoogleMap?.addMarker((MarkerOptions().position(latLng).title(flag)))
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getDistanceMatrix(origin: String, destination: String, apiKey: String): Int {
        var diff = 0
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                    "?departure_time=now" +
                    "&origins=$origin" +
                    "&destinations=$destination" +
                    "&key=$apiKey"

            val request = Request.Builder().url(url).build()
            Log.e("response:", "request: $request")

            val response = client.newCall(request).execute()
            Log.e("response:", "response: $response")



            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body!!.string())

                Log.e("response:", "This:$jsonResponse")
                val rows = jsonResponse.getJSONArray("rows")
                if (rows.length() > 0) {
                    val elements = rows.getJSONObject(0).getJSONArray("elements")
                    if (elements.length() > 0) {
                        val distance = elements.getJSONObject(0).getJSONObject("distance")
                        val duration = elements.getJSONObject(0).getJSONObject("duration")
                        val durationInTraffic =
                            elements.getJSONObject(0).getJSONObject("duration_in_traffic")

                        val distanceText = distance.getString("text")
                        val durationText = duration.getString("text")
                        val durationInTrafficText = durationInTraffic.getString("text")
                        diff = (duration.getInt("value") - durationInTraffic.getInt("value")) / 60

                        Log.e("response: ", "Distance: $distanceText")
                        Log.e("response: ", "Duration: $durationText")
                        Log.e("response: ", "Duration In Traffic: $durationInTrafficText")

                    } else {
                        Log.e("response: ", "No elements found in the response.")
                    }
                } else {
                    Log.e("response: ", "No rows found in the response.")
                }
            } else {
                Log.e("response: ", "Error: ${response.code}")
            }

        }
        return diff
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }
}