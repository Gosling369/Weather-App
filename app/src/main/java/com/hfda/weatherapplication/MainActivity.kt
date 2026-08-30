package com.hfda.weatherapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.hfda.weatherapplication.data.local.WeatherEntity
import com.hfda.weatherapplication.databinding.ActivityMainBinding
import com.hfda.weatherapplication.ui.adapter.WeatherAdapter
import com.hfda.weatherapplication.ui.viewmodel.WeatherViewModel
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: WeatherAdapter
    private lateinit var sharedPreferences: SharedPreferences

    // TODO: Replace with your actual OpenWeatherMap API Key
    private val API_KEY = "e2792115084b4a2089a29e8feb04edd2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupPreferences()

        requestLocation()
    }

    private fun setupRecyclerView() {
        adapter = WeatherAdapter(
            onDelete = { record -> viewModel.deleteRecord(record) },
            onShare = { record -> shareWeather(record) },
            onEdit = { record -> showEditDialog(record) }
        )
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }

    private fun showEditDialog(record: WeatherEntity) {
        val input = EditText(this)
        input.setText(record.locationName)
        
        AlertDialog.Builder(this)
            .setTitle("Edit Location Name")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotEmpty()) {
                    viewModel.updateRecord(record.copy(locationName = newName))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.weatherData.observe(this) { weather ->
            weather?.let {
                val isFahrenheit = sharedPreferences.getBoolean("is_fahrenheit", false)
                val temp = if (isFahrenheit) {
                    (it.main.temp * 9 / 5) + 32
                } else {
                    it.main.temp
                }
                val unit = if (isFahrenheit) "°F" else "°C"

                binding.tvLocationName.text = it.locationName
                binding.tvTemperature.text = String.format(Locale.getDefault(), "%.1f%s", temp, unit)
                binding.tvCondition.text = it.weather.firstOrNull()?.description?.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
                binding.tvHumidity.text = "${it.main.humidity}%"
                binding.tvWindSpeed.text = "${it.wind.speed} km/h"
                binding.cvCurrentWeather.visibility = View.VISIBLE
            }
        }

        viewModel.allRecords.observe(this) { records ->
            adapter.submitList(records)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.mainProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnRefresh.setOnClickListener {
            requestLocation()
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveCurrentWeather()
            Toast.makeText(this, "Record saved locally", Toast.LENGTH_SHORT).show()
        }

        binding.fabLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupPreferences() {
        val isFahrenheit = sharedPreferences.getBoolean("is_fahrenheit", false)
        binding.swUnit.isChecked = isFahrenheit

        binding.swUnit.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("is_fahrenheit", isChecked).apply()
            // Refresh UI if we have data
            viewModel.weatherData.value?.let {
                viewModel.fetchWeather(0.0, 0.0, "") // Trigger observer refresh with dummy lat/lon if needed, 
                // but actually the observer will re-run when we update LiveData.
                // A better way is to just call a method to refresh UI.
                refreshDisplay()
            }
        }
    }

    private fun refreshDisplay() {
        viewModel.weatherData.value?.let {
            val isFahrenheit = sharedPreferences.getBoolean("is_fahrenheit", false)
            val temp = if (isFahrenheit) (it.main.temp * 9 / 5) + 32 else it.main.temp
            val unit = if (isFahrenheit) "°F" else "°C"
            binding.tvTemperature.text = String.format(Locale.getDefault(), "%.1f%s", temp, unit)
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    android.util.Log.d("MainActivity", "Location found: ${location.latitude}, ${location.longitude}")
                    viewModel.fetchWeather(location.latitude, location.longitude, API_KEY)
                } else {
                    Toast.makeText(this, "Unable to get location. Ensure GPS is ON.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            requestLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareWeather(record: WeatherEntity) {
        val shareText = "Weather in ${record.locationName}: ${record.temperature}°C, ${record.condition}. Humidity: ${record.humidity}%, Wind: ${record.windSpeed} km/h"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Weather via"))
    }
}
