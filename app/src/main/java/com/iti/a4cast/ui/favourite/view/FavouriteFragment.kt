package com.iti.a4cast.ui.favourite.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.repo.FavLocationsRepo
import com.iti.a4cast.databinding.FragmentFavouriteBinding
import com.iti.a4cast.ui.favourite.viewmode.FavouriteViewModel
import com.iti.a4cast.ui.favourite.viewmode.FavouriteViewModelFactory
import com.iti.a4cast.ui.map.view.MapActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavouriteFragment : Fragment() {

    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var viewModel: FavouriteViewModel
    private lateinit var vmFactory: FavouriteViewModelFactory
    lateinit var adapter: FavouriteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavouriteBinding.inflate(inflater, container, false)

        vmFactory =
            FavouriteViewModelFactory(
                FavLocationsRepo.getInstant(LocalDatasource.getInstance(requireContext()))
            )
        viewModel = ViewModelProvider(this, vmFactory)[FavouriteViewModel::class.java]
        adapter = FavouriteAdapter(requireContext()){
            viewModel.deleteFavLocation(it)
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set up RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavouriteFragment.adapter
        }

        viewModel.getAllFavLocations()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favLocations.collectLatest { locations ->
                    if (locations.isNotEmpty()) {
                        binding.imageViewEmptyList.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        adapter.submitList(locations)
                        Log.i("TAG Fav", "onViewCreated: ${locations.size}")
                    } else {
                        binding.imageViewEmptyList.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            }
        }

        binding.btnAddFavLoc.setOnClickListener {
            startActivity(Intent(requireActivity(), MapActivity::class.java))
        }
    }
}