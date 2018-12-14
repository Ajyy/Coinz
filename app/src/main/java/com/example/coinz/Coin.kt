package com.example.coinz

import java.io.Serializable

// Coin class, please check document to see detail information
class Coin(var id: String? = null, var value: Double? = 0.0, var type: String? = null, var markerSymbol: String? = null, var markerColor: String? = null, var latitude: Double = 0.0, var longitude: Double = 0.0, var checked: Boolean? = false): Serializable{
    companion object {
        val ratesArr = hashMapOf("SHIL" to 0.0, "DOLR" to 0.0, "PENY" to 0.0, "QUID" to 0.0, "GOLD" to 1.0)
    }
}