package com.scorealarm.doorapp.activity

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings


object UiUtils {

    private val TAG = UiUtils::class.java.canonicalName

    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    fun vibrateOpenTheDoor(context: Context?) {
        (context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                )
        )
    }

}