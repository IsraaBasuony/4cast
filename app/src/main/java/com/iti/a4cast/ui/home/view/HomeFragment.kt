package com.iti.a4cast.ui.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.iti.a4cast.R
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.local.ForecastDatabase
import com.iti.a4cast.data.local.LocalDatasource
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
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vmFactory =
            HomeViewModelFactory(
                ForecastRepo.getInstant(
                    ForecastRemoteDataSource.getInstance(),
                   LocalDatasource.getInstance(ForecastDatabase.getInstance(requireActivity().applicationContext).forecastDao()),
                    SettingsSharedPref.getInstance(requireActivity())
                )
            )
        viewModel = ViewModelProvider(this, vmFactory)[HomeViewModel::class.java]

        sheredPref = SettingsSharedPref.getInstance(requireContext())


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

        if (HomeUtils.checkForInternet(requireContext())) {
            if (SettingsSharedPref.getInstance(requireContext())
                    .getLocationPref() == SettingsSharedPref.GPS
            ) {
                getLastMLocation()
            } else {
                viewModel.getForecastWeather(
                    sheredPref.getLatitudePref()!!.toDouble(),
                    sheredPref.getLongitudePref()!!.toDouble(),
                    sheredPref.getLanguagePref()
                )
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

            lifecycleScope.launch {
                viewModel.getStoredForecast()
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
                            binding.homeView.visibility = View.VISIBLE
                        }

                        is WeatherStatus.Loading -> {
                            binding.homeView.visibility = View.GONE
                            binding.progress.visibility = View.VISIBLE
                        }

                        else -> {
                            binding.homeView.visibility = View.GONE
                            binding.progress.visibility = View.VISIBLE
                        }
                    }


                }
            }

        }

    }




    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermission() {
        Log.i("per", "requestPermission: ")

        requestPermissions(
            arrayOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastMLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getLastMLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {

                requestNewLocationData()
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(200000)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            mFusedLocationClient.removeLocationUpdates(this)
            var mLastLocation: Location? = locationResult.getLastLocation()
            viewModel.getForecastWeather(
                mLastLocation!!.latitude,
                mLastLocation!!.longitude,
                sheredPref.getLanguagePref()
            )
            Log.i("mloc", "onLocationResult: first")
        }
    }

}


