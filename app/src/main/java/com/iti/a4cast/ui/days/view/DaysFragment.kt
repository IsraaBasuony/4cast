package com.iti.a4cast.ui.days.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DaysFragment : Fragment() {

    private var _binding: FragmentDaysBinding? = null
    private val binding get() = _binding!!
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var vmFactory: HomeViewModelFactory
    private val _location = MutableStateFlow<Pair<Double, Double>>(0.0 to 0.0)
    val location = _location.asStateFlow()

    companion object {
        private const val REQUEST_LOCATION_CODE = 1005
    }
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
           getLastLocation()
            lifecycleScope.launch {
                location.collectLatest {
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


    fun getLastLocation() {
        //step1
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                getFreshLocation()
            } else {
                enableLocationServices()
            }
        } else {
            //step2
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==REQUEST_LOCATION_CODE) {
            if (grantResults.size > 1 && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
                getFreshLocation()
            }
        }

    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity().applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
            requireActivity().applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun enableLocationServices() {
        Toast.makeText(
            requireActivity().applicationContext,
            "Please turn on location",
            Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    @SuppressLint("MissingPermission")
    fun getFreshLocation() {

        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(20000)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    if (location != null) {
                        _location.value = (Pair(location.latitude, location.longitude))
                    }
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            },
            Looper.myLooper()
        )
    }


}