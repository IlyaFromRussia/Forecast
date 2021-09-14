package com.ilrcompany.forecast.recyclerview

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ilrcompany.forecast.R

class WeatherAdapter(private val weather : List<WeatherDay>) : RecyclerView.Adapter<WeatherAdapter.DayViewHolder>() {

    class DayViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        var dayOfWeek : TextView = itemView.findViewById(R.id.day_of_week)
        var temperature : TextView = itemView.findViewById(R.id.temperature)
        var image : ImageView = itemView.findViewById(R.id.image)
        var date : TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.weather_day_list,parent,false)
        return DayViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.dayOfWeek.text = weather[position].dayOfWeek
        holder.date.text = weather[position].date
        holder.temperature.text = weather[position].temperature
        holder.image.setBackgroundResource(getImageResId(weather[position].imageType))
    }

    override fun getItemCount() = weather.size

    private fun getImageResId(type : String) =
        when(type){
            "Clear" -> R.drawable.sun
            "Clouds" -> R.drawable.cloudy
            "Cold" -> R.drawable.cold
            "Rain" -> R.drawable.rain
            "Snow" -> R.drawable.snow
            else -> {
                Log.d("Not yet added image of ", type)
                R.drawable.caution
            }
        }
}