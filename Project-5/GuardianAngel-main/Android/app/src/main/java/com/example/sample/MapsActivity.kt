package com.example.sample

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.sample.R.string
import com.example.sample.R.string.google_maps_api_key
import com.example.sample.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding3: ActivityMapsBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var autocompleteStartFragment: AutocompleteSupportFragment
    private lateinit var autocompleteEndFragment: AutocompleteSupportFragment
    private lateinit var startCord: LatLng
    private lateinit var endCord: LatLng
    private lateinit var startAddress: String
    private lateinit var endAddress: String

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding3 = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding3.root)

        val apiKey = getString(google_maps_api_key)
        Places.initialize(applicationContext, apiKey)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        autocompleteStartFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_start) as AutocompleteSupportFragment
        autocompleteEndFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment_end) as AutocompleteSupportFragment
        autocompleteStartFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteEndFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        autocompleteStartFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@MapsActivity, "Error in Search", Toast.LENGTH_SHORT).show()}
            override fun onPlaceSelected(p0: Place) {
                startAddress = p0.address!!
                startCord = addressToCord(startAddress)!!
                zoomOnMap(startCord)
                addMarker(startCord, "Start")
            }
        })

        autocompleteEndFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@MapsActivity, "Error in Search", Toast.LENGTH_SHORT).show()}
            override fun onPlaceSelected(p1: Place) {
                binding3.resultTv.setText(string.calc)
                endAddress = p1.address!!
                endCord = addressToCord(endAddress)!!
                zoomOnMap(endCord)
                addMarker(endCord, "End")

                startAddress = startAddress.replace(", ", "%2C")
                endAddress = endAddress.replace(", ","%2C")
                startAddress = startAddress.replace(" ", "%2C")
                endAddress = endAddress.replace(" ","%2C")

//                Log.e("response:", "StartAddress: $startAddress ,EndAddress: $endAddress")

                GlobalScope.launch(Dispatchers.Main) {
                    val diff = getDistanceMatrixAsync(startAddress, endAddress, apiKey)
                    if (diff > 5) {
                        Toast.makeText(this@MapsActivity, string.poorRoad, Toast.LENGTH_SHORT)
                            .show()
                        findViewById<TextView>(R.id.resultTv).text = getString(string.poorRoad)
                    } else {
                        Toast.makeText(this@MapsActivity, string.normalRoad, Toast.LENGTH_SHORT)
                            .show()
                        findViewById<TextView>(R.id.resultTv).text = getString(string.normalRoad)
                    }
                }
            }
        })
    }

    @Suppress("DEPRECATION")
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

    private suspend fun getDistanceMatrixAsync(origin: String, destination: String, apiKey: String): Int {
        return withContext(Dispatchers.IO) {
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
                val rows = jsonResponse.getJSONArray("rows")
                if (rows.length() > 0) {
                    val elements = rows.getJSONObject(0).getJSONArray("elements")
                    if (elements.length() > 0) {
//                        val distance = elements.getJSONObject(0).getJSONObject("distance")
                        val duration = elements.getJSONObject(0).getJSONObject("duration")
                        val durationInTraffic = elements.getJSONObject(0).getJSONObject("duration_in_traffic")
                        val durationValue = duration.getInt("value")
                        val durationInTrafficValue = durationInTraffic.getInt("value")
                        return@withContext (durationInTrafficValue - durationValue) / 60
                    } else {
                        Log.e("response: ", "No elements found in the response.")
                    }
                } else {
                    Log.e("response: ", "No rows found in the response.")
                }
            } else {
                Log.e("response: ", "Error: ${response.code}")
            }
            return@withContext -1 // Return a default value if an error occurs
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }
}