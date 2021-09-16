package com.ilrcompany.forecast

import android.location.Location
import android.location.LocationListener
import android.util.Log
import android.widget.Toast
import com.ilrcompany.forecast.model.Model

class MyLocationListener(private val model : Model) : LocationListener {
    private var lat : Double? = null
    private var lon : Double? = null
    private var isFirstTime = false

    override fun onLocationChanged(location: Location) {
        lat = location.latitude
        lon = location.longitude
        isFirstTime = true
        Log.d("LocationListener", "$lat  $lon")

        if (isFirstTime){
            model.locationManager.removeUpdates(this)
            model.workWithCurrentLocation(this)
            model.activity.hideProgressBarAndActivateButton()
        }
    }

    fun getLatAndLon() : String{
        return if(lat != null && lon != null)
            "$lat#$lon"
        else
            ""
    }

    override fun onProviderDisabled(provider: String) {
        model.activity.runOnUiThread{
            model.activity.hideProgressBarAndActivateButton()
            Toast.makeText(model.activity, model.activity.getString(R.string.gps_error),
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProviderEnabled(provider: String) {

    }
}