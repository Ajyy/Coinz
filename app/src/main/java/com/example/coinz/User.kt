package com.example.coinz

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class User {
    var name  = "SetYourName"
    var email: String? = null
    var age: Int? = 0
    var gender = "Unknown"
    var gold = 0.0
    var todayStep = 0
    var demandDeposit = mutableMapOf("GOLD" to 0.0, "SHIL" to 0.0, "DOLR" to 0.0, "QUID" to 0.0, "PENY" to 0.0)
    var demandTime = mutableMapOf("GOLD" to "no", "SHIL" to "no", "DOLR" to "no", "QUID" to "no", "PENY" to "no")
    var achievements = ArrayList<Int>()
    var limit = 0
    var depositTime = "no"
    var isExchange = false
    var coinsId = ArrayList<String>()

    companion object {
        private var mAuth: FirebaseAuth? = FirebaseAuth.getInstance()
        private val tag = "User"

        fun addCoins(coins: ArrayList<Point>){
            val db = FirebaseFirestore.getInstance().collection("users").document(mAuth!!.currentUser!!.uid)
            for (coin in coins){
                db.collection("balance_"+coin.currency).document(coin.id!!)
                        .set(coin, SetOptions.merge())
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful){
                                Log.d(tag, "update balance: Success")
                            } else {
                                Log.w(tag, "update balance: fail")
                            }
                        }

                db.update("coinsId", FieldValue.arrayUnion(coin.id))
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful){
                                Log.d(tag, "update balance: Success")
                            } else {
                                Log.w(tag, "update balance: fail")
                            }
                        }
            }
        }

        fun deleteBalance(coins: ArrayList<Point>, coinType: String){
            for (coin in coins){
                FirebaseFirestore.getInstance().collection("users").document(mAuth!!.currentUser!!.uid)
                        .collection("balance_$coinType").document(coin.id!!).delete()
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful){
                                Log.d(tag, "delete coin: Success")
                            } else {
                                Log.w(tag, "delete coin: fail")
                            }
                        }
            }
        }

        fun addRecord(record: Record){
            FirebaseFirestore.getInstance().collection("users").document(mAuth!!.currentUser!!.uid).collection("records").document(record.id!!)
                    .set(record, SetOptions.merge())
                    .addOnCompleteListener {task ->
                        if (task.isSuccessful){
                            Log.d(tag, "Submit deposit: Success")
                        } else {
                            Log.w(tag, "Submit deposit: Fail")
                        }
                    }
        }
    }

    fun getData(){
        var userData: User?
        FirebaseFirestore.getInstance().collection("users").document(mAuth!!.currentUser!!.uid).get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        userData = task.result!!.toObject(User::class.java)
                        name = userData!!.name
                        email = userData!!.email
                        age = userData!!.age
                        gender = userData!!.gender
                        gold = userData!!.gold
                        todayStep = userData!!.todayStep
                        demandDeposit = userData!!.demandDeposit
                        achievements = userData!!.achievements
                        limit = userData!!.limit
                        depositTime = userData!!.depositTime
                        isExchange = userData!!.isExchange
                        coinsId = userData!!.coinsId
                        Log.d(tag, "get user data: Success")
                    } else {
                        Log.w(tag, "get user data: fail")
                    }
                }
    }

    fun updateDemand(coinType: String, id: String){
        FirebaseFirestore.getInstance().collection("users").document(id).update(
                "demandTime.$coinType", demandTime[coinType],
                "demandDeposit.$coinType", demandDeposit[coinType],
                "limit", limit,
                "depositTime", depositTime
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail" , task.exception)
            }
        }
    }
}
