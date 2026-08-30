package com.hfda.weatherapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hfda.weatherapplication.data.local.WeatherEntity
import com.hfda.weatherapplication.databinding.ItemWeatherBinding

class WeatherAdapter(
    private val onDelete: (WeatherEntity) -> Unit,
    private val onShare: (WeatherEntity) -> Unit,
    private val onEdit: (WeatherEntity) -> Unit
) : ListAdapter<WeatherEntity, WeatherAdapter.WeatherViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        return WeatherViewHolder(
            ItemWeatherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
    }

    inner class WeatherViewHolder(private val binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: WeatherEntity) {
            binding.apply {
                tvItemLocation.text = record.locationName
                tvItemTemp.text = "${record.temperature}°C"
                tvItemDetails.text = "${record.condition} | Hum: ${record.humidity}% | Wind: ${record.windSpeed} km/h"
                
                btnDelete.setOnClickListener { onDelete(record) }
                btnShare.setOnClickListener { onShare(record) }
                btnEdit.setOnClickListener { onEdit(record) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherEntity>() {
        override fun areItemsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem == newItem
        }
    }
}
