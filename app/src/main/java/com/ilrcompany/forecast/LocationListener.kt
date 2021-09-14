package com.ilrcompany.forecast

import android.location.Location
import android.location.LocationListener
import com.ilrcompany.forecast.model.Model

class MyLocationListener(private val model : Model) : LocationListener {
    private var lat : Double? = null
    private var lon : Double? = null
    private var isFirstTime = false

    override fun onLocationChanged(location: Location) {
        lat = location.latitude
        lon = location.longitude
        isFirstTime = true

        if (isFirstTime){
            model.locationManager.removeUpdates(this)
            model.workWithCurrentLocation(this)
        }
    }

    fun getLatAndLon() : String{
        return if(lat != null && lon != null)
            "$lat#$lon"
        else
            ""
    }
}