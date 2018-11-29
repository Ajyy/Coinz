package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.temporal.ChronoUnit
import java.util.*

class DrawMoneyActivity : AppCompatActivity() {

    private var rgCoinTypeDraw: RadioGroup? = null
    private var etAmountDraw: EditText? = null
    private var btnSubmitDraw: Button? = null
    private var tvDrawInf: TextView? = null

    private var user = FirebaseAuth.getInstance().currentUser
    private var db = FirebaseFirestore.getInstance()

    private var tag = "DrawMoneyActivity"
    private var userClass: User? = null
    private var now = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_money)
        getUserData()
        title = "Central Money"

        rgCoinTypeDraw = findViewById(R.id.rgCoinTypeDraw)
        etAmountDraw = findViewById(R.id.etAmountDraw)
        btnSubmitDraw = findViewById(R.id.btnSubmitDraw)
        tvDrawInf = findViewById(R.id.tvDrawInf)

        btnSubmitDraw!!.setOnClickListener {
            val coinType: String = when{
                rgCoinTypeDraw!!.checkedRadioButtonId == R.id.rbGold -> "GOLD"
                rgCoinTypeDraw!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                rgCoinTypeDraw!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                rgCoinTypeDraw!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                else  -> "PENY"
            }

            if (etAmountDraw!!.text.toString().toDouble() > 0.0) {
                if (etAmountDraw!!.text.toString().toDouble() <= userClass!!.balance[coinType]!!) {
                    val lastCal = Calendar.getInstance()
                    lastCal.set(userClass!!.demandTime[coinType]!!.substring(6, 10).toInt(), userClass!!.demandTime[coinType]!!.substring(0, 2).toInt(),
                            userClass!!.demandTime[coinType]!!.substring(3, 5).toInt())
                    val num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
                    userClass!!.demandDeposit[coinType] = userClass!!.demandDeposit[coinType]!! *(1+(0.35/360)*num)
                    userClass!!.demandDeposit[coinType] = userClass!!.demandDeposit[coinType]!!-etAmountDraw.toString().toDouble()
                    updateUser()
                    finish()
                } else {
                    tvDrawInf!!.text = "Amount should be smaller than your balance"
                }
            } else {
                tvDrawInf!!.text = "Amount should be larger than 0"
            }
        }
    }

    private fun getUserData(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "get user data: Success")
                        val userData = document.toObject(User::class.java)
                        userClass = userData
                    } else {
                        Toast.makeText(this@DrawMoneyActivity, "Please check your internet", Toast.LENGTH_SHORT)
                        finish()
                        Log.w(tag, "get user data: Fail")
                    }
                }
    }

    private fun updateUser(){
        db.collection("user").document(user!!.uid).update(
                "demandDeposit", userClass!!.demandDeposit,
                "demandTime", userClass!!.demandTime
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail" )
                Toast.makeText(this@DrawMoneyActivity, "Submit failed, please check your internet", Toast.LENGTH_SHORT)
                finish()
            }
        }
    }
}
