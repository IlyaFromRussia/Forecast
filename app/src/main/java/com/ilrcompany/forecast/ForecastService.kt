package com.ilrcompany.forecast

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ilrcompany.forecast.model.InternetUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForecastService : Service() {

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        forecastTask()
        return START_STICKY
    }

    private fun forecastTask(){
        GlobalScope.launch(Dispatchers.IO) {
            while (true){
                delay(15 * 900_000L) // задержка 15 минут
//                delay(1000L * 2)          // задержка 2 секунды
                val internetUtil = InternetUtil(this@ForecastService)
                if(internetUtil.request())
                    Log.d("Service#Log", "rows was inserted!")
            }
        }
    }
}