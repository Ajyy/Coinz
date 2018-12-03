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
    private var db = FirebaseFirestore.getInstance()

    private var type: String? = null
    private var userClass: User? = null
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
            intent.putExtra("inf", arrayListOf(coinType, "deposit"))
            startActivityForResult(intent, chooseCoinActivity)
        }

        btnSubmitDeposit!!.setOnClickListener {
            getUserData()
            val coinType: String = when{
                rgCoinType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                rgCoinType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                rgCoinType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                else  -> "PENY"
            }

            now = Calendar.getInstance()
            if (type == "time"){
                if (sdf.format(now!!.time) != btnCalendar!!.text){
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

    private fun getUserData(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "get user data: Success")
                        val userData = document.toObject(User::class.java)
                        userClass = userData
                    } else {
                        Toast.makeText(this@DepositSubmitActivity, "Please check your internet", Toast.LENGTH_SHORT)
                        finish()
                        Log.w(tag, "get user data: Fail")
                    }
                }
    }

    private fun storeDeposit(record: Record, coinType: String, type: String){
        userClass!!.balance[coinType]!!.clear()
        for (point in coins) if (!point.isChecked) userClass!!.balance[coinType]!!.add(point)

        if (userClass!!.depositTime == "no"){
            userClass!!.limit = num
            userClass!!.depositTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
        } else {
            val lastCal = Calendar.getInstance()
            lastCal.set(userClass!!.depositTime.substring(6, 10).toInt(), userClass!!.depositTime.substring(0, 2).toInt(),
                    userClass!!.depositTime.substring(3, 5).toInt())
            if (ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant()) > 0){
                userClass!!.limit = num
                userClass!!.depositTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
            } else {
                userClass!!.limit+=num
            }
        }

        if (type == "demand"){
            if (userClass!!.demandTime[record.coinType] != "no"){
                val lastCal = Calendar.getInstance()
                lastCal.set(userClass!!.demandTime[record.coinType]!!.substring(6, 10).toInt(), userClass!!.demandTime[record.coinType]!!.substring(0, 2).toInt(),
                        userClass!!.demandTime[record.coinType]!!.substring(3, 5).toInt())
                val num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
                userClass!!.demandDeposit[record.coinType] = userClass!!.demandDeposit[record.coinType]!! *(1+(0.35/360)*num)
            }

            userClass!!.demandDeposit[record.coinType] = userClass!!.demandDeposit[record.coinType]!!+record.profit+record.deposit
            userClass!!.demandTime[record.coinType] = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
        }

        updateUser(userClass!!,coinType)
        db.collection("users").document(user!!.uid).collection("records").document(record.id)
                .set(record, SetOptions.merge())
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Submit deposit: Success")
                    } else {
                        Log.w(tag, "Submit deposit: Fail")
                        finish()
                        Toast.makeText(this@DepositSubmitActivity, "Submit failed, please check your internet", Toast.LENGTH_SHORT)
                    }
                }
    }

    private fun updateUser(userClass: User, coinType: String){
        db.collection("user").document(user!!.uid).update(
                "demandTime", userClass.demandTime,
                "demandDeposit.$coinType", userClass.demandDeposit[coinType],
                "limit", userClass.limit,
                "depositTime", userClass.depositTime
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail" )
                Toast.makeText(this@DepositSubmitActivity, "Submit failed, please check your internet", Toast.LENGTH_SHORT)
                finish()
            }
        }
    }
}
