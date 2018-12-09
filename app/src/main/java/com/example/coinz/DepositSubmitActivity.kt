package com.example.coinz

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_deposit_submit.*
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList


class DepositSubmitActivity : AppCompatActivity(){

    private var rgCoinType: RadioGroup? = null
    private var btnChooseDeposit: Button? = null
    private var btnCalendar: Button? = null
    private var btnSubmitDeposit: Button? = null
    private var tvDepositInf: TextView? = null
    private var tvFromDemand: TextView? = null
    private var tvEndTime: TextView? = null
    private var etTimeDemand: EditText? = null

    private var type: String? = null
    private var userData = User()
    private var now: Calendar? = null
    private val chooseCoinActivity = 2
    private var coins = ArrayList<Coin>()
    private var totalCoinValue = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit_submit)

        title = "Central Bank"
        userData.getData()

        rgCoinType = findViewById(R.id.rgCoinType)
        btnChooseDeposit = findViewById(R.id.btnChooseDeposit)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnSubmitDeposit = findViewById(R.id.btnSubmitDeposit)
        tvDepositInf = findViewById(R.id.tvDepositInf)
        tvFromDemand = findViewById(R.id.tvFromDemand)
        tvEndTime = findViewById(R.id.tvEndTime)
        etTimeDemand = findViewById(R.id.etTimeDemand)

        val sdf = SimpleDateFormat("MM/dd/yyyy")

        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener{ _: DatePicker, year: Int, monthofYear: Int, dayofMonth: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthofYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayofMonth)
            btnCalendar!!.text = String.format("%02d/%02d/%d", (monthofYear+1), dayofMonth, year)
        }

        type = intent.getStringExtra("type")
        if (type == "demand"){
            btnCalendar!!.visibility = View.GONE
            tvEndTime!!.visibility = View.GONE
            tvFromDemand!!.visibility = View.GONE
            etTimeDemand!!.visibility = View.GONE

        } else {
            btnCalendar!!.setOnClickListener {
                val datePickerDialog = DatePickerDialog(this@DepositSubmitActivity, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                datePickerDialog.show()
            }
        }

        rgCoinType!!.setOnClickListener{
            coins.clear()
            tvCoinInf!!.text = "Please choose coins"
        }

        btnChooseDeposit!!.setOnClickListener {
            val coinType: String = when{
                rgCoinType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                rgCoinType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                rgCoinType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                else  -> "PENY"
            }

            val intent = Intent(this@DepositSubmitActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayOf(coinType, "deposit"))
            startActivityForResult(intent, chooseCoinActivity)
        }

        btnSubmitDeposit!!.setOnClickListener {
            val coinType: String = when{
                rgCoinType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                rgCoinType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                rgCoinType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                else  -> "PENY"
            }

            now = Calendar.getInstance()

            if (type == "time"){
                val fromDemand = etTimeDemand!!.text.toString().toDouble()
                if (coins.size != 0||fromDemand > 0){
                    val time = btnCalendar!!.text.toString()
                    if (sdf.format(now!!.time) !=  time&& time != "Calendar"){
                        val dayNum = getDifferentToNow(time)
                        val rate = when {
                            dayNum <= 7 -> 0.05
                            dayNum in 8..21 -> 0.10
                            dayNum in 22..42 -> 0.20
                            else -> 0.40
                        }
                        val profit =  (totalCoinValue+fromDemand)*rate

                        val record = Record(System.currentTimeMillis().toString(),"time", coinType, sdf.format(now!!.time), btnCalendar!!.text as String,
                                totalCoinValue+fromDemand, profit)
                        storeDeposit(record, coinType, type!!)
                    } else {
                        tvDepositInf!!.text = "Please choose the expired date"
                    }
                } else {
                    tvDepositInf!!.text = "Please choose some coins or enter the amount from your demand deposit"
                }
            } else if (type == "demand") {
                if (coins.size != 0){
                    val record = Record(System.currentTimeMillis().toString(), "demand", coinType, sdf.format(now!!.time), "null", totalCoinValue, isFinish = true)
                    storeDeposit(record, coinType, type!!)
                } else {
                    tvDepositInf!!.text = "Please choose some coins"
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == chooseCoinActivity){
            if (resultCode == Activity.RESULT_OK){
                coins = data!!.getSerializableExtra("points") as ArrayList<Coin>
                for (coin in coins) if (coin.isChecked!!) {
                    totalCoinValue+= coin.value!!
                }

                tvCoinInf!!.text = "Coin number: ${coins.size} Coin Value: ${totalCoinValue}"
            }
        }
    }

    private fun storeDeposit(record: Record, coinType: String, type: String){
        User.deleteBalance(coins, coinType, "self")

        if (userData.depositTime == "no"){
            userData.limit = coins.size
            userData.depositTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
        } else {
            val dayNum = getDifferentToNow(userData.demandTime[record.coinType]!!)
            if (dayNum > 0){
                userData.limit = coins.size
                userData.depositTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
            } else {
                userData.limit+=coins.size
            }
        }

        if (type == "demand"){
            if (userData.demandTime[record.coinType] != "no"){
                val dayNum = getDifferentToNow(userData.demandTime[record.coinType]!!)
                userData.demandDeposit[record.coinType!!] = userData.demandDeposit[record.coinType!!]!! *(1+(0.35/360)*dayNum)
            }

            userData.demandDeposit[record.coinType!!] = userData.demandDeposit[record.coinType!!]!!+record.interest+record.deposit
            userData.demandTime[record.coinType!!] = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
            userData.updateDemand(coinType)
        }

        User.addRecord(record, "self")
        finish()
    }

    private fun getDifferentToNow(time: String): Long{
        val lastCal = Calendar.getInstance()
        lastCal.set(time.substring(6, 10).toInt(), time.substring(0, 2).toInt(),
                time.substring(3, 5).toInt())
        return ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
    }
}
