package com.ilrcompany.forecast.model

import android.content.ContentValues
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import androidx.recyclerview.widget.RecyclerView
import com.ilrcompany.forecast.ForecastService
import com.ilrcompany.forecast.MyLocationListener
import com.ilrcompany.forecast.activity.MainActivity
import com.ilrcompany.forecast.database.*
import com.ilrcompany.forecast.recyclerview.WeatherAdapter
import com.ilrcompany.forecast.recyclerview.WeatherDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.*


class Model(private val recyclerView: RecyclerView, private val activity: MainActivity) {

    lateinit var locationManager : LocationManager
    private  var dataBaseUtil : DataBaseUtil = DataBaseUtil(activity.applicationContext)

    fun firstAction(){
        GlobalScope.launch(Dispatchers.IO) {
            activity.startService(Intent(activity, ForecastService::class.java))

            val internetUtil = InternetUtil(activity)
            if (internetUtil.request()){
                activity.runOnUiThread{
                    recyclerView.adapter = WeatherAdapter(internetUtil.getNewForecast())
                    updateCityNameAndTemperature(dataBaseUtil.getLasCity()[0], getTotalStringForUI(internetUtil.getNewForecast()))
                }
            }
            else{ // если не удалось получить ответ, то получаю прогноз для выбранного города из базы
                showOldForecast()
            }
        }
    }

    private fun showOldForecast(){
        val oldForecast = dataBaseUtil.getWeatherDaysFromDB()

        activity.runOnUiThread{
            recyclerView.adapter = WeatherAdapter(oldForecast)
            if (oldForecast.isNotEmpty())
                updateCityNameAndTemperature(dataBaseUtil.getLasCity()[0], getTotalStringForUI(oldForecast))
        }
    }

    suspend fun actionAfterNewCity(newCity : List<String>){
        var flag = true
        for (city in DataBaseUtil(activity.applicationContext).getAllCity())
            if (city.cityName == newCity[0])
                flag = false
        if (flag)                           // добавляю только новые города
            insertCity(newCity)

         setNewCurrentCity(newCity[0])

        val internetUtil = InternetUtil(activity)
        if (internetUtil.request()){
            activity.runOnUiThread{
                recyclerView.adapter = WeatherAdapter(internetUtil.getNewForecast())
                updateCityNameAndTemperature(dataBaseUtil.getLasCity()[0], getTotalStringForUI(internetUtil.getNewForecast()))
            }
        }
        else{ // если не удалось получить ответ, то получаю прогноз для выбранного города из базы
            showOldForecast()
        }
    }

    private suspend fun actionAfterCloser(newCity: String){
        setNewCurrentCity(newCity)
        val internetUtil = InternetUtil(activity)
        if (internetUtil.request()){
            activity.runOnUiThread{
                recyclerView.adapter = WeatherAdapter(internetUtil.getNewForecast())
                updateCityNameAndTemperature(dataBaseUtil.getLasCity()[0], getTotalStringForUI(internetUtil.getNewForecast()))
            }
        }
        else{ // если не удалось получить ответ, то получаю прогноз для выбранного города из базы
            showOldForecast()
        }
    }

    private fun insertCity(newCity : List<String>){
        val dataBaseWrite = CityWeatherDataBaseHelper(activity.applicationContext).writableDatabase
        dataBaseWrite.insert(WeatherDaySchema.City.NAME, null, getContentValuesOfCity(newCity[0],
            newCity[1].toInt(), newCity[2].toInt()))
    }

    private fun setNewCurrentCity(cityName : String){
        val dataBaseWrite = CityWeatherDataBaseHelper(activity.applicationContext).writableDatabase
        dataBaseWrite.delete(WeatherDaySchema.LastCity.NAME, "", null)
        val content = ContentValues()
        content.put(WeatherDaySchema.LastCity.LAST_CITY_ID, dataBaseUtil.getCityIdByName(cityName))
        dataBaseWrite.insert(WeatherDaySchema.LastCity.NAME, null, content)
    }

    fun setCloserCity(){
        locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener = MyLocationListener(this)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, locationListener)
    }

    fun workWithCurrentLocation(locationListener : MyLocationListener){
        val arr = locationListener.getLatAndLon().split("#")
        val name = getCloserCityName(arr[0].toDouble(), arr[1].toDouble())
        GlobalScope.launch(Dispatchers.IO) {
            actionAfterCloser(name)
        }
    }

    private fun getCloserCityName(lat : Double, lon : Double) : String{
        val listDistance = mutableListOf<Double>()
        val database = DataBaseUtil(activity.applicationContext)
        val listCity = database.getAllCity()

        for (city in listCity){
            listDistance.add(calculateDistance(lat, lon, city.lat.toDouble(), city.lon.toDouble()))
        }
        val min = listDistance.minOrNull()

        return listCity[listDistance.indexOf(min)].cityName
    }

    private fun calculateDistance(lat1 : Double, lon1 : Double, lat2 : Double, lon2 : Double) : Double{
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2-lat1)
        val dLon = Math.toRadians(lon2-lon1)
        val a = sin(dLat/2) * sin(dLat/2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon/2) * sin(dLon/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return (earthRadiusMeters * c)
    }

    private fun updateCityNameAndTemperature(cityName : String, mainString : String){
        activity.setCityNameAndTemperature(cityName, mainString)
    }

    private fun getTotalStringForUI(newForecast : List<WeatherDay>) =
        "${newForecast[0].imageType}, ${newForecast[0].temperature}°"
}