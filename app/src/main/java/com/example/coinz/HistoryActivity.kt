package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView

// History activity to check the record
class HistoryActivity : AppCompatActivity() {
    private var records = ArrayList<Record>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecordAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null
    private var tvHistoryInf: TextView? = null

    private val tag = "HistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        title = "Central Bank"
        tvHistoryInf = findViewById(R.id.tvHistoryInf)

        recyclerView = findViewById(R.id.rvHistoryList)
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@HistoryActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager
        getRecords()

        myAdapter = RecordAdapter(records)

        recyclerView!!.adapter = myAdapter
    }

    // Get all the records including time and demand deposit
    private fun getRecords(){
        User.userDb.document(User.userAuth!!.uid).collection("records").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.d(tag, "getRecord: Success")
                        for (document1 in task1.result!!){
                            if (document1!!.exists()){
                                val record = document1.toObject(Record::class.java)
                                records.add(record)
                            } else {
                                Log.w(tag, "No Record")
                            }
                        }

                        if (myAdapter != null){
                            myAdapter!!.notifyDataSetChanged()
                            tvHistoryInf!!.text = "There is/are "+ task1.result!!.size()+" pieces of history"
                        }
                    } else {
                        Log.w(tag, "getRecord: Fail")
                    }
                }
    }
}
