package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TimeDepositActivity : AppCompatActivity() {

    private var records = ArrayList<Record>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecordAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null

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
        return super.onOptionsItemSelected(item)
    }

    private fun getRecords(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid!!).collection("records").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.d(tag, "getRecord: Success")
                        for (document1 in task1.result!!){
                            if (document1!!.exists()){
                                val record = document1.toObject(Record::class.java)
                                if (record.type == "Time"&&!record.isFinish){
                                    records.add(record)
                                }
                                if (myAdapter != null){
                                    myAdapter!!.notifyDataSetChanged()
                                }
                            } else {
                                Log.w(tag, "No Record")
                            }
                        }
                    } else {
                        Log.w(tag, "getRecord: Fail")
                    }
                }
    }
}
