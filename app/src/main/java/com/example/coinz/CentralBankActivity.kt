package com.example.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

class CentralBankActivity : AppCompatActivity() {

    private var btnDeposit: Button? = null
    private var btnRate: Button? = null
    private var btnHistory: Button? = null
    private var btnDraw: Button? = null
    private var ratesArray: DoubleArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_central_bank)
        title = "Central Bank"

        val intent1 = intent
        ratesArray = intent1.getDoubleArrayExtra("rates")

        btnDeposit = findViewById(R.id.btnDeposit)
        btnRate = findViewById(R.id.btnRate)
        btnHistory = findViewById(R.id.btnHistory)
        btnDraw = findViewById(R.id.btnDraw)

        btnHistory!!.setOnClickListener {
            val intent2 = Intent(this@CentralBankActivity, HistoryActivity::class.java)
            startActivity(intent2)
        }

        btnDeposit!!.setOnClickListener {
            val intent3 = Intent(this@CentralBankActivity, DepositActivity::class.java)
            startActivity(intent3)
        }

        btnDraw!!.setOnClickListener {
            val intent4 = Intent(this@CentralBankActivity, DrawMoneyActivity::class.java)
            startActivity(intent4)
        }

        btnRate!!.setOnClickListener {view ->
            onButtonShowPopupWindowClick(view)
        }
    }

    @SuppressLint("InflateParams")
    fun onButtonShowPopupWindowClick(view: View){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_rates, null)
        val tvShilRate = popupView.findViewById<View>(R.id.tvShilRate) as TextView
        val tvDolrRate = popupView.findViewById<View>(R.id.tvDolrRate) as TextView
        val tvQuidRate = popupView.findViewById<View>(R.id.tvQuidRate) as TextView
        val tvPenyRate = popupView.findViewById<View>(R.id.tvPenyRate) as TextView

        tvShilRate.text = String.format("SHIL:  %.10f", ratesArray!![0])
        tvDolrRate.text = String.format("DOLR:  %.10f", ratesArray!![1])
        tvQuidRate.text = String.format("QUID:  %.10f", ratesArray!![2])
        tvPenyRate.text = String.format("PENY:  %.10f", ratesArray!![3])

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        val lp = window.attributes
        lp.alpha = 0.4f
        window.attributes = lp

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.isOutsideTouchable = false
        popupWindow.isFocusable = false
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        popupView.setOnTouchListener { _, _ ->
            lp.alpha = 1f
            window.attributes = lp
            popupWindow.dismiss()
            return@setOnTouchListener true
        }
    }
}
