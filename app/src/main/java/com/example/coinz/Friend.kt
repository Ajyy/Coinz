package com.example.coinz

import java.io.Serializable

data class Friend(var uid: String = "Null", var name: String = "Null", var email: String = "Null", var age: Int = 0, var gender: String = "Unknown", var todaySteps: Int = 0, var isVerified: Long = -2, var isAccepted: Long = -2): Serializable