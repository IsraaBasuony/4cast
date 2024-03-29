package com.iti.a4cast.ui.alert.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.databinding.AlertItemBinding
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.HomeUtils

class AlertAdapter(var context: Context, val onClick:(AlertModel)-> Unit) :
    ListAdapter<AlertModel, AlertAdapter.ViewHolder>(DiffUtils) {
        val sheredPref:  SettingsSharedPref  = SettingsSharedPref.getInstance(context)
    class ViewHolder(val binding: AlertItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AlertItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)

        HomeUtils.getLocationAddress(
            context,
            current.latitude,
            current.longitude
        ) { address ->

            holder.binding.textCity.text =
                address?.let { it1 -> HomeUtils.getAddressFormat(it1) }
        }
        holder.binding.btnDelete.setOnClickListener { onClick.invoke(current) }
        holder.binding.startTime.text ="${ HomeUtils.getTimeFormat(current.start,sheredPref.getLanguagePref())} ${HomeUtils.getADateFormat(current.start,sheredPref.getLanguagePref())}"
        holder.binding.endTime.text ="${ HomeUtils.getTimeFormat(current.end,sheredPref.getLanguagePref())} ${HomeUtils.getADateFormat(current.end,sheredPref.getLanguagePref())}"

    }

    object DiffUtils : DiffUtil.ItemCallback<AlertModel>() {
        override fun areItemsTheSame(oldItem: AlertModel, newItem: AlertModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem:AlertModel, newItem:AlertModel): Boolean {
            return oldItem == newItem
        }

    }
}