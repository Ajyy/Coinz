package com.example.coinz

import java.io.Serializable

data class Record(var type: String, var begin: String, var end: String, var deposit: Double, var profit: Double = 0.0, var isFinish: Boolean = false): Serializable