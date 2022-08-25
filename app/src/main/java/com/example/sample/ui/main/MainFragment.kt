package com.example.sample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sample.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.io.FileOutputStream

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(requireContext())
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Copy asset "trails.mbtiles" to internal storage of APP
        val mbtiles = File(requireContext().filesDir, "trails.mbtiles")
        if (!mbtiles.exists()) {
            requireContext().assets.open("trails.mbtiles").use { inputStream ->
                val outputFile = File(mbtiles.path)
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }
            }
        }

        mapView.getMapAsync { map ->
            // Load simple style to read MBTiles
            Style.Builder()
                .fromJson(
                    """
                        {
                            "sources": {
                                "mbtiles": {
                                    "type": "vector",
                                    "url": "mbtiles://${mbtiles.path}"
                                }
                            },
                            "layers": [
                                {
                                    "id": "trails",
                                    "type": "line",
                                    "source": "mbtiles",
                                    "source-layer": "multilinestring-glacier_trails",
                                    "paint": {
                                        "line-color": "red"
                                    }
                                }
                            ]
                        }
                    """
                )
                .let(map::setStyle)

            map.cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(48.640200, -113.834800)
                )
                .zoom(10.0)
                .build()
        }
    }
}