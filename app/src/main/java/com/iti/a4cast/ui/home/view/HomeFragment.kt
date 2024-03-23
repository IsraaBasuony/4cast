package com.iti.a4cast.ui.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.iti.a4cast.R
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.repo.ForecastRepo
import com.iti.a4cast.databinding.FragmentHomeBinding
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
import kotlin.properties.Delegates

class HomeFragment : Fragment() {


    private lateinit var viewModel: HomeViewModel
    private lateinit var vmFactory: HomeViewModelFactory

    private val _location = MutableStateFlow<Pair<Double, Double>>(0.0 to 0.0)
    val location = _location.asStateFlow()
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()

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

        hourlyAdapter = HourlyAdapter(requireActivity().applicationContext)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.weeklyForecast.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_daysFragment)
        }
        // img.setImageResource(R.drawable.icon_01n)

        val language = viewModel.getLanguage()

        if (SettingsSharedPref.getInstance(requireContext()).getLocationPref() == SettingsSharedPref.GPS){
            getLastLocation()
            lifecycleScope.launch {
              location.collectLatest {
                    viewModel.getForecastWeather(it.first, it.second, language)
                    latitude = it.first
                    longitude = it.second
                    Log.d("Emitting", "onLocationResult: ${it.first}")

                }
            }
        }

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
                                latitude,
                                longitude,
                            ) { address ->

                                binding.locationTxt.text =
                                address?.let { it1 -> HomeUtils.getAddressFormat(it1) }
                            }


                            // binding.todayWeatherImg.setImageResource(HomeUtils.getWeatherIcon(res.data.current!!.weather[0].icon!!))

                            hourlyAdapter.submitList(res.data.hourly)
                            binding.homeRecycleHours.apply {
                                adapter = hourlyAdapter
                                layoutManager =
                                    LinearLayoutManager(requireActivity().applicationContext).apply {
                                        orientation = RecyclerView.HORIZONTAL
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




    private fun getLastLocation() {
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
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults.size > 1 && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
                getFreshLocation()
            }
        }

    }

    private fun checkPermissions(): Boolean {
        return checkSelfPermission(
            requireActivity().applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
                || checkSelfPermission(
            requireActivity().applicationContext,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
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


