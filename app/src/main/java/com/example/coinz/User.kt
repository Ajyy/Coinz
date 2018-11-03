package com.example.coinz

class User(){
    var name: String? = null
    var email: String? = null
    var age: Int? = 0
    var gender: String? = null
    var balance: Double = 0.0
    var todayStep: Int = 0

    constructor(name: String = "SetYourName", email: String, age: Int = 0, gender: String = "Unknown", balance: Double = 0.0, todayStep: Int = 0): this(){
        this.name = name
        this.email = email
        this.age = age
        this.gender = gender
        this.balance = balance
        this.todayStep = todayStep
    }
}
