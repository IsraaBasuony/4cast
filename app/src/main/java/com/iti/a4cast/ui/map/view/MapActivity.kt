package com.iti.a4cast.ui.map.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModelProvider
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.iti.a4cast.R
import com.iti.a4cast.data.local.ForecastDatabase
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.FavAndAlertRepo
import com.iti.a4cast.databinding.ActivityMapBinding
import com.iti.a4cast.ui.alert.viewmodel.AlertViewModel
import com.iti.a4cast.ui.alert.viewmodel.AlertViewModelFactory
import com.iti.a4cast.ui.favourite.viewmode.FavouriteViewModel
import com.iti.a4cast.ui.favourite.viewmode.FavouriteViewModelFactory
import com.iti.a4cast.ui.settings.SettingsSharedPref
import com.iti.a4cast.util.Constants
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var vmFactory : FavouriteViewModelFactory
    lateinit var  viewModel: FavouriteViewModel

    lateinit var alertViewModel: AlertViewModel
    lateinit var alertViewModelFactory: AlertViewModelFactory

    private lateinit var mMap: GoogleMap
    private lateinit var _latLng: LatLng

    private var marker: Marker? = null
    private val PERMISSION_ID = 1005
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var sheredPref: SettingsSharedPref
    lateinit var binding: ActivityMapBinding

    private val getAlertId by lazy {
        intent.getStringExtra(Constants.ID)
    }
    private val getAlertStart by lazy {
        intent.getLongExtra(Constants.START, 0L)
    }
    private val getAlertEne by lazy {
        intent.getLongExtra(Constants.END, 0L)
    }
    private val getAlertType by lazy {
        intent.getStringExtra(Constants.TYPE)
    }
    private val getDestination by lazy {
        intent.getStringExtra(Constants.MAP_DESTINATION)
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vmFactory =
            FavouriteViewModelFactory(
                FavAndAlertRepo.getInstant(LocalDatasource.getInstance(ForecastDatabase.getInstance(this).forecastDao()))
            )
        viewModel = ViewModelProvider(this, vmFactory)[FavouriteViewModel::class.java]

      alertViewModelFactory =
            AlertViewModelFactory(
                FavAndAlertRepo.getInstant(LocalDatasource.getInstance(ForecastDatabase.getInstance(this).forecastDao()))
            )
     alertViewModel = ViewModelProvider(this, alertViewModelFactory)[AlertViewModel::class.java]


        sheredPref = SettingsSharedPref.getInstance(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        binding.buttonSaveAsMainLoc.setOnClickListener {
             if (::_latLng.isInitialized) {
            sheredPref.setLocationPref(SettingsSharedPref.MAP)
            sheredPref.setLatitudePref(_latLng.latitude)
            sheredPref.setLongitudePref(_latLng.longitude)
             }
            finish()
        }

        binding.buttonSaveAsFavLoc.setOnClickListener {
            if (::_latLng.isInitialized){
            viewModel.insertFavLocation(FavLocation(latitude = _latLng.latitude, longitude = _latLng.longitude))
            }
            finish()
        }

        binding.buttonSaveAsAlertLoc.setOnClickListener {
            if(::_latLng.isInitialized){
                val alert = AlertModel(id =getAlertId!!, longitude =  _latLng.longitude!!, latitude= _latLng.latitude!!, start =  getAlertStart, end = getAlertEne!!.toLong(), type = getAlertType!! )
                alertViewModel.insertAlert(alert)
            }

            finish()
        }


        mapFragment.getMapAsync(this)


        onBackPressedDispatcher.addCallback(this) {
            if (getDestination.equals(Constants.ALERT) || getDestination.equals(Constants.SETTING) ) {
                Snackbar.make(
                    binding.root, getString(R.string.please_choose_loc_first), Snackbar.LENGTH_LONG
                ).show()
            }else{
                finish()
            }

        }

        binding.buttonSaveAsMainLoc.isEnabled = false
        binding.buttonSaveAsFavLoc.isEnabled=false
        binding.buttonSaveAsAlertLoc.isEnabled = false


    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

     //   mMap.isMyLocationEnabled = true

        mMap.uiSettings.apply {
            isCompassEnabled = true
            isZoomControlsEnabled = true
        }
        setMapLongClick(mMap)
        setPoiClick(mMap)
        setMapCameraChanged(mMap)

    }

    private fun enableSaveButton() {
        binding.buttonSaveAsMainLoc.isEnabled = true
        binding.buttonSaveAsFavLoc.isEnabled = true
        binding.buttonSaveAsAlertLoc.isEnabled = true
    }

    private fun showTheCorrectButton() {
        if (getDestination.equals(Constants.ALERT)) {
            binding.buttonSaveAsAlertLoc.visibility = View.VISIBLE
        } else if (getDestination.equals(Constants.SETTING)) {
            binding.buttonSaveAsMainLoc.visibility = View.VISIBLE
        } else {
            binding.buttonSaveAsFavLoc.visibility = View.VISIBLE
        }

    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            marker?.remove()
            val snippet = String.format(
                Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude
            )
            val markerOptions = MarkerOptions().position(latLng).snippet(snippet)
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, 10f
                )
            )
            marker = map.addMarker(markerOptions)

        }
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            marker?.showInfoWindow()
            _latLng = poi.latLng
            enableSaveButton()
            showTheCorrectButton()
            Log.d("MapClick", "Latitude: ${poi.latLng.latitude}, Longitude:${poi.latLng.latitude}")


        }
    }

    private fun setMapCameraChanged(googleMap: GoogleMap) {
        googleMap.setOnCameraMoveListener {
            googleMap.clear()
            _latLng = googleMap.cameraPosition.target
            marker = googleMap.addMarker(MarkerOptions().position(_latLng))
        }
    }

    private fun locationChecker() {

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this)
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PermissionChecker.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
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
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
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
                moveCameraMap(LatLng(res.lastLocation!!.latitude, res.lastLocation!!.longitude))
            }
        }
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }


    fun moveCameraMap(latLng: LatLng) {

        val cameraPos = CameraPosition.builder().target(latLng)
            .zoom(11f).build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos))

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