package com.example.coinz

class User(){
    var name: String? = null
    var email: String? = null
    var age: Int? = 0
    var gender: String? = null
    var balance = mutableMapOf("GOLD" to 0.0, "SHIL" to 0.0, "DOLR" to 0.0, "QUID" to 0.0, "PENY" to 0.0)
    var todayStep: Int = 0

    constructor(name: String = "SetYourName", email: String, age: Int = 0, gender: String = "Unknown", todayStep: Int = 0): this(){
        this.name = name
        this.email = email
        this.age = age
        this.gender = gender
        this.todayStep = todayStep
    }
}
