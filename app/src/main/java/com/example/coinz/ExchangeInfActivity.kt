package com.example.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

// This activity is used for exchanging information
class ExchangeInfActivity : AppCompatActivity() {
    private var btnExchange: Button? = null
    private var btnRate: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_inf)

        btnExchange = findViewById(R.id.btnExchange)
        btnRate = findViewById(R.id.btnRate)

        btnExchange!!.setOnClickListener {
            startActivity(Intent(this@ExchangeInfActivity, ExchangeActivity::class.java))
        }

        btnRate!!.setOnClickListener {view ->
            onButtonShowPopupWindowClick(view)
        }
    }

    // Set the popup window
    @SuppressLint("InflateParams")
    fun onButtonShowPopupWindowClick(view: View){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_rates, null)
        val tvShilRate = popupView.findViewById<View>(R.id.tvShilRate) as TextView
        val tvDolrRate = popupView.findViewById<View>(R.id.tvDolrRate) as TextView
        val tvQuidRate = popupView.findViewById<View>(R.id.tvQuidRate) as TextView
        val tvPenyRate = popupView.findViewById<View>(R.id.tvPenyRate) as TextView

        tvShilRate.text = String.format("SHIL:  %.10f", Coin.ratesArr["SHIL"])
        tvDolrRate.text = String.format("DOLR:  %.10f", Coin.ratesArr["DOLR"])
        tvQuidRate.text = String.format("QUID:  %.10f", Coin.ratesArr["QUID"])
        tvPenyRate.text = String.format("PENY:  %.10f", Coin.ratesArr["PENY"])

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
