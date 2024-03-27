package com.iti.a4cast.ui.favourite.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.databinding.FavPlaceItemBinding
import com.iti.a4cast.util.HomeUtils

class FavouriteAdapter(var context: Context, val onClick:(FavLocation)-> Unit, val onItemClick:(FavLocation)-> Unit) :
    ListAdapter<FavLocation, FavouriteAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: FavPlaceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FavPlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)

        HomeUtils.getLocationAddress(
            context,
            current.latitude,
            current.longitude
        ) { address ->

            holder.binding.txtViewLocation.text =
                address?.let { it1 -> HomeUtils.getAddressFormat(it1) }
        }
        holder.binding.btnDelete.setOnClickListener { onClick.invoke(current) }
        holder.binding.cardViewPlace.setOnClickListener { onItemClick.invoke(current) }


    }

    object DiffUtils : DiffUtil.ItemCallback<FavLocation>() {
        override fun areItemsTheSame(oldItem: FavLocation, newItem: FavLocation): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: FavLocation, newItem: FavLocation): Boolean {
            return oldItem == newItem
        }

    }
}