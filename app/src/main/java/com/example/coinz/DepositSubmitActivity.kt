package com.example.coinz

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.temporal.ChronoUnit
import java.util.*


class DepositSubmitActivity : AppCompatActivity(){

    private var rgCoinType: RadioGroup? = null
    private var etAmount: EditText? = null
    private var btnCalendar: Button? = null
    private var btnSubmitDeposit: Button? = null
    private var tvDepositInf: TextView? = null

    private var user = FirebaseAuth.getInstance().currentUser
    private var db = FirebaseFirestore.getInstance()

    private var type: String? = null
    private var tag = "DepositSubmitActivity"
    private var userClass: User? = null
    private var now: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit_submit)

        title = "Central Bank"

        rgCoinType = findViewById(R.id.rgCoinType)
        etAmount = findViewById(R.id.etAmount)
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

        btnSubmitDeposit!!.setOnClickListener {
            getUserData()
            val coinType: String = when{
                rgCoinType!!.checkedRadioButtonId == R.id.rbGold -> "GOLD"
                rgCoinType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                rgCoinType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                rgCoinType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                else  -> "PENY"
            }

            if (etAmount!!.text.toString().toDouble() > 0.0){
                val amount = etAmount!!.text.toString().toDouble()
                if (amount <= userClass!!.balance[coinType]!!){
                    now = Calendar.getInstance()
                    if (type == "time"){
                        if (sdf.format(now!!.time) != btnCalendar!!.text){
                            val profit =  amount*0.05
                            val record = Record(System.currentTimeMillis().toString(),"time", coinType,sdf.format(now!!.time), btnCalendar!!.text as String,
                                    amount, profit)
                            storeDeposit(record, coinType, type!!)
                        } else {
                            tvDepositInf!!.text = "Please choose the expired date"
                        }
                    } else if (type == "demand") {
                        val record = Record(System.currentTimeMillis().toString(), "demand", coinType, sdf.format(now!!.time), "null", amount, isFinish = true)
                        storeDeposit(record, coinType, type!!)
                    }
                } else {
                    tvDepositInf!!.text = "Amount should be smaller than your balance"
                }
            } else {
                tvDepositInf!!.text = "Amount should be larger than 0"
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
        userClass!!.balance[coinType] = userClass!!.balance[coinType]!!-etAmount!!.text.toString().toDouble()
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

        updateUser(userClass!!)
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

    private fun updateUser(userClass: User){
        db.collection("user").document(user!!.uid).update(
                "demandDeposit", userClass.demandDeposit,
                "demandTime", userClass.demandTime
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
