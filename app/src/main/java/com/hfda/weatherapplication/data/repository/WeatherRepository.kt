package com.hfda.weatherapplication.data.repository

import com.hfda.weatherapplication.data.local.WeatherDao
import com.hfda.weatherapplication.data.local.WeatherEntity
import com.hfda.weatherapplication.data.model.WeatherResponse
import com.hfda.weatherapplication.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow

class WeatherRepository(private val weatherDao: WeatherDao) {

    suspend fun fetchWeather(lat: Double, lon: Double, apiKey: String): WeatherResponse {
        return RetrofitClient.instance.getCurrentWeather(lat, lon, apiKey)
    }

    val allRecords: Flow<List<WeatherEntity>> = weatherDao.getAllWeatherRecords()

    suspend fun saveRecord(weather: WeatherEntity) {
        weatherDao.insertWeather(weather)
    }

    suspend fun updateRecord(weather: WeatherEntity) {
        weatherDao.updateWeather(weather)
    }

    suspend fun deleteRecord(weather: WeatherEntity) {
        weatherDao.deleteWeather(weather)
    }
}
