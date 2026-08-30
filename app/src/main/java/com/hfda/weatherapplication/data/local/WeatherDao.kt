package com.hfda.weatherapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather_records ORDER BY timestamp DESC")
    fun getAllWeatherRecords(): Flow<List<WeatherEntity>>

    @Update
    suspend fun updateWeather(weather: WeatherEntity)

    @Delete
    suspend fun deleteWeather(weather: WeatherEntity)
}
