package com.example.sample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sample.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import kotlinx.android.synthetic.main.main_fragment.*
import org.json.JSONObject
import timber.log.Timber


class MainFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(requireContext(), null)
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(mapView) {
            getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri("asset://rudymap.json"))
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(25.023167, 121.585674))
                    .zoom(12.0)
                    .build()

                setOffline()
            }
        }
    }

    private fun setOffline() = with(OfflineManager.getInstance(requireContext())) {
        setOfflineMapboxTileCountLimit(10000000L)

        val latLngBounds = LatLngBounds.Builder()
            .include(LatLng(25.280, 121.983)) // Northeast
            .include(LatLng(21.910, 119.955)) // Southwest
            .build()

// Define the offline region
        val definition = OfflineTilePyramidRegionDefinition(
            "https://typebrook.github.io/mapstew/styles/rudymap.json",
            latLngBounds,
            10.0,
            14.0,
            resources.displayMetrics.density
        )

        // Implementation that uses JSON to store Yosemite National Park as the offline region name.
        val metadata: ByteArray = try {
            val jsonObject = JSONObject()
            jsonObject.put("Taiwan", "Taiwan Boundary")
            val json = jsonObject.toString()
            json.toByteArray()
        } catch (exception: Exception) {
            Timber.e("Failed to encode metadata: " + exception.message)
            null
        } ?: return

        createOfflineRegion(definition, metadata,
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                    // Monitor the download progress using setObserver
                    offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                        override fun onStatusChanged(status: OfflineRegionStatus) {

                            // Calculate the download percentage
                            val percentage = if (status.requiredResourceCount >= 0)
                                100.0 * status.completedResourceCount /status.requiredResourceCount else 0.0

                            if (status.isComplete) {
                                // Download complete
                                Timber.d("Region downloaded successfully.")
                            } else if (status.isRequiredResourceCountPrecise) {
                                Timber.d("Finished: $percentage")
                            }
                        }

                        override fun onError(error: OfflineRegionError) {
                            // If an error occurs, print to logcat
                            Timber.e("onError reason: " + error.reason)
                            Timber.e("onError message: " + error.message)
                        }

                        override fun mapboxTileCountLimitExceeded(limit: Long) {
                            // Notify if offline region exceeds maximum tile count
                            Timber.e("Mapbox tile count limit exceeded: $limit")
                        }
                    })
                }

                override fun onError(error: String) {
                    Timber.e("Error: $error")
                }
            })
    }
}
