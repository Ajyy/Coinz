package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import java.time.temporal.ChronoUnit
import java.util.*

// This activity is used for time deposit
class TimeDepositActivity : AppCompatActivity() {
    private var records = ArrayList<Record>()
    private var updateRecords = ArrayList<Record>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecordAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null
    private var tvTimeInf: TextView? = null

    private var now = Calendar.getInstance()

    private var userData: User? = null
    private val tag = "TimeDepositActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_deposit)
        tvTimeInf = findViewById(R.id.tvTimeInf)

        getUserData()

        // Check whether there exist some time deposits expired and get them
        getRecords()

        title = "Central Bank"

        recyclerView = findViewById(R.id.rvTimeList)
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@TimeDepositActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager

        myAdapter = RecordAdapter(records)

        recyclerView!!.adapter = myAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.deposit_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val intent = Intent(this@TimeDepositActivity, DepositSubmitActivity::class.java)
        intent.putExtra("type", "time")
        startActivity(intent)

        return super.onOptionsItemSelected(item)
    }

    // Receive result from deposit submit activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            Toast.makeText(this@TimeDepositActivity, "Deposit Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        getRecords()
    }

    private fun updateRecordsBalance(){
        for (record in updateRecords){
            // add to the Firebase
            User.userDb.document(User.userAuth!!.uid).collection("records").document(record.id!!)
                    .update("finish", true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            Log.d(tag, "update record: Success")
                        } else {
                            Log.d(tag, "update record: Fail")
                        }
                    }

            // Calculate the interest of demand deposit
            var addValue = 0.0
            if (userData!!.demandTime[record.coinType] != "no"){
                val num = getDifferenceToNow(userData!!.demandTime[record.coinType]!!)
                addValue = userData!!.demandDeposit[record.coinType!!]!!*(0.35/360)*num
                userData!!.demandDeposit[record.coinType!!] = userData!!.demandDeposit[record.coinType!!]!!+addValue
            }

            addValue+=record.interest
            userData!!.demandDeposit[record.coinType!!] = userData!!.demandDeposit[record.coinType!!]!!+record.interest+record.deposit
            userData!!.demandTime[record.coinType!!] = SimpleDateFormat("MM/dd/yyyy").format(now!!.time)
            userData!!.addBalance(addValue, record.coinType!!)
        }

        if (updateRecords.size != 0){
            updateUser()
            updateRecords.clear()
        }
    }

    // Get the records which are expired
    private fun getRecords(){
        User.userDb.document(User.userAuth!!.uid).collection("records").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.d(tag, "getRecord: Success")
                        val records1 = ArrayList<Record>()
                        val records2 = ArrayList<Record>()
                        for (document1 in task1.result!!){
                            if (document1!!.exists()){
                                // Get all the time deposit records
                                val record = document1.toObject(Record::class.java)
                                if (record.type == "time"&&record.finish){
                                    records2.add(record)
                                }

                                if (record.type == "time"&&!record.finish){
                                    if (getDifferenceToNow(record.end!!) >= 0){
                                        record.finish = true
                                        updateRecords.add(record)
                                        records2.add(record)
                                    } else {
                                        records1.add(record)
                                    }
                                }
                            } else {
                                Log.w(tag, "No Record")
                            }
                        }

                        updateRecordsBalance()
                        records.clear()
                        records.addAll(records1)
                        records.addAll(records2)
                        if (myAdapter != null){
                            myAdapter!!.notifyDataSetChanged()
                            tvTimeInf!!.text = "There is/are "+ (records1.size+records2.size)+" pieces of records"
                        }
                    } else {
                        Log.w(tag, "getRecord: Fail")
                    }
                }
    }

    // This method is used to computer the difference between the String time like "12/11/2018" and today
    private fun getDifferenceToNow(lastTime: String): Long{
        val lastCal = Calendar.getInstance()
        lastCal.set(lastTime.substring(6, 10).toInt(), lastTime.substring(0, 2).toInt()-1, lastTime.substring(3, 5).toInt())
        return ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
    }

    private fun getUserData(){
        User.userDb.document(User.userAuth!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "get user data: Success")
                        val userDataR = document.toObject(User::class.java)
                        userData = userDataR
                    } else {
                        Toast.makeText(this@TimeDepositActivity, "Please check your internet", Toast.LENGTH_SHORT)
                        finish()
                        Log.w(tag, "get user data: Fail")
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.d(tag, "get failed with ", exception.cause)
                }
    }

    // Update user data information
    private fun updateUser(){
        User.userDb.document(User.userAuth!!.uid).update(
                "demandDeposit", userData!!.demandDeposit,
                "demandTime", userData!!.demandTime
        ).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Log.d(tag, "Update user inf: Success")
            } else {
                Log.w(tag, "Update user inf: Fail"+task.exception)
                Toast.makeText(this@TimeDepositActivity, "Submit failed, please check your internet", Toast.LENGTH_SHORT)
                finish()
            }
        }
    }
}
