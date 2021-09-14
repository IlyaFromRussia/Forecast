package com.ilrcompany.forecast.database

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.ilrcompany.forecast.recyclerview.WeatherDay
import java.lang.Exception

class DataBaseUtil(private val applicationContext : Context) {

    fun getAllCity() : List<City> {
        val cities = mutableListOf<City>()
        val dataBase = CityWeatherDataBaseHelper(applicationContext).readableDatabase
        val cursor = dataBase.query(
            WeatherDaySchema.City.NAME, arrayOf(
                WeatherDaySchema.City.CITY_NAME,
                WeatherDaySchema.City.ID_CITY, WeatherDaySchema.City.LAT, WeatherDaySchema.City.LON), null,
            null,null, null,null)

        cursor.moveToFirst()

        while (!cursor.isAfterLast){
            val cityName = cursor.getString(cursor.getColumnIndex(WeatherDaySchema.City.CITY_NAME))
            val idCity = cursor.getInt(cursor.getColumnIndex(WeatherDaySchema.City.ID_CITY))
            val lat = cursor.getInt(cursor.getColumnIndex(WeatherDaySchema.City.LAT))
            val lon = cursor.getInt(cursor.getColumnIndex(WeatherDaySchema.City.LON))

            cities.add(City(idCity, cityName, lat, lon))
            cursor.moveToNext()
        }

         cursor.close()
        return cities
    }

    fun getCityLatAndLonByName(cityName : String) : String{
       val dataBase = CityWeatherDataBaseHelper(applicationContext).readableDatabase
       val cursor = dataBase.query(WeatherDaySchema.City.NAME, arrayOf(WeatherDaySchema.City.ID_CITY,
           WeatherDaySchema.City.CITY_NAME, WeatherDaySchema.City.LAT, WeatherDaySchema.City.LON)
           ,"${WeatherDaySchema.City.CITY_NAME} = '$cityName'", null, null,null,
           null,null
       )
       cursor.moveToFirst()

       val lat = cursor.getInt(cursor.getColumnIndex("lat"))
       val lon = cursor.getInt(cursor.getColumnIndex("lon"))

       cursor.close()
       return "$lat#$lon"
   }

    fun getLasCity() : List<String>{
        val dataBaseRead = CityWeatherDataBaseHelper(applicationContext).readableDatabase
        val lastCityId = dataBaseRead.query(WeatherDaySchema.LastCity.NAME, arrayOf(WeatherDaySchema.LastCity.LAST_CITY_ID),
            "", null, null,null, null,null)
        lastCityId.moveToFirst()
        val lasCityCursor = dataBaseRead.query(WeatherDaySchema.City.NAME, arrayOf(WeatherDaySchema.City.CITY_NAME,
            WeatherDaySchema.City.LAT, WeatherDaySchema.City.LON), "${WeatherDaySchema.City.ID_CITY} " +
                "= ${lastCityId.getInt(0)}",
            null, null,null, null,null)
        lasCityCursor.moveToFirst()

        val list = listOf(lasCityCursor.getString(lasCityCursor.getColumnIndex(WeatherDaySchema.City.CITY_NAME)),
            lasCityCursor.getDouble(lasCityCursor.getColumnIndex(WeatherDaySchema.City.LAT)).toString(),
            lasCityCursor.getDouble(lasCityCursor.getColumnIndex(WeatherDaySchema.City.LON)).toString()
        )
        lasCityCursor.close()
        lastCityId.close()

        return list
    }

    fun getCityIdByName(cityName : String) : Int{
        val dataBaseRead = CityWeatherDataBaseHelper(applicationContext).readableDatabase
        val cursor = dataBaseRead.query(WeatherDaySchema.City.NAME, arrayOf(WeatherDaySchema.City.ID_CITY),
            "${WeatherDaySchema.City.CITY_NAME} = '$cityName'", null, null,null, null,null)
        cursor.moveToFirst()
        val id = cursor.getInt(0)
        cursor.close()
        return id
    }

    fun saveForecastToDB(newForecast : List<WeatherDay>, lastCityName : String){
        deleteOldForecast(lastCityName)
        for (weatherDay in newForecast){
            insertWeatherDay(weatherDay)
        }
    }

    private fun deleteOldForecast(lastCityName : String){
        val dataBaseWrite = CityWeatherDataBaseHelper(applicationContext).writableDatabase
        dataBaseWrite.delete(WeatherDaySchema.WeatherDayTable.NAME, "${WeatherDaySchema.City.ID_CITY} =" +
                " ${getCityIdByName(lastCityName)}",
            null)
    }

    private fun insertWeatherDay(weatherDay: WeatherDay){
        val dataBaseWrite = CityWeatherDataBaseHelper(applicationContext).writableDatabase
        dataBaseWrite.insert(WeatherDaySchema.WeatherDayTable.NAME, null, getContentValues(weatherDay))
    }

    fun getWeatherDaysFromDB() : List<WeatherDay> {
        Log.d("Error Model.startAction","response from OpenWeather is null!")
        val lastCity = getLasCity()
        val lastCityId = getCityIdByName(lastCity[0])
        val dataBaseRead = CityWeatherDataBaseHelper(applicationContext).readableDatabase

        val cursor = dataBaseRead.query(WeatherDaySchema.WeatherDayTable.NAME,
            arrayOf("${WeatherDaySchema.WeatherDayTable.ID_WEATHER_DAY}, ${WeatherDaySchema.WeatherDayTable.DAY_OF_WEEK}," +
                    "${WeatherDaySchema.WeatherDayTable.TEMPERATURE}, ${WeatherDaySchema.WeatherDayTable.IMAGE_TYPE}," +
                    "${WeatherDaySchema.WeatherDayTable.DATE}, ${WeatherDaySchema.WeatherDayTable.ID_CITY}"),
            "${WeatherDaySchema.WeatherDayTable.ID_CITY} = $lastCityId",
            null, null,null, null,null
        )

        return parseWeatherDaysFromCursor(cursor)
    }

    private fun parseWeatherDaysFromCursor(cursor : Cursor) : List<WeatherDay> {
        val list = mutableListOf<WeatherDay>()

        try{
            cursor.moveToFirst()
            while (!cursor.isAfterLast){
                val idWeatherDay = cursor.getInt(cursor.getColumnIndex(WeatherDaySchema.WeatherDayTable.ID_WEATHER_DAY))
                val dayOfWeek = cursor.getString(cursor.getColumnIndex(WeatherDaySchema.WeatherDayTable.DAY_OF_WEEK))
                val temperature = cursor.getString(cursor.getColumnIndex(WeatherDaySchema.WeatherDayTable.TEMPERATURE))
                val imageType = cursor.getString(cursor.getColumnIndex(WeatherDaySchema.WeatherDayTable.IMAGE_TYPE))
                val date = cursor.getString(cursor.getColumnIndex(WeatherDaySchema.WeatherDayTable.DATE))
                val idCity = cursor.getInt(cursor.getColumnIndex(WeatherDaySchema.WeatherDayTable.ID_CITY))

                list.add(WeatherDay(idWeatherDay, dayOfWeek, temperature, imageType, date, idCity))
                cursor.moveToNext()
            }
        }
        catch (e : Exception){
            Log.d("Error-parse cursor", e.message.toString())
        }
        finally {
            cursor.close()
        }

        return list
    }
}