package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

// This activity is used to see the coins' balance
class BalanceActivity : AppCompatActivity() {

    private var btnShil: Button? = null
    private var btnDolr: Button? = null
    private var btnQuid: Button? = null
    private var btnPeny: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        title = "Balance"

        btnShil = findViewById(R.id.btnShil)
        btnDolr = findViewById(R.id.btnDolr)
        btnQuid = findViewById(R.id.btnQuid)
        btnPeny = findViewById(R.id.btnPeny)

        // Start the activity with the corresponding coin's type
        btnPeny!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayOf("PENY", "balance"))
            startActivity(intent)
        }

        btnQuid!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayOf("QUID", "balance"))
            startActivity(intent)
        }

        btnShil!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayOf("SHIL", "balance"))
            startActivity(intent)
        }

        btnDolr!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayOf("DOLR", "balance"))
            startActivity(intent)
        }
    }
}
