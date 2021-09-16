package com.ilrcompany.forecast.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ilrcompany.forecast.model.Model
import com.ilrcompany.forecast.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var search : Button
    private lateinit var navigate : Button
    private lateinit var container : ConstraintLayout
    private lateinit var image : ImageView
    private lateinit var recyclerView : RecyclerView
    private lateinit var cityName: TextView
    private lateinit var temperature: TextView
    private lateinit var activityLauncher : ActivityResultLauncher<Void>
    private lateinit var model : Model
    private lateinit var progresaBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search = findViewById(R.id.search_button)
        navigate = findViewById(R.id.navigate_button)
        container = findViewById(R.id.container)
        image = findViewById(R.id.image)
        cityName = findViewById(R.id.city_name)
        temperature = findViewById(R.id.temperature)
        recyclerView = findViewById(R.id.weather_day_list)
        progresaBar = findViewById(R.id.progress_bar)

        navigate.setOnClickListener{
            checkPermission()
        }

        model = Model(recyclerView, this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        activityLauncher = registerForActivityResult(ChooseCityActivityContract()){
            if (it.size == 3){
                GlobalScope.launch(Dispatchers.IO) {
                    model.actionAfterNewCity(it)
                }
            }
        }

        search.setOnClickListener{
            chooseCity()
        }

        val model = Model(recyclerView, this)
        model.firstAction()
    }

    private fun setBackgroundAndImageAccordingTime(){
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour in 6..19) {container.setBackgroundColor(getColor(R.color.DayColor))
            image.setBackgroundResource(R.drawable.lemon)
            Log.d("DebugBackgroundColor ","Day Color!!!!! $currentHour")}
        else {container.setBackgroundColor(getColor(R.color.NightColor))
            image.setBackgroundResource(R.drawable.moon)
            Log.d("DebugBackgroundColor","Night Color!!!!! $currentHour")}
    }

    override fun onResume() {
        super.onResume()
        setBackgroundAndImageAccordingTime() // для обработки возобновления активности после 19 часов
        navigate.isEnabled = true
        search.isEnabled = true
    }

    fun setCityNameAndTemperature(name : String, temp : String){
        cityName.text = name
        temperature.text = temp
    }

    private fun chooseCity(){
        activityLauncher.launch(null)
    }

    private fun checkPermission(){
        val status = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if (status == PackageManager.PERMISSION_GRANTED){
            setCloserCity()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
        }
    }

    private fun setCloserCity(){
        navigate.isClickable = false
        search.isClickable = false
        model.setCloserCity()
        progresaBar.visibility = ProgressBar.VISIBLE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0){
            if (grantResults.isNotEmpty()){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setCloserCity()
                }
                else{
                    // DENIED
                    Log.d("PERMISSION", "Permission denied!")
                }
            }
        }
    }

    fun hideProgressBarAndActivateButton(){
        progresaBar.visibility = ProgressBar.INVISIBLE
        navigate.isClickable = true
        search.isClickable = true
    }

}