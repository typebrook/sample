package com.example.sample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sample.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(
            requireContext(),
            "pk.eyJ1IjoidHlwZWJyb29rIiwiYSI6ImNqNHVyaTc5dDBuazczMm1jenl3cG8wb3IifQ.2UEZ-jiHgHvYYqVirXhgpw"
        )
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            val intent = PlacePicker.IntentBuilder()
                    .accessToken("pk.eyJ1IjoidHlwZWJyb29rIiwiYSI6ImNqNHVyaTc5dDBuazczMm1jenl3cG8wb3IifQ.2UEZ-jiHgHvYYqVirXhgpw")
                    .placeOptions(PlacePickerOptions.builder().build())
                    .build(requireActivity())
            startActivityForResult(intent, 8888)
        }
    }
}
