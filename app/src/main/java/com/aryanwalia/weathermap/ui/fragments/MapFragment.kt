package com.aryanwalia.weathermap.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aryanwalia.weathermap.ui.CustomInfoWindowAdapter
import com.aryanwalia.weathermap.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.NullPointerException

class MapFragment : Fragment(),OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap : GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val DEFAULT_ZOOM : Float = 15f

    private var location_locality : String = ""
    private var location_sub_locality : String = ""
    private var location_country : String = ""


    private lateinit var my_location_button : Button
    private lateinit var get_weather : ImageButton
    private lateinit var get_info : ImageButton
    private lateinit var get_back : ImageButton
    private lateinit var get_city : EditText
    private lateinit var mMarker: Marker



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        my_location_button = view.findViewById(R.id.my_location_button)
        get_weather = view.findViewById(R.id.check_button)
        get_city = view.findViewById(R.id.search_city_field)
        get_info = view.findViewById(R.id.info_button)
        get_back = view.findViewById(R.id.back_button)

        my_location_button.setOnClickListener{ vi:View ->
            mMap.clear()
            centerMapAndSendMyLocation()
            Navigation.findNavController(vi).navigate(R.id.action_mapFragment_to_mainFragment)
        }

        get_weather.setOnClickListener { vi:View ->
            val city = get_city.text.toString().trim()
            if(city.isNotEmpty()){
                getDataFromApi(city)
            }else{
                Toast.makeText(context,"Enter city name",Toast.LENGTH_SHORT).show()
            }
        }

        get_info.setOnClickListener { vi:View ->
             try {
                 if(mMarker.isInfoWindowShown){
                     mMarker.hideInfoWindow()
                 }else{
                     mMarker.showInfoWindow()
                 }
             }catch (e:NullPointerException){
                 e.printStackTrace()
             }
        }

        get_back.setOnClickListener { vi: View ->
            Navigation.findNavController(vi).navigate(R.id.action_mapFragment_to_mainFragment)
        }
        centerOnMyLocation()
        return view
    }

//    private fun geoLocateUsingCityName(city: String) {
//        val geoCoder = Geocoder(activity)
//        val address : List<Address>
//        try{
//            address = geoCoder.getFromLocationName(city,1)
//            if(address.isNotEmpty()){
//                geoLocate(address[0].latitude,address[0].longitude)
//                moveCameraForCityName(LatLng(address[0].latitude,address[0].longitude))
//            }
//        }catch (e: IOException){
//           Toast.makeText(context,"Map Error",Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun centerMapAndSendMyLocation() {
        try {
            val mlocation = mFusedLocationProviderClient.lastLocation.addOnCompleteListener{ task->
                if(task.isSuccessful){
                    val result = task.result
                    geoLocate(result.latitude,result.longitude)
                    moveCamera(LatLng(result.latitude,result.longitude))
                }else{
                    Toast.makeText(requireContext(),"Could not connect you", Toast.LENGTH_LONG).show()
                }
            }
        }catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun centerOnMyLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(),"Could not connect you", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val mlocation = mFusedLocationProviderClient.lastLocation
            mlocation.addOnCompleteListener{ task->
                if(task.isSuccessful){
                    val result = task.result
                    geoLocate(result.latitude,result.longitude)
                    moveCamera(LatLng(result.latitude,result.longitude))
                }else{
                    Toast.makeText(requireContext(),"Could not connect you", Toast.LENGTH_LONG).show()
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
            location_locality=""
            location_sub_locality=""
            location_country=""
            if(address.isNotEmpty()){
                location_locality = address[0].locality.toString()
                location_sub_locality = address[0].subLocality.toString()
                location_country = address[0].countryName.toString()
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun moveCamera(latLng: LatLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM))
        mMap.clear()
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireActivity()))
        val markerOptions = MarkerOptions()
            .position(latLng)
            .draggable(true)
//            .title("\t\t\t"+location_sub_locality+" "+location_locality+" "+location_country+"\t\t\t"+"\n\t\t\t\t"+
//                    "Temperature = $tempe \u2103"+"\n\t\t\t\t"+"Humidity = $humid %"+"\n\t\t\t\t"+"Wind Speed = $wSpeed km/h"+"\n\t\t\t\t"+"Visibility = $visible"+"\n\t\t\t\t"+"Feels like = $feelsLike"+"\n\t\t\t\t"+"Description = $description")
        mMarker = mMap.addMarker(markerOptions)
        if(location_locality!="") {
            Toast.makeText(context,location_locality,Toast.LENGTH_SHORT).show()
            getDataFromApi(location_locality)
        }
        else if(location_sub_locality!="") {
            Toast.makeText(context,location_sub_locality,Toast.LENGTH_SHORT).show()
            getDataFromApi(location_sub_locality)
        }
        else{
            Toast.makeText(context,"Could not find\nEnter City Name",Toast.LENGTH_SHORT).show()
        }
        closeKeyBoard()
    }

    private fun moveCameraForCityName(latLng: LatLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM))
        mMap.clear()
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireActivity()))
        val markerOptions = MarkerOptions()
            .position(latLng)
            .draggable(true)
//            .title("\t\t\t"+location_sub_locality+" "+location_locality+" "+location_country+"\t\t\t"+"\n\t\t\t\t"+
//                    "Temperature = $tempe \u2103"+"\n\t\t\t\t"+"Humidity = $humid %"+"\n\t\t\t\t"+"Wind Speed = $wSpeed km/h"+"\n\t\t\t\t"+"Visibility = $visible"+"\n\t\t\t\t"+"Feels like = $feelsLike"+"\n\t\t\t\t"+"Description = $description")
        mMarker = mMap.addMarker(markerOptions)
        closeKeyBoard()
    }

    private fun closeKeyBoard() {
        val view = this.view
        if (view != null) {
            val imm: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.applicationWindowToken,0)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled=true
        mMap.setOnMapLongClickListener(this)
        mMap.isTrafficEnabled=true
        mMap.isBuildingsEnabled=true

    }

    override fun onMapLongClick(p0: LatLng) {
        try {
            val lat = p0.latitude
            val long = p0.longitude
            geoLocate(lat,long)
            moveCamera(LatLng(lat,long))
        }catch (e:IOException){
            Toast.makeText(context,"Map Click Error",Toast.LENGTH_SHORT).show()
        }

    }

    private fun getDataFromApi(city:String) {
         val address = "http://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=aa741e271a6c4e998caeab1263c063b0"
         val requestQueue = Volley.newRequestQueue(context)
         val stringRequest = StringRequest(
            Request.Method.POST, address, { response ->
                //Log.d("response",response)
                try {
                    val jsonResponse = JSONObject(response)
                    val jsonArray = jsonResponse.getJSONArray("weather")
                    val jsonObjectWeather = jsonArray.getJSONObject(0)
                    val description = jsonObjectWeather.getString("description")
                    val des = description.split(" ").toTypedArray()
                    val jsonObjectMain = jsonResponse.getJSONObject("main")
                    val latLongJSONObject = jsonResponse.getJSONObject("coord")


                    val lat_h = latLongJSONObject.getDouble("lat")
                    val long_h = latLongJSONObject.getDouble("lon")

//                    var location_loc=""
//                    var location_sub_loc=""
//                    var location_coun=""
//
//                    val geoCoder = Geocoder(activity)
//
//                    val addresses = geoCoder.getFromLocation(lat_h,long_h,1)
//                    location_locality=""
//                    location_sub_locality=""
//                    location_country=""
//                    if(addresses.isNotEmpty()){
//                        location_locality = addresses[0].locality.toString()
//                        location_sub_locality = addresses[0].subLocality.toString()
//                        location_country = addresses[0].countryName.toString()
//                    }

//                    geoLocate(lat_h,long_h)
//                    moveCameraForCityName(LatLng(lat_h,long_h))


                    val tempe = jsonObjectMain.getDouble("temp")
                    val humid = jsonObjectMain.getDouble("humidity")
                    val windObject = jsonResponse.getJSONObject("wind")
                    val wSpeed = windObject.getDouble("speed")
                    val visible = jsonResponse.getInt("visibility")
                    val feelsLike = jsonObjectMain.getString("feels_like")

                    val infoWindow = "\n\t\t\t\t\t\t"+location_sub_locality+" "+location_locality+" "+location_country+
                            "\t\t\t\t\t\t"+"\n\t\t\t\t\t\t\t"+ "Temperature = $tempe \u2103"+"\n\t\t\t\t\t\t\t"+"Humidity = $humid %"+
                            "\n\t\t\t\t\t\t\t"+"Wind Speed = $wSpeed km/h"+"\n\t\t\t\t\t\t\t"+"Visibility = $visible"+
                            "\n\t\t\t\t\t\t\t"+"Feels like = $feelsLike"+"\n\t\t\t\t\t\t\t"+"Description = $description"
                    moveCameraForCityName(LatLng(lat_h,long_h))
                    mMarker.title = infoWindow


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

}