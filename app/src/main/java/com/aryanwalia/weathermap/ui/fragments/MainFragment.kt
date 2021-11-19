package com.aryanwalia.weathermap.ui.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aryanwalia.weathermap.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


class MainFragment : Fragment(){

    private val DEFAULT_ZOOM : Float = 15f
    private lateinit var search_btn:ImageButton
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var location_name : TextView

    private lateinit var temp_reading : TextView
    private lateinit var wind_reading : TextView
    private lateinit var humid_reading : TextView
    private lateinit var visibility_reading : TextView
    private lateinit var feels_what : TextView
    private lateinit var desc : TextView
    private lateinit var itemList : ListView

    private var tempe : Double = 0.0
    private var humid : Double = 0.0
    private var wSpeed : Double = 0.0
    private var visible : Int = 0
    private var feelsLike : String = ""
    private lateinit var windObject : JSONObject


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_main, container, false)

        search_btn = view.findViewById(R.id.search_button_main_fragment)
        location_name = view.findViewById(R.id.location_name)


        temp_reading = view.findViewById(R.id.temp_reading)
        wind_reading = view.findViewById(R.id.wind_reading)
        humid_reading = view.findViewById(R.id.humid_reading)
        visibility_reading = view.findViewById(R.id.visible_reading)
        feels_what = view.findViewById(R.id.feel)
        desc = view.findViewById(R.id.desc)
        itemList = view.findViewById(R.id.content_list)

        getDeviceLocation()

        search_btn.setOnClickListener { vi:View ->
            Navigation.findNavController(vi).navigate(R.id.action_mainFragment_to_mapFragment)
        }

        return view
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(),"Could not connect you",Toast.LENGTH_LONG).show()
            return
        }
        try {
            val mlocation = mFusedLocationProviderClient.lastLocation.addOnCompleteListener{ task->
                if(task.isSuccessful){
//                    Toast.makeText(requireContext(),"connected",Toast.LENGTH_LONG).show()
                    geoLocate(task.result.latitude,task.result.longitude)

                }else{
                    Toast.makeText(requireContext(),"Could not connect you",Toast.LENGTH_LONG).show()
                }
            }
        }catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun geoLocate(lat:Double,long:Double){
        val geoCoder = Geocoder(activity)
        val address : List<Address>
        try{
            address = geoCoder.getFromLocation(lat,long,1)
            if(address.isNotEmpty()){
                location_name.text = address[0].locality.toString()
                getDataFromApi(location_name.text.toString().trim())
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun getDataFromApi(city:String) {
        val address = "http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=aa741e271a6c4e998caeab1263c063b0"
        val requestQueue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(
            Request.Method.POST, address, { response ->
                //Log.d("response",response);
                try {
                    val jsonResponse = JSONObject(response)
                    val jsonArray = jsonResponse.getJSONArray("weather")
                    val jsonObjectWeather = jsonArray.getJSONObject(0)
                    val description = jsonObjectWeather.getString("description")
                    val des = description.split(" ").toTypedArray()
                    val jsonObjectMain = jsonResponse.getJSONObject("main")

                    feelsLike = jsonObjectMain.getString("feels_like")
                    tempe = jsonObjectMain.getDouble("temp")
                    humid = jsonObjectMain.getDouble("humidity")
                    windObject = jsonResponse.getJSONObject("wind")
                    wSpeed = windObject.getDouble("speed")



                    val tValue : String = tempe.toString()+" "+"\u2103"
                    temp_reading.setText(tValue)

                    val hValue : String = humid.toString()+" "+"%"
                    humid_reading.setText(hValue)

                    val wValue : String = wSpeed.toString()+" "+"m/s"
                    wind_reading.setText(wValue)

                    val feel: String = "Feels Like $feelsLike"
                    feels_what.setText(feel)

                    desc.setText(description)

                    visible = jsonResponse.getInt("visibility")
                    visibility_reading.setText(visible.toString())

                    val sea_level = jsonObjectMain.getDouble("sea_level")

                    val windObject = jsonResponse.getJSONObject("wind")


                    val jsonCordObject = jsonResponse.getJSONObject("coord")
                    val lon = jsonCordObject.getDouble("lon")
                    val lat = jsonCordObject.getDouble("lat")

                    populateList(description, tempe, humid, wSpeed,lat,lon,sea_level)



                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        ) { error ->
            Toast.makeText(
                context,
                "An Error occurred $error",
                Toast.LENGTH_SHORT
            ).show()
        }
        requestQueue.add(stringRequest)
    }

    private fun populateList(description: String, tempe: Double, humid: Double, wSpeed: Double,lat: Double,long: Double,seaLevel:Double) {
        val cities = arrayOf(
            "Description: $description",
            "Temperature: $tempe℃",
            "Humidity: $humid %",
            "Wind Speed: " + wSpeed + "m/s",
            "Latitude: " +lat ,
            "Longitude: " + long ,
            "Sea Level: " + seaLevel
        )

        val arrayAdapter = context?.let { ArrayAdapter(it, R.layout.row, cities) }
        itemList.adapter = arrayAdapter
    }


}