package com.iti.a4cast.ui.days.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.a4cast.data.model.Daily
import com.iti.a4cast.databinding.DailyItemBinding
import com.iti.a4cast.util.HomeUtils

class DailyAdapter : ListAdapter<Daily, DailyAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: DailyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DailyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val daily = getItem(position)

            holder.binding.dayMaxTemp.text = daily.temp.max.toString()
            holder.binding.dayMinTemp.text = daily.temp.min.toString()
            holder.binding.dayStatus.text = daily.weather[0].description
            holder.binding.dayIcon.setImageResource(HomeUtils.getWeatherIcon(daily.weather[0].icon))
            holder.binding.dayTxt.text= HomeUtils.getDayFormat(daily.dt)

    }

    object DiffUtils : DiffUtil.ItemCallback<Daily>() {
        override fun areItemsTheSame(oldItem: Daily, newItem: Daily): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Daily, newItem: Daily): Boolean {
            return oldItem == newItem
        }

    }
}