package com.example.coinz

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import java.time.temporal.ChronoUnit
import java.util.*

// This class is used for exchange money to another type
class ExchangeActivity : AppCompatActivity() {
    private var etExchangeAmount: EditText? = null
    private var tvExchangeInf: TextView? = null
    private var tvExchangeInf2: TextView? = null
    private var btnSubmitExchange: Button? = null
    private var btnCoinTypeF: Button? = null
    private var btnCoinTypeT: Button? = null

    private val tag = "ExchangeActivity"
    private val now = Calendar.getInstance()
    private var userData = User()
    private var coinTypes = arrayOf("GOLD", "GOLD")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange)

        userData.getData()
        etExchangeAmount = findViewById(R.id.etExchangeAmount)
        tvExchangeInf = findViewById(R.id.tvExchangeInf)
        tvExchangeInf2 = findViewById(R.id.tvExchangeInf2)
        btnSubmitExchange = findViewById(R.id.btnSubmitExchange)
        btnCoinTypeF = findViewById(R.id.btnCoinTypeF)
        btnCoinTypeT = findViewById(R.id.btnCoinTypeT)

        // Choose the money type exchange from
        btnCoinTypeF!!.setOnClickListener {
            showAlertDialogButtonClicked(0)
        }

        // Choose the money type exchange to
        btnCoinTypeT!!.setOnClickListener {
            showAlertDialogButtonClicked(1)
            btnCoinTypeT!!.text = coinTypes[1]
        }

        btnSubmitExchange!!.setOnClickListener {
            // Get the amount and the rate
            val amount = etExchangeAmount!!.text.toString().toDouble()
            val rate = Coin.ratesArr[coinTypes[1]]!!/ Coin.ratesArr[coinTypes[0]]!!

            if (amount > 0){
                if (amount < userData.demandDeposit[coinTypes[0]]!!){
                    // Update the demand deposit balance first of all
                    updateBalance(coinTypes[0])
                    updateBalance(coinTypes[1])

                    userData.demandDeposit[coinTypes[0]] = userData.demandDeposit[coinTypes[0]]!!-amount
                    userData.demandDeposit[coinTypes[1]] = userData.demandDeposit[coinTypes[1]]!!+amount*rate

                    updateUser()
                } else {
                    tvExchangeInf2!!.text = getString(R.string.exchange_inf_less_than_balance)
                }
            } else {
                tvExchangeInf2!!.text = getString(R.string.exchange_inf_larger_than_zero)
            }
        }
    }

    // Alert Dialog for choose money's type
    @SuppressLint("SetTextI18n")
    private fun showAlertDialogButtonClicked(index: Int){
        val builder = AlertDialog.Builder(this@ExchangeActivity)
        builder.setTitle("Choose coin's type")

        val types = arrayOf("GOLD", "PENY", "SHIL", "DOLR", "QUID")
        builder.setItems(types) { _: DialogInterface, i: Int ->
            coinTypes[index] = types[i]
            if (index == 0) btnCoinTypeF!!.text = coinTypes[0]
            else btnCoinTypeT!!.text = coinTypes[1]

            // Set the exchange rate information
            if (!(btnCoinTypeF!!.text.toString() !in types||btnCoinTypeT!!.text.toString() !in types))
                tvExchangeInf!!.text = "The rate of ${coinTypes[1]}:${coinTypes[0]} is" +
                    " ${Coin.ratesArr[coinTypes[1]]!!/ Coin.ratesArr[coinTypes[0]]!!}"
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Update demand deposit balance
    private fun updateBalance(coinType: String){
        if (userData.demandTime[coinType] != "no"){
            val lastCal = Calendar.getInstance()
            lastCal.set(userData.demandTime[coinType]!!.substring(6, 10).toInt(), userData.demandTime[coinType]!!.substring(0, 2).toInt(),
                    userData.demandTime[coinType]!!.substring(3, 5).toInt())
            var num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
            if (num == 0L){
                if (now.get(Calendar.DAY_OF_MONTH) != userData.demandTime[coinType]!!.substring(3, 5).toInt()){
                    num++
                }
            } else if (num > 0){
                num++
            }

            userData.demandDeposit[coinType] = userData.demandDeposit[coinType]!! *(1+(0.35/360)*num)
        }

        userData.demandTime[coinType] = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
    }

    // Update player's demand information
    private fun updateUser(){
        User.userDb.document(User.userAuth!!.uid).update(
                "demandDeposit.${coinTypes[0]}", userData.demandDeposit[coinTypes[0]],
                "demandDeposit.${coinTypes[1]}", userData.demandDeposit[coinTypes[1]],
                "demandTime.${coinTypes[0]}", userData.demandTime[coinTypes[0]],
                "demandTime.${coinTypes[1]}", userData.demandTime[coinTypes[1]]
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail", task.exception)
            }

            finish()
        }
    }
}
