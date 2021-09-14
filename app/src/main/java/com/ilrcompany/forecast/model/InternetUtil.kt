package com.ilrcompany.forecast.model

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ilrcompany.forecast.R
import com.ilrcompany.forecast.Response
import com.ilrcompany.forecast.database.DataBaseUtil
import com.ilrcompany.forecast.recyclerview.WeatherDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class InternetUtil(private val context : Context) {

    private val dataBaseUtil = DataBaseUtil(context.applicationContext)
    private lateinit var newForecast : List<WeatherDay>

    suspend fun request() : Boolean {
        val lastCity = dataBaseUtil.getLasCity()
        val linkWithLastCity = "https://api.openweathermap.org/data/2.5/onecall?lat=${lastCity[1]}&lon=${lastCity[2]}&exclude=current,minutely,hourly,alerts&appid=48019c0726dea99ca20d949b05f06cd1"
        val lastCityId = dataBaseUtil.getCityIdByName(lastCity[0])
        val response = getResponseFromOpenWeather(URL(linkWithLastCity))

        if (response == "null")
            return false
        else{
           newForecast = parseJson(response, lastCityId)
           dataBaseUtil.saveForecastToDB(newForecast, lastCity[0])
        }
        return true
    }

    private suspend fun getResponseFromOpenWeather(url : URL) : String{
        val request : String
        return try{
            request = httpRequest(url)
            request
        } catch (e : Exception){
            if (context is Activity && (!context.isFinishing)){
                showInternetErrorMessage()
            }
            Log.d("Cacheted message",e.toString())
            "null"
        }
    }

    private suspend fun httpRequest(url: URL): String {
        val connection = url.openConnection() as HttpURLConnection
        return withContext(Dispatchers.IO){
            connection.inputStream.bufferedReader().use { it.readText() }
        }
    }

    private fun showInternetErrorMessage(){
        if (context is Activity){
            context.runOnUiThread { Toast.makeText(context, R.string.problem, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun parseJson(string : String, lastCityId : Int) : List<WeatherDay>{
        val response = Response(string, lastCityId)
        return response.getWeatherDayList()
    }

    fun getNewForecast() = newForecast
}