package com.example.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

// This activity is used for central bank
class CentralBankActivity : AppCompatActivity() {

    private var btnDeposit: Button? = null
    private var btnRate: Button? = null
    private var btnSpareChange: Button? = null
    private var btnHistory: Button? = null
    private var btnExchange: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_central_bank)
        title = "Central Bank"

        btnDeposit = findViewById(R.id.btnDeposit)
        btnRate = findViewById(R.id.btnRate)
        btnSpareChange = findViewById(R.id.btnSpareChange)
        btnHistory = findViewById(R.id.btnHistory)
        btnExchange = findViewById(R.id.btnExchange)

        // Start activity
        btnHistory!!.setOnClickListener {
            val intent2 = Intent(this@CentralBankActivity, HistoryActivity::class.java)
            startActivity(intent2)
        }

        btnDeposit!!.setOnClickListener {
            val intent3 = Intent(this@CentralBankActivity, DepositActivity::class.java)
            startActivity(intent3)
        }

        btnSpareChange!!.setOnClickListener {
            startActivity(Intent(this@CentralBankActivity, SpareChangeActivity::class.java))
        }

        btnExchange!!.setOnClickListener {
            startActivity(Intent(this@CentralBankActivity, ExchangeInfActivity::class.java))
        }
    }
}
