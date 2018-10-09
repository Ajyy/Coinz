package com.example.coinz

import com.mapbox.mapboxsdk.geometry.LatLng

data class Point(var id: String, var value: Double, var currency: String, var markerSymbol: String, var markerColor: String, var latlng: LatLng)