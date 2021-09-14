package com.ilrcompany.forecast.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ilrcompany.forecast.recyclerview.WeatherDay

class CityWeatherDataBaseHelper(contex : Context) : SQLiteOpenHelper(contex,DATABASE_NAME, null, VERSION){
    companion object{
        private var VERSION = 1
        private var DATABASE_NAME = "CityWeather.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table ${WeatherDaySchema.WeatherDayTable.NAME} (" +
                "${WeatherDaySchema.WeatherDayTable.ID_WEATHER_DAY} integer primary key autoincrement, " +
                "${WeatherDaySchema.WeatherDayTable.DAY_OF_WEEK}, ${WeatherDaySchema.WeatherDayTable.TEMPERATURE}," +
                "${WeatherDaySchema.WeatherDayTable.DATE}, ${WeatherDaySchema.WeatherDayTable.ID_CITY}," +
                "${WeatherDaySchema.WeatherDayTable.IMAGE_TYPE})"
        )

        db?.execSQL("create table ${WeatherDaySchema.City.NAME} " +
                "(${WeatherDaySchema.City.ID_CITY} integer primary key autoincrement, ${WeatherDaySchema.City.CITY_NAME}," +
                "${WeatherDaySchema.City.LAT}, ${WeatherDaySchema.City.LON})"
        )

        insertDeafultCities(db)

        db?.execSQL("create table ${WeatherDaySchema.LastCity.NAME} (${WeatherDaySchema.LastCity.LAST_CITY_ID} " +
                "integer primary key autoincrement)")

        setDeafultCity(db)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}

fun getContentValues(weatherDay : WeatherDay) : ContentValues {
    val content = ContentValues()
    content.put(WeatherDaySchema.WeatherDayTable.DAY_OF_WEEK, weatherDay.dayOfWeek)
    content.put(WeatherDaySchema.WeatherDayTable.TEMPERATURE, weatherDay.temperature)
    content.put(WeatherDaySchema.WeatherDayTable.IMAGE_TYPE, weatherDay.imageType)
    content.put(WeatherDaySchema.WeatherDayTable.DATE, weatherDay.date)
    content.put(WeatherDaySchema.WeatherDayTable.ID_CITY, weatherDay.idCity)

    return content
}

fun insertDeafultCities(db: SQLiteDatabase?){
    db?.execSQL("INSERT INTO ${WeatherDaySchema.City.NAME} (${WeatherDaySchema.City.CITY_NAME}," +
            "${WeatherDaySchema.City.LAT},${WeatherDaySchema.City.LON}) values " +
            "('Rostov on Don', 47.27410460334183, 39.722761104448914 )," +
            "('Moscow', 55.7480591854557, 37.60241225647985 )," +
            "('Prague', 50.10936783771256, 14.412277645873699)," +
            "('Amsterdam', 52.376385449783946, 4.9032670876708195);"
    )
}

fun setDeafultCity(db: SQLiteDatabase?){
    db?.execSQL("INSERT INTO ${WeatherDaySchema.LastCity.NAME} (${WeatherDaySchema.LastCity.LAST_CITY_ID}) values(1);")
}

fun getContentValuesOfCity(cityName : String, lat : Int, lon : Int) : ContentValues{
    val content = ContentValues()
    content.put(WeatherDaySchema.City.CITY_NAME, cityName)
    content.put(WeatherDaySchema.City.LAT, lat)
    content.put(WeatherDaySchema.City.LON,lon)

    return content
}
