package com.iti.a4cast.ui.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
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
    lateinit var sheredPref: SettingsSharedPref
    private lateinit var dailyAdapter: DailyAdapter
    lateinit var hourlyTomorrowAdapter: HourlyAdapter

    private val PERMISSION_ID = 1005

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    lateinit var mLocationRequest: LocationRequest

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
            HomeViewModelFactory(
                ForecastRepo.getInstant(
                    ForecastRemoteDataSource.getInstance(),
                    SettingsSharedPref.getInstance(requireActivity())
                )
            )
        viewModel = ViewModelProvider(this, vmFactory)[HomeViewModel::class.java]

        sheredPref = SettingsSharedPref.getInstance(requireContext())

        initLocation()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        hourlyAdapter = HourlyAdapter(requireActivity().applicationContext)
        hourlyTomorrowAdapter = HourlyAdapter(requireActivity().applicationContext)
        dailyAdapter = DailyAdapter(requireActivity().applicationContext)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // img.setImageResource(R.drawable.icon_01n)

        val language = viewModel.getLanguage()

        if (SettingsSharedPref.getInstance(requireContext())
                .getLocationPref() == SettingsSharedPref.GPS
        ) {
            checkPermissions()
            lifecycleScope.launch {
                location.collectLatest {
                    viewModel.getForecastWeather(it.first, it.second, language)
                    latitude = it.first
                    longitude = it.second
                    Log.d("Emitting", "onLocationResult: ${it.first}")

                }
            }
        } else {
            viewModel.getForecastWeather(
                sheredPref.getLatitudePref()!!.toDouble(),
                sheredPref.getLongitudePref()!!.toDouble(),
                sheredPref.getLanguagePref()
            )
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
                                res.data.lat,
                                res.data.lon,
                            ) { address ->

                                binding.locationTxt.text =
                                    address?.let { it1 -> HomeUtils.getAddressFormat(it1) }
                            }


                            // binding.todayWeatherImg.setImageResource(HomeUtils.getWeatherIcon(res.data.current!!.weather[0].icon!!))

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
                                binding.hourlyForecast.setTextColor(resources.getColor( R.color.secondary))
                                binding.tomorrowForecast.setTextColor(resources.getColor( R.color.gray))
                                binding.weeklyForecast.setTextColor(resources.getColor( R.color.gray))
                            }
                            binding.weeklyForecast.setOnClickListener {
                                // Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_daysFragment)
                                dailyAdapter.submitList(res.data.daily.subList(0, 8))
                                binding.homeRecycleHours.adapter = dailyAdapter
                                binding.hourlyForecast.setTextColor(resources.getColor( R.color.gray))
                                binding.tomorrowForecast.setTextColor(resources.getColor( R.color.gray))
                                binding.weeklyForecast.setTextColor(resources.getColor( R.color.secondary))
                                //binding.weeklyForecast.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corner))

                            }
                            binding.tomorrowForecast.setOnClickListener {

                                hourlyTomorrowAdapter.submitList(res.data.hourly.subList(24, 48))
                                binding.homeRecycleHours.adapter = hourlyTomorrowAdapter

                                binding.hourlyForecast.setTextColor(resources.getColor( R.color.gray))
                                binding.tomorrowForecast.setTextColor(resources.getColor( R.color.secondary))
                                binding.weeklyForecast.setTextColor(resources.getColor( R.color.gray))
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


    fun locationChecker() {

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->

            try {
                task.getResult(ApiException::class.java)
                getLastLocation()
            } catch (exception: ApiException) {

                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {

                            val resolve = exception as ResolvableApiException

                            startIntentSenderForResult(
                                resolve.resolution.intentSender,
                                Priority.PRIORITY_HIGH_ACCURACY,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        } catch (ex: Exception) {

                        }
                    }
                }
            }


        }

    }

    private fun checkPermissions() {
        if (checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PermissionChecker.PERMISSION_GRANTED
            && checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        } else {
            locationChecker()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()

        }
    }

    private fun initLocation() {
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        mLocationRequest = LocationRequest()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(20000)
            .setFastestInterval(2000)
            .setSmallestDisplacement(5f)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {

                val location = res.lastLocation
                if (location != null) {
                    _location.value = (Pair(location.latitude, location.longitude))
                }
                mFusedLocationProviderClient.removeLocationUpdates(this)
            }

        }
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Priority.PRIORITY_HIGH_ACCURACY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        getLastLocation()
                    }

                    Activity.RESULT_CANCELED -> {
                        locationChecker()
                    }
                }
            }
        }

    }
}


