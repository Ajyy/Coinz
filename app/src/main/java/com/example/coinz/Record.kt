package com.example.coinz

import java.io.Serializable

data class Record(var id: String? = null, var type: String? = null, var coinType: String? = null, var begin: String? = null, var end: String? = null, var deposit: Double = 0.0, var profit: Double = 0.0, var isFinish: Boolean = false): Serializable