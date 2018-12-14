package com.example.coinz

import java.io.Serializable

// Friend class, please check document to see detail information
data class Friend(var uid: String = "Null", var name: String = "Null", var email: String = "Null", var age: Int = 0, var totalBal: Double = 0.0, var gender: String = "Unknown", var isVerified: Long = -2, var isAccepted: Long = -2): Serializable