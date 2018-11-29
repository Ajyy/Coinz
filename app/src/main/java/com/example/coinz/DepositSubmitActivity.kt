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
            btnCalendar!!.text = "$monthofYear/$dayofMonth/$year"
            datePicker.minDate = System.currentTimeMillis()-1000
        }

        type = intent.getStringExtra("type")
        if (type == "demand"){
            btnCalendar!!.isEnabled = false
        } else {
            btnCalendar!!.setOnClickListener {v ->
                DatePickerDialog(this@DepositSubmitActivity, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
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

            if (etAmount!!.toString().toDouble() <= 0.0){
                if (etAmount!!.toString().toDouble() <= userClass!!.balance[coinType]!!){
                    now = Calendar.getInstance()
                    if (type == "time"){
                        if (sdf.format(now!!.time) != btnCalendar!!.text){
                            val profit =  etAmount.toString().toDouble()*0.05
                            val record = Record("time", sdf.format(now!!.time), btnCalendar!!.text as String,
                                    etAmount.toString().toDouble(), profit)
                            storeDeposit(record, coinType, type!!)
                        } else {
                            tvDepositInf!!.text = "Please choose the expired date"
                        }
                    } else if (type == "demand") {
                        val record = Record("demand", sdf.format(now!!.time), "null", 0.0, isFinish = true)
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
                        Log.d(tag, "getBalance: Success")
                        val userData = document.toObject(User::class.java)
                        userClass = userData
                    } else {
                        Toast.makeText(this@DepositSubmitActivity, "Please check your internet", Toast.LENGTH_SHORT)
                        finish()
                        Log.w(tag, "getBalance: Fail")
                    }
                }
    }

    private fun storeDeposit(record: Record, coinType: String, type: String){
        userClass!!.balance[coinType] = userClass!!.balance[coinType]!!-etAmount.toString().toDouble()
        if (type == "demand"){
            if (userClass!!.demandTime != "no"){
                val lastCal = Calendar.getInstance()
                lastCal.set(userClass!!.demandTime.substring(6, 10).toInt(), userClass!!.demandTime.substring(0, 2).toInt(), userClass!!.demandTime.substring(3, 5).toInt())
                val num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
                userClass!!.demandDeposit+=userClass!!.demandDeposit*(0.35/360)*num
            }

            userClass!!.demandDeposit+=record.deposit
            userClass!!.demandTime = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
        }

        updateUser(userClass!!)
        db.collection("users").document(user!!.uid).collection("records").document(System.currentTimeMillis() as String)
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
