package com.example.coinz

class User {
    var name  = "SetYourName"
    var email: String? = null
    var age: Int? = 0
    var gender = "Unknown"
    var balance = mutableMapOf("SHIL" to ArrayList<Point>(), "DOLR" to ArrayList<Point>(), "QUID" to ArrayList<Point>(), "PENY" to ArrayList<Point>())
    var gold = 0.0
    var todayStep = 0
    var demandDeposit = mutableMapOf("GOLD" to 0.0, "SHIL" to 0.0, "DOLR" to 0.0, "QUID" to 0.0, "PENY" to 0.0)
    var demandTime = mutableMapOf("GOLD" to "no", "SHIL" to "no", "DOLR" to "no", "QUID" to "no", "PENY" to "no")
    var achievements = ArrayList<Int>()
    var limit = 0
    var depositTime = "no"
    var isExchange = false
    var coinsId = ArrayList<String>()
}
