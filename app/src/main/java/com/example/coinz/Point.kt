package com.example.coinz

import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.Serializable

data class Point( var id: String? = null, var value: Double? = 0.0, var currency: String? = null, var markerSymbol: String? = null, var markerColor: String? = null, var latlng: LatLng? = null, var isChecked: Boolean? = false): Serializable