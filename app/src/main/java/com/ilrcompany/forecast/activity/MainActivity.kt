package com.ilrcompany.forecast.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ilrcompany.forecast.MyLocationListener
import com.ilrcompany.forecast.model.Model
import com.ilrcompany.forecast.R
import kotlinx.coroutines.*
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
    private lateinit var leftBar : SeekBar
    private lateinit var rightBar : SeekBar
    private lateinit var job : Job

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
        leftBar = findViewById(R.id.leftBar)
        rightBar = findViewById(R.id.rightBar)

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
            startSeekBar()
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0){
            if (grantResults.isNotEmpty()){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startSeekBar()
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
        job.cancel()

        leftBar.visibility = SeekBar.INVISIBLE
        rightBar.visibility = SeekBar.INVISIBLE
        navigate.isClickable = true
        search.isClickable = true
    }

    fun startSeekBar(){
        setSeekBarProperties(leftBar)
        setSeekBarProperties(rightBar)
        rightBar.progress = 100

        val leftBitmap = getDrawable(R.drawable.placeholder)?.toBitmap(30,30)
        leftBar.thumb = leftBitmap?.toDrawable(resources)

        val rightBitmap = getDrawable(R.drawable.earth_planet)?.toBitmap(30,30)
        rightBar.thumb = rightBitmap?.toDrawable(resources)

        leftBar.progress = 0
        rightBar.progress = 90
        job = GlobalScope.launch(Dispatchers.Main) {
            var flag = true
            while (true){
                for (i in 0..90){
                    delay(30)
                    leftBar.progress = leftBar.progress + getLeftProgress(flag)
                    rightBar.progress = rightBar.progress + getLeftProgress(!flag)
                }
                flag = !flag
            }
        }
    }

    private fun setSeekBarProperties(bar : SeekBar){
        bar.visibility = SeekBar.VISIBLE
        bar.isEnabled = false
        bar.min = 0
        bar.max = 100

        val back : Drawable?
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour in 6..19) back = getColor(R.color.DayColor).toDrawable()
        else back = getColor(R.color.NightColor).toDrawable()

        bar.background = back
        bar.progressDrawable = back
    }

    private fun getLeftProgress(flag : Boolean) =
        if (flag) 1 else - 1
}