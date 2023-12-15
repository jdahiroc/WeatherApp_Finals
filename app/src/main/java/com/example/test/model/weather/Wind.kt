package com.example.test.model.weather

import com.google.gson.annotations.SerializedName

data class Wind (
    @SerializedName("speed") var speed: Double? = null,
    @SerializedName("deg") var deg: Double? = null,

)