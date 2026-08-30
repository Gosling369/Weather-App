package com.hfda.weatherapplication.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name") val locationName: String,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherData>,
    @SerializedName("wind") val wind: WindData
)

data class MainData(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int
)

data class WeatherData(
    @SerializedName("main") val condition: String,
    @SerializedName("description") val description: String
)

data class WindData(
    @SerializedName("speed") val speed: Double
)
