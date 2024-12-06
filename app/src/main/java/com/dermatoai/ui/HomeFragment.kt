package com.dermatoai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dermatoai.R
import com.dermatoai.databinding.FragmentHomeBinding
import com.dermatoai.helper.DiagnosisRecordListAdapter
import com.dermatoai.helper.Resource
import com.dermatoai.model.AnalyzeViewModel
import com.dermatoai.model.HomeViewModel
import com.dermatoai.oauth.GoogleAuthenticationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()
    private val analyzeViewModel: AnalyzeViewModel by viewModels()

    @Inject
    lateinit var oauthPreferences: GoogleAuthenticationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocationPermission()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val historyListAdapter = DiagnosisRecordListAdapter()
        binding.historyRecycleView.adapter = historyListAdapter
        binding.historyRecycleView.layoutManager = LinearLayoutManager(requireContext())

        binding.settingAndInfoButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingAndInfoActivity::class.java)
            requireActivity().startActivity(intent)
        }

        homeViewModel.recordList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.historyRecycleView.visibility = GONE
                binding.historyEmptyAnima.visibility = VISIBLE
            } else {
                binding.historyRecycleView.visibility = VISIBLE
                binding.historyEmptyAnima.visibility = GONE
            }
            historyListAdapter.submitList(it)
        }

        analyzeViewModel.history.observe(viewLifecycleOwner) {
            homeViewModel.putRecordList(it)
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d(
                        "Location",
                        "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                    )
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                homeViewModel.requestClimateInfo(latitude, longitude)
                Log.d("Location", "Lat: $latitude, Lon: $longitude")
            } else {
                Log.e("Location", "Fail to get location")
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }.addOnFailureListener {
            Log.e("Location", "Error: ${it.message}")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            oauthPreferences.getProfilePicture().collect {
                Glide.with(requireContext())
                    .load(it)
                    .into(binding.profileImage)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            oauthPreferences.getNickname().collect {
                binding.nicknameText.text = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            oauthPreferences.getAccountName().collect {
                binding.accountName.text = it
            }
        }

        homeViewModel.climateInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
                is Resource.Success -> binding.apply {
                    resource.data?.run {
                        temperature.let {
                            temperatureNumberMini.text = getString(R.string.celsius_format, it)
                            temperatureNumberText.text = getString(R.string.degree_format, it)
                        }
                        force.let { windNumberMini.text = getString(R.string.speed_format, it) }
                        humidity.let {
                            humidityNumberMini.text = getString(R.string.percent_format, it)
                        }
                        uvi.let { uvNumberText.text = it.toString() }
                        cloudCategory.let { cloudStatusText.text = it }
                        cloudDescription.let { cloudDescriptionText.text = it }
                        cloudIcon.let { cloudIconIndicator.setImageResource(it) }
                    }
                }
            }

        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}