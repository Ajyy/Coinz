package com.example.coinz

import android.graphics.Bitmap
import java.io.Serializable

data class Friend(var name: String, var email: String, var age: Int, var gender: String, var todaySteps: Int = 0, var isVerified: Boolean = false): Serializable