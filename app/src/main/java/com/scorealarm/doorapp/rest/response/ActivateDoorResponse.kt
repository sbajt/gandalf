package com.scorealarm.doorapp.rest.response

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class ActivateDoorResponse (

    @SerializedName("valid_until")
    val validUntil: DateTime?
)
