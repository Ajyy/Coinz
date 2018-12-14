package com.example.coinz

import java.io.Serializable

// Record class, please check document to see detail information
data class Record(var id: String? = null, var type: String? = null, var coinType: String? = null, var begin: String? = null, var end: String? = null, var deposit: Double = 0.0, var interest: Double = 0.0, var finish: Boolean = false): Serializable