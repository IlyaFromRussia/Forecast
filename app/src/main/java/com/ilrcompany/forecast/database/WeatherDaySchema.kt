package com.ilrcompany.forecast.database

class WeatherDaySchema {
    class WeatherDayTable{
        companion object{
            val NAME : String = "weather_day"

            val ID_WEATHER_DAY : String = "id_weather_day"
            val DAY_OF_WEEK : String = "day_of_week"
            val TEMPERATURE : String = "temperature"
            val DATE : String = "date"
            val IMAGE_TYPE = "image_type"
            val ID_CITY : String = "id_city"
        }
    }

    class LastCity{
        companion object{
            val NAME : String = "last_city"

            val LAST_CITY_ID : String = "last_city_id"
        }
    }

    class City{
        companion object{
            val NAME : String = "city"

            val ID_CITY : String = "id_city"
            val CITY_NAME : String = "city_name"
            val LAT : String = "lat"
            val LON : String = "lon"
        }
    }
}