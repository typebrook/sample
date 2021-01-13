package com.example.sample.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sample.R
import com.example.sample.databinding.MainFragmentBinding
import com.google.gson.JsonObject
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions
import com.mapbox.mapboxsdk.plugins.places.picker.viewmodel.PlacePickerViewModel
import timber.log.Timber
import java.util.*

class MainFragment : Fragment(), OnMapReadyCallback {

    private val viewModel by activityViewModels<PlacePickerViewModel>()
    private val binding by lazy { MainFragmentBinding.inflate(layoutInflater) }
    private val accessToken =
        "pk.eyJ1IjoidHlwZWJyb29rIiwiYSI6ImNqNHVyaTc5dDBuazczMm1jenl3cG8wb3IifQ.2UEZ-jiHgHvYYqVirXhgpw"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(requireContext(), accessToken)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        with(mapView) {
            onCreate(savedInstanceState)
            getMapAsync(this@MainFragment)
        }

        search.setOnClickListener {
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(accessToken)
                .placeOptions(
                    PlaceOptions.builder()
                    .backgroundColor(Color.WHITE)
                    .build())
                .build(activity)
            startActivityForResult(intent, 8888)
        }

        binding.mapboxPluginsPickerBottomSheet.findViewById<View>(R.id.place_chosen_button)?.run {
            setOnClickListener {
                Toast.makeText(requireContext(), "foo", Toast.LENGTH_LONG).show()
            }
        }

        Unit
    }

    override fun onMapReady(map: MapboxMap) = with(map) {
        setStyle(Style.LIGHT)
        cameraPosition = CameraPosition.Builder()
            .target(LatLng(25.023167, 121.585674))
            .zoom(12.0)
            .build()

        addOnCameraMoveStartedListener {
            if (binding.mapboxPluginsImageViewMarker.translationY == 0f) {
                binding.mapboxPluginsImageViewMarker.animate().translationY(-75f)
                    .setInterpolator(OvershootInterpolator()).setDuration(250).start()
                if (binding.mapboxPluginsPickerBottomSheet.isShowing) {
                    binding.mapboxPluginsPickerBottomSheet.dismissPlaceDetails()
                }
            }
        }

        addOnCameraIdleListener {
            Timber.v("Map camera is now idling.")
            binding.mapboxPluginsImageViewMarker.animate().translationY(0f)
                .setInterpolator(OvershootInterpolator()).setDuration(250).start()
            binding.mapboxPluginsPickerBottomSheet.setPlaceDetails(null)
            makeReverseGeocodingSearch(this)
        }

        viewModel.results.observe(this@MainFragment) { carmenFeature ->
            val feature = carmenFeature
                ?: CarmenFeature.builder().placeName(
                    String.format(
                        Locale.US, "[%f, %f]",
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    )
                ).text("No address found").properties(JsonObject()).build()
            binding.mapboxPluginsPickerBottomSheet.setPlaceDetails(feature)
        }
    }

    private fun makeReverseGeocodingSearch(map: MapboxMap) {
        map.cameraPosition.target?.run {
            viewModel.reverseGeocode(
                Point.fromLngLat(longitude, latitude),
                accessToken,
                PlacePickerOptions.builder().build()
            )
        }
    }
}
