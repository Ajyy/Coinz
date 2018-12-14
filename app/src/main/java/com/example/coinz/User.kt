package com.example.coinz

import android.support.annotation.VisibleForTesting
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// User class, please check document to see detail information
class User {
    var name  = "SetYourName"
    var email: String? = null
    var age: Int? = 0
    var gender = "Unknown"
    var totalBal = 0.0
    var demandDeposit = mutableMapOf("GOLD" to 0.0, "SHIL" to 0.0, "DOLR" to 0.0, "QUID" to 0.0, "PENY" to 0.0)
    var demandTime = mutableMapOf("GOLD" to "no", "SHIL" to "no", "DOLR" to "no", "QUID" to "no", "PENY" to "no")
    var achievements = ArrayList<Long>()
    var limit = 0
    var depositTime = "no"
    var exchange = false
    var verified = false
    var coinsId = ArrayList<String>()

    companion object {
        var mAuth = FirebaseAuth.getInstance()
        var userAuth = mAuth.currentUser
        var userDb = FirebaseFirestore.getInstance().collection("users")
        private val tag = "User"

        // Delete some coins when exchange with friends or deposit to bank
        fun deleteBalance(coins: ArrayList<Coin>, coinType: String, id: String){
            val realId = if (id == "self") userAuth!!.uid else id
            for (coin in coins){
                userDb.document(realId)
                        .collection("balance_$coinType").document(coin.id!!).delete()
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful){
                                Log.d(tag, "delete coin: Success")
                            } else {
                                Log.w(tag, "delete coin: fail", task.exception)
                            }
                        }
            }
        }

        // add record to user data
        fun addRecord(record: Record, id: String){
            val realId = if (id == "self") userAuth!!.uid else id
            userDb.document(realId).collection("records").document(record.id!!)
                    .set(record, SetOptions.merge())
                    .addOnCompleteListener {task ->
                        if (task.isSuccessful){
                            Log.d(tag, "add record: Success")
                        } else {
                            Log.w(tag, "add record: Fail", task.exception)
                        }
                    }
        }

        // Reload User
        fun reloadUser(){
            mAuth.currentUser!!.reload().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if (task.isSuccessful){
                        userAuth = mAuth.currentUser
                        if (User.userAuth!!.isEmailVerified){
                            User.setVerified()
                        }

                        Log.d(tag, "Reload: Success")
                    } else {
                        Log.w(tag, "Reload: Fail", task.exception)
                    }
                }
            }
        }

        fun setVerified(){
            userDb.document(userAuth!!.uid).get()
                    .addOnCompleteListener {task1 ->
                        if (task1.isSuccessful){
                            if (!(task1.result!!["verified"] as Boolean)){
                                userDb.document(userAuth!!.uid).update("verified", true)
                                        .addOnCompleteListener {task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "Update verify information: Success")
                                            } else {
                                                Log.w(tag, "Update verify information: Fail", task2.exception)
                                            }
                                        }
                            }
                            Log.d(tag, "Get verify information: Success")
                        } else {
                            Log.w(tag, "Get verify information: Fail", task1.exception)
                        }
                    }
        }

        @VisibleForTesting
        fun getUser() = userAuth
    }

    // Get user data
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
                        exchange = userData!!.exchange
                        coinsId = userData!!.coinsId
                        verified = userData!!.verified
                        Log.d(tag, "get user data: Success")
                    } else {
                        Log.w(tag, "get user data: fail", task.exception)
                    }
                }
    }

    // Update the demand
    fun updateDemand(coinType: String){
        userDb.document(userAuth!!.uid).update(
                "demandTime.$coinType", demandTime[coinType],
                "demandDeposit.$coinType", demandDeposit[coinType],
                "limit", limit,
                "depositTime", depositTime,
                "exchange", exchange
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail" , task.exception)
            }
        }
    }

    // add achievement
    fun addAchievement(achievementId: Long){
        achievements.add(achievementId)
        userDb.document(userAuth!!.uid).update("achievements", FieldValue.arrayUnion(achievementId))
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Submit deposit: Success")
                    } else {
                        Log.w(tag, "Submit deposit: Fail", task.exception)
                    }
                }
    }

    // add to total balance of user
    fun addBalance(value: Double, coinType: String){
        totalBal+= value/Coin.ratesArr[coinType]!!
        userDb.document(userAuth!!.uid).update("totalBal", totalBal)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Add balance: Success")
                    } else {
                        Log.w(tag, "Add balance", task.exception)
                    }
                }
    }
}
