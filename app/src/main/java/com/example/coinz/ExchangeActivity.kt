package com.example.coinz

import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.temporal.ChronoUnit
import java.util.*

class ExchangeActivity : AppCompatActivity() {

    private var etExchangeAmount: EditText? = null
    private var tvExchangeInf: TextView? = null
    private var tvExchangeInf2: TextView? = null
    private var btnSubmitExchange: Button? = null
    private var btnCoinTypeF: Button? = null
    private var btnCoinTypeT: Button? = null

    private var user = FirebaseAuth.getInstance().currentUser
    private var db = FirebaseFirestore.getInstance()

    private val tag = "ExchangeActivity"
    private val now = Calendar.getInstance()
    private var userData: User? = null
    private var ratesArray: HashMap<String, Double>? = null
    private var coinTypes = arrayOf("GOLD", "GOLD")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange)

        getUserData()
        ratesArray = intent.getSerializableExtra("rates") as HashMap<String, Double>

        etExchangeAmount = findViewById(R.id.etExchangeAmount)
        tvExchangeInf = findViewById(R.id.tvExchangeInf)
        tvExchangeInf2 = findViewById(R.id.tvExchangeInf2)
        btnSubmitExchange = findViewById(R.id.btnSubmitExchange)
        btnCoinTypeF = findViewById(R.id.btnCoinTypeF)
        btnCoinTypeT = findViewById(R.id.btnCoinTypeT)

        btnCoinTypeF!!.setOnClickListener {v ->
            showAlertDialogButtonClicked(v, 0)
        }

        btnCoinTypeT!!.setOnClickListener {v ->
            showAlertDialogButtonClicked(v, 1)
            btnCoinTypeT!!.text = coinTypes[1]
        }

        btnSubmitExchange!!.setOnClickListener {
            val amount = etExchangeAmount!!.text.toString().toDouble()
            val rate = ratesArray!![coinTypes[1]]!!/ ratesArray!![coinTypes[0]]!!

            if (amount > 0){
                if (amount < userData!!.demandDeposit[coinTypes[0]]!!){
                    updateBalance(coinTypes[0])
                    updateBalance(coinTypes[1])

                    userData!!.demandDeposit[coinTypes[0]] = userData!!.demandDeposit[coinTypes[0]]!!-amount
                    userData!!.demandDeposit[coinTypes[1]] = userData!!.demandDeposit[coinTypes[1]]!!+amount*rate

                    updateUser()
                } else {
                    tvExchangeInf2!!.text = "Amount Should be smaller than your balance"
                }
            } else {
                tvExchangeInf2!!.text = "Amount should be larger than 0"
            }
        }
    }

    private fun showAlertDialogButtonClicked(v: View, index: Int){
        val builder = AlertDialog.Builder(this@ExchangeActivity)
        builder.setTitle("Choose coin's type")

        val types = arrayOf("GOLD", "PENY", "SHIL", "DOLR", "QUID")
        builder.setItems(types) { _: DialogInterface, i: Int ->
            coinTypes[index] = types[i]
            if (index == 0) btnCoinTypeF!!.text = coinTypes[0]
            else btnCoinTypeT!!.text = coinTypes[1]

            if (!(btnCoinTypeF!!.text.toString() !in types||btnCoinTypeT!!.text.toString() !in types))
                tvExchangeInf!!.text = "The rate of ${coinTypes[1]}:${coinTypes[0]} is" +
                    " ${ratesArray!![coinTypes[1]]!!/ ratesArray!![coinTypes[0]]!!}"
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateBalance(coinType: String){
        if (userData!!.demandTime[coinType] != "no"){
            val lastCal = Calendar.getInstance()
            lastCal.set(userData!!.demandTime[coinType]!!.substring(6, 10).toInt(), userData!!.demandTime[coinType]!!.substring(0, 2).toInt(),
                    userData!!.demandTime[coinType]!!.substring(3, 5).toInt())
            val num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
            userData!!.demandDeposit[coinType] = userData!!.demandDeposit[coinType]!! *(1+(0.35/360)*num)
        }

        userData!!.demandTime[coinType] = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
    }

    private fun getUserData(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "get user data: Success")
                        userData = document.toObject(User::class.java)
                    } else {
                        Toast.makeText(this@ExchangeActivity, "Please check your internet", Toast.LENGTH_SHORT)
                        finish()
                        Log.w(tag, "get user data: Fail")
                    }
                }
    }

    private fun updateUser(){
        db.collection("user").document(user!!.uid).update(
                "demandDeposit", userData!!.demandDeposit,
                "demandTime", userData!!.demandTime
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail" )
                Toast.makeText(this@ExchangeActivity, "Submit failed, please check your internet", Toast.LENGTH_SHORT)
                finish()
            }
        }
    }
}
