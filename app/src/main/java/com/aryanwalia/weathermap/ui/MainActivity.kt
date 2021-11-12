package com.aryanwalia.weathermap.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.aryanwalia.weathermap.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

class MainActivity : AppCompatActivity() {

    private val FINE_LOCATION: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val COARSE_LOCATION : String = Manifest.permission.ACCESS_COARSE_LOCATION
    private var mLocationPermissionsGranted : Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermissions()
    }

    private fun getPermissions() {
//        Toast.makeText(this, "Reaching here", Toast.LENGTH_SHORT).show()
        val permissions_array = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if(ContextCompat.checkSelfPermission(this,FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true
            }
        }else{
            requestPermissions(this, permissions_array,LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionsGranted = false
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()){
                    for(i in 0..1){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return
                        }
                    }
                    mLocationPermissionsGranted = true
                }

            }
        }
    }

}
