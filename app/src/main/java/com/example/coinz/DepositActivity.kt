package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

// This activity is used to deposit for time and demand mode
class DepositActivity : AppCompatActivity() {

    private var btnTime: Button? = null
    private var btnDemand: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)
        title = "Central Bank"

        btnTime = findViewById(R.id.btnTime)
        btnDemand = findViewById(R.id.btnDemand)

        btnTime!!.setOnClickListener {
            val intent1 = Intent(this@DepositActivity, TimeDepositActivity::class.java)
            intent1.putExtra("type", "time")
            startActivity(intent1)
        }

        btnDemand!!.setOnClickListener {
            val intent2 = Intent(this@DepositActivity, DemandDepositActivity::class.java)
            intent2.putExtra("type", "demand")
            startActivity(intent2)
        }
    }
}
