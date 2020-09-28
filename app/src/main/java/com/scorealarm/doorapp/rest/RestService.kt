package com.scorealarm.doorapp.rest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.scorealarm.doorapp.rest.api.DoorApi
import com.scorealarm.doorapp.rest.converter.DateTimeConverter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *
 */
object RestService {

    private const val ENDPOINT_LOTTO_API = "https://sa-office-doorman.azurewebsites.net/api/"
    private val TAG = RestService::class.java.canonicalName

    val doorApi: DoorApi by lazy {
        Retrofit.Builder()
            .baseUrl(ENDPOINT_LOTTO_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(DoorApi::class.java)
    }

    val gson: Gson by lazy {
        GsonBuilder().enableComplexMapKeySerialization()
            .registerTypeAdapter(DateTime::class.java, DateTimeConverter())
            .create()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addNetworkInterceptor(loggingInterceptor)
            .build()
    }


    private val loggingInterceptor by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        interceptor
    }
}