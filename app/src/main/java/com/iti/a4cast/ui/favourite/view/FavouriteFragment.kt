package com.iti.a4cast.ui.favourite.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.iti.a4cast.R
import com.iti.a4cast.data.local.ForecastDatabase
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.FavAndAlertRepo
import com.iti.a4cast.databinding.FragmentFavouriteBinding
import com.iti.a4cast.ui.favourite.viewmode.FavouriteViewModel
import com.iti.a4cast.ui.favourite.viewmode.FavouriteViewModelFactory
import com.iti.a4cast.ui.map.view.MapActivity
import com.iti.a4cast.util.Constants
import com.iti.a4cast.util.HomeUtils
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
                FavAndAlertRepo.getInstant(
                    LocalDatasource.getInstance(
                        ForecastDatabase.getInstance(
                            requireActivity().applicationContext
                        ).forecastDao()
                    )
                )
            )
        viewModel = ViewModelProvider(this, vmFactory)[FavouriteViewModel::class.java]
        adapter = FavouriteAdapter(requireContext(),
            onClick = { item ->
                checkDeleteDialog(item)
            },
            onItemClick = { item ->
                if (HomeUtils.checkForInternet(requireContext())) {
                    val action =
                        FavouriteFragmentDirections.actionFavouriteFragmentToFavFragmentDetails(
                            item.latitude.toString(),
                            item.longitude.toString()
                        )
                    Navigation.findNavController(requireView()).navigate(action)
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.no_internet),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getString(R.string.settings), View.OnClickListener {
                            startActivityForResult(
                                Intent(
                                    Settings.ACTION_SETTINGS
                                ), 0
                            );
                        }).show()
                }


            }
        )

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    } else {
                        binding.imageViewEmptyList.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                }
            }
        }

        binding.btnAddFavLoc.setOnClickListener {
            if (HomeUtils.checkForInternet(requireContext())) {
                with(Intent(requireContext(), MapActivity::class.java)) {
                    putExtra(Constants.MAP_DESTINATION, Constants.FAVORITE)
                    startActivity(this)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    getString(R.string.no_internet),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.settings), View.OnClickListener {
                        startActivityForResult(
                            Intent(
                                Settings.ACTION_SETTINGS
                            ), 0
                        );
                    }).show()
            }
        }

    }

    private fun checkDeleteDialog(favLocation: FavLocation) {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.delete_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()


        val removeButton = dialog.findViewById<TextView>(R.id.remove)
        val cancelButton = dialog.findViewById<TextView>(R.id.cancel)


        removeButton.setOnClickListener {
            viewModel.deleteFavLocation(favLocation)
            dialog.dismiss()

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

}