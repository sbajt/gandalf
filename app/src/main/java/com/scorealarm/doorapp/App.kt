package com.scorealarm.doorapp

import android.app.Application
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import net.danlew.android.joda.JodaTimeAndroid

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)

        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(
                                CalligraphyInterceptor(
                                        CalligraphyConfig.Builder()
                                                .setDefaultFontPath("fonts/anirb.ttf")
                                                .setFontAttrId(R.attr.fontPath)
                                                .build()
                                )
                        ).build()

        )
    }
}