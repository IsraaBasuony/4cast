package com.iti.a4cast.ui.days.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.FragmentDaysBinding
import com.iti.a4cast.ui.home.viewmodel.HomeViewModel
import com.iti.a4cast.ui.home.viewmodel.HomeViewModelFactory
import com.iti.a4cast.util.HomeUtils
import kotlinx.coroutines.launch

class DaysFragment : Fragment() {

    private var _binding: FragmentDaysBinding? = null
    private val binding get() = _binding!!
    lateinit var dailyAdapter: DailyAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var vmFactory: HomeViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dailyAdapter = DailyAdapter()
        _binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        vmFactory =
            HomeViewModelFactory(ForecastRepo.getInstant(ForecastRemoteDataSource.getInstance()))
        viewModel = ViewModelProvider(this, vmFactory)[HomeViewModel::class.java]

        viewModel.getForecastWeather(30.0333, 31.2333, "standard", "en")
        lifecycleScope.launch {

            viewModel.forecastResponse.collect { res ->

                when (res) {
                    is WeatherStatus.Success -> {
                        binding.windSpeed.text = "${(res.data.daily[2]?.wind_speed).toString()}m/s"
                        binding.humidity.text = "${res.data.daily[2]?.humidity}%"
                        binding.uvi.text = "${res.data.daily[2]?.uvi}"

                        binding.tomorrowMaxTemp.text=res.data.daily[2].temp.max.toString()
                        binding.tomorrowMinTemp.text=res.data.daily[2].temp.min.toString()

                        binding.tomorrowStatus.text=res.data.daily[2].weather[0].description
                        binding.tomorrowStatus.setCompoundDrawablesWithIntrinsicBounds(
                            HomeUtils.getWeatherIcon(
                                res.data.daily[2]!!.weather[0].icon!!
                            ), 0, 0, 0
                        )
                        binding.tomorrowWeatherImg.setImageResource(HomeUtils.getWeatherIcon(res.data.daily[2]!!.weather[0].icon!!))
                        dailyAdapter.submitList(res.data.daily.subList(2, 8))
                        binding.homeRecycleDays.apply {
                            adapter = dailyAdapter
                            layoutManager =
                                LinearLayoutManager(requireActivity().applicationContext).apply {
                                    orientation = RecyclerView.VERTICAL
                                }
                        }
                    }

                    is WeatherStatus.Loading -> {

                    }

                    else -> {

                    }
                }


            }


        }

    }



}