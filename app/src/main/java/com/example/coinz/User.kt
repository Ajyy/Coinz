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
    var demandDeposit = mutableMapOf("GOLD" to 0.0, "SHIL" to 0.0, "DOLR" to 0.0, "QUID" to 0.0, "PENY" to 0.0)
    var demandTime = mutableMapOf("GOLD" to "no", "SHIL" to "no", "DOLR" to "no", "QUID" to "no", "PENY" to "no")
    var achievements = ArrayList<Int>()
    var limit = 0
    var depositTime = "no"
    var isExchange = false
    var coinsId = ArrayList<String>()

    companion object {
        var mAuth = FirebaseAuth.getInstance()
        var userAuth = mAuth.currentUser
        var userDb = FirebaseFirestore.getInstance().collection("users")
        private val tag = "User"

        fun deleteBalance(coins: ArrayList<Coin>, coinType: String, id: String){
            val realId = if (id == "self") userAuth!!.uid else id
            for (coin in coins){
                userDb.document(realId)
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

        fun addRecord(record: Record, id: String){
            val realId = if (id == "self") userAuth!!.uid else id
            userDb.document(realId).collection("records").document(record.id!!)
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
        userDb.document(userAuth!!.uid).get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        userData = task.result!!.toObject(User::class.java)
                        name = userData!!.name
                        email = userData!!.email
                        age = userData!!.age
                        gender = userData!!.gender
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

    fun updateDemand(coinType: String){
        userDb.document(userAuth!!.uid).update(
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