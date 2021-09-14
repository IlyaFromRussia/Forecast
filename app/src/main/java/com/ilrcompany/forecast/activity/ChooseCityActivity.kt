package com.ilrcompany.forecast.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.ilrcompany.forecast.R
import com.ilrcompany.forecast.database.DataBaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ChooseCityActivity : AppCompatActivity() {
    private lateinit var check : Button
    private lateinit var cityName : EditText
    private lateinit var name : TextView
    private lateinit var country : TextView
    private lateinit var lon : TextView
    private lateinit var lat : TextView
    private lateinit var listOfCities : ListView
    private lateinit var dataBaseUtil : DataBaseUtil
    private val API_KEY = "48019c0726dea99ca20d949b05f06cd1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_city)

        dataBaseUtil = DataBaseUtil(this.applicationContext)

        name = findViewById(R.id.name)
        country = findViewById(R.id.country)
        lon = findViewById(R.id.lon)
        lat = findViewById(R.id.lat)
        cityName = findViewById(R.id.editText)
        listOfCities = findViewById(R.id.list_of_cities)

        listOfCities.adapter = ArrayAdapter(this, R.layout.city_in_list,
            getListForAdapter(""))

        listOfCities.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            val cityNameString = getListForAdapter(cityName.text.toString())[position]
            val arr = dataBaseUtil.getCityLatAndLonByName(cityNameString).split("#")
            result(listOf(arr[1],arr[0], "",cityNameString))
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(cityName.windowToken, 0)
            Toast.makeText(this, "Выбран город: $cityNameString"
                , Toast.LENGTH_LONG).show()
        }

        cityName.addTextChangedListener {
            listOfCities.adapter = ArrayAdapter(this, R.layout.city_in_list,
                getListForAdapter(cityName.text.toString()))
        }

        check = findViewById(R.id.check)
        check.setOnClickListener{
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(cityName.windowToken, 0)

            GlobalScope.launch(Dispatchers.IO) {
                if (cityName.text.toString() == "")
                    runOnUiThread{
                        Toast.makeText(this@ChooseCityActivity, getString(R.string.empty_city_name),
                            Toast.LENGTH_LONG).show()
                    }
                else{
                    val url = Uri.parse("https://api.openweathermap.org/data/2.5/weather")
                        .buildUpon()
                        .appendQueryParameter("q",cityName.text.toString())
                        .appendQueryParameter("appid", API_KEY)
                        .build().toString()

                    try {
                        val connection = URL(url).openConnection() as HttpURLConnection
                        val s = getHttpResponse(connection)
                        val coordinate = ResponseCoordinate(s).getLonAndLat()
                        val list = coordinate.split("#")

                        runOnUiThread{
                            name.text = list[3]
                            country.text = list[2]
                            lon.text = list[0]
                            lat.text = list[1]
                        }

                        result(list)
                    }
                    catch (e : IOException){
                        Log.d("URL Error",e.message.toString())
                        runOnUiThread{Toast.makeText(this@ChooseCityActivity, R.string.cannot_find_city,
                            Toast.LENGTH_LONG).show()}
                    }
                }
            }
        }
    }

    private suspend fun getHttpResponse(connection : HttpURLConnection) : String =
         withContext(Dispatchers.IO){
            connection.inputStream.bufferedReader().use { it.readText() }
        }

    class ResponseCoordinate(body : String) : JSONObject(body) {
        fun getLonAndLat() : String =
            "${getJSONObject("coord").getDouble("lon").toInt()}#" +
                    "${getJSONObject("coord").getDouble("lat").toInt()}#${getJSONObject("sys")
                        .getString("country")}#${getString("name")}"
    }

    private fun result(arr : List<String>) {
        this.setResult(Activity.RESULT_OK, intent.putExtra("cityName", arr[3]).putExtra("lat",arr[1])
            .putExtra("lon", arr[0]))
    }

    private fun getListForAdapter(prefix : String) : List<String> {
        val list = mutableListOf<String>()
        if (prefix == ""){
            for (city in dataBaseUtil.getAllCity())
                list.add(city.cityName)
        }
        else{
            for (city in dataBaseUtil.getAllCity()){
                if (city.cityName.startsWith(prefix))
                    list.add(city.cityName)
            }
        }

        return list
    }
}