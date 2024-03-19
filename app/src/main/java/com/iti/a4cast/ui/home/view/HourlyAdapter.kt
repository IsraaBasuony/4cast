package com.iti.a4cast.ui.home.view

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.a4cast.data.model.Current
import com.iti.a4cast.databinding.HourlyItemBinding
import com.iti.a4cast.util.HomeUtils

class HourlyAdapter : ListAdapter<Current, HourlyAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: HourlyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HourlyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hourly = getItem(position)

        holder.binding.hourTxt.text = if (position == 0) {
            holder.binding.hourTxt.setTextColor( Color.parseColor("#396295"))
            holder.binding.hourTxt.setTypeface(null, Typeface.BOLD)
            "Now"
        } else {
            "${HomeUtils.timeStampToHour(hourly.dt)}"
        }
        holder.binding.hourTemp.text = hourly.temp.toInt().toString()
        holder.binding.hourIcon.setImageResource(HomeUtils.getWeatherIcon(hourly.weather[0].icon))

    }

    object DiffUtils : DiffUtil.ItemCallback<Current>() {
        override fun areItemsTheSame(oldItem: Current, newItem: Current): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Current, newItem: Current): Boolean {
            return oldItem == newItem
        }

    }
}