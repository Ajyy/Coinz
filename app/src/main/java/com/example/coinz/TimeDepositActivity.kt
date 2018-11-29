package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.temporal.ChronoUnit
import java.util.*

class TimeDepositActivity : AppCompatActivity() {

    private var records = ArrayList<Record>()
    private var updateRecords = ArrayList<Record>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecordAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null

    private var now = Calendar.getInstance()
    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance()
    private val tag = "TimeDepositActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_deposit)
        getRecords()

        title = "Central Bank"

        recyclerView = findViewById(R.id.rvTimeList)
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@TimeDepositActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager

        myAdapter = RecordAdapter(this@TimeDepositActivity, records)

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

    override fun onResume() {
        super.onResume()
        getRecords()
    }

    private fun getRecords(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid!!).collection("records").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.d(tag, "getRecord: Success")
                        val records1 = ArrayList<Record>()
                        val records2 = ArrayList<Record>()
                        for (document1 in task1.result!!){
                            if (document1!!.exists()){
                                val record = document1.toObject(Record::class.java)
                                if (record.type == "time"&&record.isFinish){
                                    records2.add(record)
                                }

                                if (record.type == "time"&&!record.isFinish){
                                    if (differenceDay(record.end) >= 0){
                                        record.isFinish = true
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

                        records.clear()
                        records.addAll(records1)
                        records.addAll(records2)
                        if (myAdapter != null){
                            myAdapter!!.notifyDataSetChanged()
                        }
                    } else {
                        Log.w(tag, "getRecord: Fail")
                    }
                }
    }

    private fun differenceDay(lastTime: String): Long{
        val lastCal = Calendar.getInstance()
        lastCal.set(lastTime.substring(6, 10).toInt(), lastTime.substring(0, 2).toInt(), lastTime.substring(3, 5).toInt())
        return ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
    }
}
