package com.example.test.model.weather

import android.health.connect.datatypes.units.Pressure
import com.google.gson.annotations.SerializedName

data class Main (
    @SerializedName("temp") var temp: Double? = null,
    @SerializedName("feels_like") var feelslike: Double? = null,
    @SerializedName("temp_min") var tempmin: Double? = null,
    @SerializedName("temp_max") var tempmax: Double? = null,
    @SerializedName("pressure") var pressure: Double? = null,
    @SerializedName("humidity") var humidity: Double? = null,

)