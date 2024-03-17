package com.iti.a4cast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.FragmentHomeBinding
import com.iti.a4cast.ui.home.viewmodel.HomeViewModel
import com.iti.a4cast.ui.home.viewmodel.HomeViewModelFactory

class HomeFragment : Fragment() {


    lateinit var viewModel: HomeViewModel
    lateinit var vmFactory: HomeViewModelFactory
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       /* vmFactory =
            HomeViewModelFactory(ForecastRepo.getInstant(ForecastRemoteDataSource.getInstance()))
        viewModel = ViewModelProvider(this, vmFactory).get(HomeViewModel::class.java)

        viewModel.forecastResponse.observe(requireActivity()) { res ->
            binding.test.text = res.current?.weather.toString()
       */ }
    }


