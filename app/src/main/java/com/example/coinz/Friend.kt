package com.example.coinz

import java.io.Serializable

data class Friend(var uid: String, var name: String = "Unknown", var email: String = "Unknown", var age: Int = 0, var gender: String = "Unknown", var todaySteps: Int = 0, var isVerified: Boolean = false, var isAccept: Boolean = false): Serializable