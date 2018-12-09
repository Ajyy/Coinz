package com.example.coinz

import java.io.Serializable

data class Coin(var id: String? = null, var value: Double? = 0.0, var type: String? = null, var markerSymbol: String? = null, var markerColor: String? = null, var latitude: Double = 0.0, var longitude: Double = 0.0, var isChecked: Boolean? = false): Serializable