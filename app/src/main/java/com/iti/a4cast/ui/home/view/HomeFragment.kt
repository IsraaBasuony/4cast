package com.iti.a4cast.ui.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.iti.a4cast.util.HomeUtils
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {


    private lateinit var viewModel: HomeViewModel
    private lateinit var vmFactory: HomeViewModelFactory
    lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var img: ImageView
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    companion object {
        private const val REQUEST_LOCATION_CODE = 1005
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        hourlyAdapter = HourlyAdapter()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.weeklyForecast.setOnClickListener {

            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_daysFragment)
        }
        // img.setImageResource(R.drawable.icon_01n)
        getLastLocation()
        vmFactory =
            HomeViewModelFactory(ForecastRepo.getInstant(ForecastRemoteDataSource.getInstance()))
        viewModel = ViewModelProvider(this, vmFactory)[HomeViewModel::class.java]

        viewModel.getForecastWeather(30.0333, 31.2333, "standard", "en")
        lifecycleScope.launch {

            viewModel.forecastResponse.collect { res ->

                when (res) {
                    is WeatherStatus.Success -> {

                        binding.temperature.text = "${res.data.current?.temp?.toInt()}°"
                        binding.status.text = res.data.current!!.weather[0].description
                        binding.status.setCompoundDrawablesWithIntrinsicBounds(
                            HomeUtils.getWeatherIcon(
                                res.data.current!!.weather[0].icon!!
                            ), 0, 0, 0
                        )
                        binding.dayMonth.text = HomeUtils.timeStampMonth(res.data.current!!.dt)
                        binding.cloud.text = "${(res.data.current?.clouds)}%"
                        binding.windSpeed.text = "${(res.data.current?.wind_speed).toString()}m/s"
                        binding.pressure.text = "${res.data.current?.pressure}hPa"
                        binding.humidity.text = "${res.data.current?.humidity}%"
                        binding.visibility.text = "${(res.data.current?.visibility)?.div(1000.0)}km"
                        binding.uvi.text = "${res.data.current?.uvi}"
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




    fun getLastLocation() {
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

    fun checkPermissions(): Boolean {
        return checkSelfPermission(
            requireActivity().applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
                || checkSelfPermission(
            requireActivity().applicationContext,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_CODE
        )
    }

    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun enableLocationServices() {
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
        mLocationRequest.setInterval(200000)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    longitude = location?.longitude!!.toDouble()
                    latitude = location?.latitude!!.toDouble()

                    getTextLocation(location!!.latitude, location.longitude)
                    //fusedLocationProviderClient.removeLocationUpdates(this)
                }
            },
            Looper.myLooper()
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getTextLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireActivity().applicationContext)

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses!!.isNotEmpty()) {
                val address = "${addresses[0].locality}, ${addresses[0].countryName}"
                binding.locationTxt.text = address
            } else {
                binding.locationTxt.text = "Address not found"
            }
        } catch (e: Exception) {
            binding.locationTxt.text = "Error retrieving address"
        }
    }


}


