package com.ilrcompany.forecast.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract


class ChooseCityActivityContract : ActivityResultContract<Void, List<String>>() {

    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(context, ChooseCityActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<String> {
        val cityName = intent?.getStringExtra("cityName")
        val lat = intent?.getStringExtra("lat")
        val lon = intent?.getStringExtra("lon")

        if (resultCode == Activity.RESULT_OK && cityName != null && lat != null && lon != null)
            return listOf(cityName, lat, lon)
        else
            return listOf("nullCity")
    }
}