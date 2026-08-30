package com.hfda.weatherapplication.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.hfda.weatherapplication.data.local.WeatherDatabase
import com.hfda.weatherapplication.data.local.WeatherEntity
import com.hfda.weatherapplication.data.model.WeatherResponse
import com.hfda.weatherapplication.data.repository.WeatherRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WeatherRepository
    private val TAG = "WeatherViewModel"
    val allRecords: LiveData<List<WeatherEntity>>

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
        repository = WeatherRepository(weatherDao)
        allRecords = repository.allRecords.asLiveData()
    }

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Fetching weather for: $lat, $lon")
                val response = repository.fetchWeather(lat, lon, apiKey)
                _weatherData.value = response
                _error.value = null
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                _error.value = "Network error: Please check your internet connection"
                _weatherData.value = null
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}", e)
                val msg = when (e.code()) {
                    401 -> "Unauthorized: Your API key might be invalid or inactive (Wait 2 hours for new keys)"
                    404 -> "City not found"
                    else -> "Server error: ${e.message()}"
                }
                _error.value = msg
                _weatherData.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                _error.value = e.localizedMessage ?: "An unexpected error occurred"
                _weatherData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCurrentWeather() {
        val current = _weatherData.value ?: return
        viewModelScope.launch {
            repository.saveRecord(
                WeatherEntity(
                    locationName = current.locationName,
                    temperature = current.main.temp,
                    condition = current.weather.firstOrNull()?.condition ?: "Unknown",
                    humidity = current.main.humidity,
                    windSpeed = current.wind.speed
                )
            )
        }
    }

    fun deleteRecord(record: WeatherEntity) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun updateRecord(record: WeatherEntity) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }
}
