package com.example.coinz

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    private var user = FirebaseAuth.getInstance().currentUser

    private var type: String? = null
    private var userData = User()
    private var now: Calendar? = null

    private val tag = "DepositSubmitActivity"
    private val chooseCoinActivity = 2
    private var coins = ArrayList<Point>()
    private var totalValue = 0.0
    private var num = 0

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

        val sdf = SimpleDateFormat("MM/dd/yyyy")

        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, year: Int, monthofYear: Int, dayofMonth: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthofYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayofMonth)
            btnCalendar!!.text = "${monthofYear+1}/$dayofMonth/$year"
        }

        type = intent.getStringExtra("type")
        if (type == "demand"){
            btnCalendar!!.isEnabled = false
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

            if (coins.size != 0){
                if (type == "time"){
                    if (sdf.format(now!!.time) != btnCalendar!!.text && btnCalendar!!.text.toString() != "Calendar"){
                        val profit =  totalValue*0.05
                        val record = Record(System.currentTimeMillis().toString(),"time", coinType, sdf.format(now!!.time), btnCalendar!!.text as String,
                                totalValue, profit)
                        storeDeposit(record, coinType, type!!)

                    } else {
                        tvDepositInf!!.text = "Please choose the expired date"
                    }
                } else if (type == "demand") {
                    val record = Record(System.currentTimeMillis().toString(), "demand", coinType, sdf.format(now!!.time), "null", totalValue, isFinish = true)
                    storeDeposit(record, coinType, type!!)
                }
            } else {
                tvDepositInf!!.text = "Please choose some coins"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == chooseCoinActivity){
            if (resultCode == Activity.RESULT_OK){
                coins = data!!.getSerializableExtra("points") as ArrayList<Point>
                for (point in coins) if (point.isChecked) {
                    totalValue+=point.value
                    num++
                }
                tvCoinInf!!.text = "Coin number: $num Coin Value: ${totalValue}"
            }
        }
    }

    private fun storeDeposit(record: Record, coinType: String, type: String){
        User.deleteBalance(coins, coinType)

        if (userData.depositTime == "no"){
            userData.limit = num
            userData.depositTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
        } else {
            val lastCal = Calendar.getInstance()
            lastCal.set(userData.depositTime.substring(6, 10).toInt(), userData.depositTime.substring(0, 2).toInt(),
                    userData.depositTime.substring(3, 5).toInt())
            if (ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant()) > 0){
                userData.limit = num
                userData.depositTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
            } else {
                userData.limit+=num
            }
        }

        if (type == "demand"){
            if (userData.demandTime[record.coinType] != "no"){
                val lastCal = Calendar.getInstance()
                lastCal.set(userData.demandTime[record.coinType]!!.substring(6, 10).toInt(), userData.demandTime[record.coinType]!!.substring(0, 2).toInt(),
                        userData.demandTime[record.coinType]!!.substring(3, 5).toInt())
                val num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
                userData.demandDeposit[record.coinType] = userData.demandDeposit[record.coinType]!! *(1+(0.35/360)*num)
            }

            userData.demandDeposit[record.coinType] = userData.demandDeposit[record.coinType]!!+record.profit+record.deposit
            userData.demandTime[record.coinType] = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
        }

        userData.updateDemand(coinType, user!!.uid)
        User.addRecord(record)
    }
}
