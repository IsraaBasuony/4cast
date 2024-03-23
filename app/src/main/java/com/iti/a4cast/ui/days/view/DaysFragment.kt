package com.iti.a4cast.ui.days.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.FragmentDaysBinding
import com.iti.a4cast.ui.home.viewmodel.HomeViewModel
import com.iti.a4cast.ui.home.viewmodel.HomeViewModelFactory
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.HomeUtils
import com.iti.a4cast.util.setTemp
import com.iti.a4cast.util.setWindSpeed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DaysFragment : Fragment() {

    private var _binding: FragmentDaysBinding? = null
    private val binding get() = _binding!!
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var vmFactory: HomeViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vmFactory =
            HomeViewModelFactory(ForecastRepo.getInstant(ForecastRemoteDataSource.getInstance(), SettingsSharedPref.getInstance(requireActivity())))
        viewModel = ViewModelProvider(this, vmFactory)[HomeViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dailyAdapter = DailyAdapter(requireActivity().applicationContext)
        _binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val language = viewModel.getLanguage()

        if (SettingsSharedPref.getInstance(requireContext()).getLocationPref() == SettingsSharedPref.GPS){
            GPSLocation.getInstant(requireContext()).getLastLocation()
            lifecycleScope.launch {
                GPSLocation.getInstant(requireContext()).location.collectLatest {
                    viewModel.getForecastWeather(it.first, it.second, language)
                }
            }
        }


        lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forecastResponse.collectLatest { res ->

                    when (res) {
                        is WeatherStatus.Success -> {
                            binding.windSpeed.setWindSpeed(
                                res.data.daily[2].wind_speed,
                                requireActivity().application
                            )
                            binding.humidity.text = "${res.data.daily[2].humidity}%"
                            binding.uvi.text = "${res.data.daily[2].uvi}"

                            binding.tomorrowMaxTemp.setTemp(
                                res.data.daily[2].temp.max.toInt(),
                                requireActivity().application
                            )
                            binding.tomorrowMinTemp.setTemp(
                                res.data.daily[2].temp.min.toInt(),
                                requireActivity().application
                            )

                            binding.tomorrowStatus.text = res.data.daily[2].weather[0].description
                            binding.tomorrowStatus.setCompoundDrawablesWithIntrinsicBounds(
                                HomeUtils.getWeatherIcon(
                                    res.data.daily[2].weather[0].icon
                                ), 0, 0, 0
                            )
                            binding.tomorrowWeatherImg.setImageResource(HomeUtils.getWeatherIcon(res.data.daily[2].weather[0].icon))
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



}