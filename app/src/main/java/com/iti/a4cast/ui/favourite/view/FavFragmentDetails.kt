package com.iti.a4cast.ui.favourite.view

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
import com.iti.a4cast.R
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.local.ForecastDatabase
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.FragmentFavDetailsBinding
import com.iti.a4cast.ui.home.view.DailyAdapter
import com.iti.a4cast.ui.home.view.HourlyAdapter
import com.iti.a4cast.ui.home.viewmodel.HomeViewModel
import com.iti.a4cast.ui.home.viewmodel.HomeViewModelFactory
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.HomeUtils
import com.iti.a4cast.util.setTemp
import com.iti.a4cast.util.setWindSpeed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavFragmentDetails : Fragment() {

    lateinit var binding: FragmentFavDetailsBinding
    lateinit var viewModel: HomeViewModel
    lateinit var vmFactory: HomeViewModelFactory
    lateinit var sheredPref: SettingsSharedPref
    lateinit var hourlyAdapter: HourlyAdapter
    lateinit var hourlyTomorrowAdapter: HourlyAdapter

    private lateinit var dailyAdapter: DailyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavDetailsBinding.inflate(inflater, container, false)
        vmFactory =
            HomeViewModelFactory(
                ForecastRepo.getInstant(
                    ForecastRemoteDataSource.getInstance(),
                    LocalDatasource.getInstance(
                        ForecastDatabase.getInstance(requireActivity().applicationContext)
                            .forecastDao()
                    ),
                    SettingsSharedPref.getInstance(requireActivity())
                )
            )
        viewModel = ViewModelProvider(this, vmFactory)[HomeViewModel::class.java]


        sheredPref = SettingsSharedPref.getInstance(requireContext())

        val latitude = arguments?.getString("lat")
        val longitude = arguments?.getString("long")

        viewModel.getForecastWeather(
            latitude!!.toDouble(),
            longitude!!.toDouble(),
            sheredPref.getLanguagePref()
        )

        dailyAdapter = DailyAdapter(requireActivity().applicationContext)
        hourlyAdapter = HourlyAdapter(requireActivity().applicationContext)
        hourlyTomorrowAdapter = HourlyAdapter(requireActivity().applicationContext)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val language = viewModel.getLanguage()

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forecastResponse.collectLatest { res ->

                    when (res) {
                        is WeatherStatus.Success -> {

                            binding.temperature.setTemp(
                                res.data.current?.temp?.toInt()!!,
                                requireActivity().application
                            )
                            binding.status.text = res.data.current!!.weather[0].description
                            binding.status.setCompoundDrawablesWithIntrinsicBounds(
                                HomeUtils.getWeatherIcon(
                                    res.data.current!!.weather[0].icon
                                ), 0, 0, 0
                            )
                            binding.dayMonth.text =
                                HomeUtils.timeStampMonth(res.data.current!!.dt, language)
                            binding.cloud.text = "${(res.data.current?.clouds)}%"
                            binding.windSpeed.setWindSpeed(
                                res.data.current!!.wind_speed,
                                requireActivity().application
                            )
                            binding.pressure.text = "${res.data.current?.pressure}hPa"
                            binding.humidity.text = "${res.data.current?.humidity}%"
                            binding.visibility.text =
                                "${(res.data.current?.visibility)?.div(1000.0)}km"
                            binding.uvi.text = "${res.data.current?.uvi}"


                            HomeUtils.getLocationAddress(
                                requireContext(),
                                res.data.lat,
                                res.data.lon,
                            ) { address ->

                                binding.locationTxt.text =
                                    address?.let { it1 -> HomeUtils.getAddressFormat(it1) }
                            }


                            binding.todayWeatherImg.setImageResource(HomeUtils.getWeatherImage(res.data.current!!.weather[0].icon!!))

                            hourlyAdapter.submitList(res.data.hourly.subList(0, 23))
                            binding.homeRecycleHours.adapter = hourlyAdapter
                            binding.homeRecycleHours.apply {

                                layoutManager =
                                    LinearLayoutManager(requireActivity().applicationContext).apply {
                                        orientation = RecyclerView.HORIZONTAL
                                    }
                            }
                            binding.hourlyForecast.setOnClickListener {
                                hourlyAdapter.submitList(res.data.hourly.subList(0, 24))
                                binding.homeRecycleHours.adapter = hourlyAdapter
                                binding.hourlyForecast.setTextColor(resources.getColor(R.color.secondary))
                                binding.tomorrowForecast.setTextColor(resources.getColor(R.color.gray))
                                binding.weeklyForecast.setTextColor(resources.getColor(R.color.gray))
                            }
                            binding.weeklyForecast.setOnClickListener {
                                // Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_daysFragment)
                                dailyAdapter.submitList(res.data.daily.subList(0, 8))
                                binding.homeRecycleHours.adapter = dailyAdapter
                                binding.hourlyForecast.setTextColor(resources.getColor(R.color.gray))
                                binding.tomorrowForecast.setTextColor(resources.getColor(R.color.gray))
                                binding.weeklyForecast.setTextColor(resources.getColor(R.color.secondary))
                                //binding.weeklyForecast.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corner))

                            }
                            binding.tomorrowForecast.setOnClickListener {

                                hourlyTomorrowAdapter.submitList(res.data.hourly.subList(24, 48))
                                binding.homeRecycleHours.adapter = hourlyTomorrowAdapter

                                binding.hourlyForecast.setTextColor(resources.getColor(R.color.gray))
                                binding.tomorrowForecast.setTextColor(resources.getColor(R.color.secondary))
                                binding.weeklyForecast.setTextColor(resources.getColor(R.color.gray))
                            }
                            binding.progress.visibility = View.GONE
                            binding.detailsView.visibility = View.VISIBLE
                        }

                        is WeatherStatus.Loading -> {
                            binding.detailsView.visibility = View.GONE
                            binding.progress.visibility = View.VISIBLE
                        }

                        else -> {
                            binding.detailsView.visibility = View.GONE
                            binding.progress.visibility = View.VISIBLE
                        }
                    }


                }
            }

        }

    }

}