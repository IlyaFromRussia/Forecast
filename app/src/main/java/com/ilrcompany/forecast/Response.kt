package com.ilrcompany.forecast

import com.ilrcompany.forecast.recyclerview.WeatherDay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Response(body : String, private val lasCityId : Int) : JSONObject(body) {

    fun getWeatherDayList() : List<WeatherDay>{
       val array = optJSONArray("daily")
       val listWeatherDay = mutableListOf<WeatherDay>()
        if (array != null){
            for ((counter, i) in (0 until array.length()).withIndex()){
                val wordDate = Date(array.getJSONObject(i).getLong("dt") * 1000)
                val splittedArr = SimpleDateFormat("EEEE.dd.MMMM.YYYY", Locale.US)
                    .format(wordDate).toString().split(".")
                val temperature = array.getJSONObject(i).getJSONObject("temp").getDouble("day") - 273
                val imageType = array.getJSONObject(i).getJSONArray("weather")
                    .getJSONObject(0).getString("main")

                listWeatherDay.add(
                    WeatherDay(i, splittedArr[0], temperature.toInt().toString(), imageType,
                    "${splittedArr[1].removePrefix("0")} ${splittedArr[2]}, ${splittedArr[3]}", lasCityId)
                )

                if (counter + 1 == 7 )   // сохраняю в списке только 7 дней. API не дает указать кол-во дней
                    return listWeatherDay
            }
        }
        return listWeatherDay
    }

}