package com.iti.a4cast.ui.map.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import com.iti.a4cast.R
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.FavLocationsRepo
import com.iti.a4cast.databinding.ActivityMapBinding
import com.iti.a4cast.ui.map.viewmodel.MapViewModel
import com.iti.a4cast.ui.map.viewmodel.MapViewModelFactory
import com.iti.a4cast.ui.settings.SettingsSharedPref
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var vmFactory : MapViewModelFactory
    lateinit var  viewModel: MapViewModel

    private lateinit var mMap: GoogleMap
    private lateinit var _latLng: LatLng
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var marker: Marker? = null
    private val PERMISSION_ID = 1005

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var sheredPref: SettingsSharedPref

    lateinit var binding: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vmFactory =
            MapViewModelFactory(
                FavLocationsRepo.getInstant(LocalDatasource.getInstance(this))
            )
        viewModel = ViewModelProvider(this, vmFactory)[MapViewModel::class.java]

        sheredPref = SettingsSharedPref.getInstance(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        binding.buttonSaveAsMainLoc.setOnClickListener {
            sheredPref.setLocationPref(SettingsSharedPref.MAP)
            sheredPref.setLatitudePref(latitude)
            sheredPref.setLongitudePref(longitude)
            finish()
        }

        binding.buttonSaveAsFavLoc.setOnClickListener {
            viewModel.insertFavLocation(FavLocation(latitude = latitude, longitude = longitude))
            finish()
        }
        mapFragment.getMapAsync(this)

        initLocation()

        binding.buttonSaveAsMainLoc.isEnabled = false
        binding.buttonSaveAsFavLoc.isEnabled=false


    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        mMap.isMyLocationEnabled = true

        mMap.uiSettings.apply {
            isCompassEnabled = true
            isZoomControlsEnabled = true
        }
        //  checkPermissions()
        setMapLongClick(mMap)
        setPoiClick(mMap)
        setMapCameraChanged(mMap)

    }

    private fun enableSaveButton() {
        binding.buttonSaveAsMainLoc.isEnabled = true
        binding.buttonSaveAsFavLoc.isEnabled = true
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
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
            enableSaveButton()
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