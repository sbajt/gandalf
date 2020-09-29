package com.scorealarm.doorapp.rest.api

import com.scorealarm.doorapp.rest.response.ActivateDoorResponse
import io.reactivex.Observable
import retrofit2.http.Header
import retrofit2.http.POST

interface DoorApi {

    @POST("activate")
    fun activate(@Header("authentication") token: String): Observable<ActivateDoorResponse>

}