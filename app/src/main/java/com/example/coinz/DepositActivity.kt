package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DepositActivity : AppCompatActivity() {

    private var records: ArrayList<Record>? = null
    private var btnTime: Button? = null
    private var btnDemand: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)
        title = "Central Bank"

        records = intent.getSerializableExtra("records") as ArrayList<Record>

        btnTime!!.setOnClickListener {
            val intent1 = Intent(this@DepositActivity, TimeDepositActivity::class.java)
            startActivity(intent1)
        }

        btnDemand!!.setOnClickListener {
            val intent2 = Intent(this@DepositActivity, DemandDepositActivity::class.java)
            startActivity(intent2)
        }

    }
}
