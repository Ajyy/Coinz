package com.example.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DemandDepositActivity : AppCompatActivity() {

    private var btnDemandDeposit: Button? = null
    private var btnDemandBalance: Button? = null

    private var balance: MutableMap<String, Double>? = null
    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance()
    private val tag = "DemandDepositActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demand_deposit)

        title = "Central Bank"

        getBalance()
        btnDemandDeposit = findViewById(R.id.btnDemandDeposit)
        btnDemandBalance = findViewById(R.id.btnDemandBalance)

        btnDemandDeposit!!.setOnClickListener {
            
        }

        btnDemandBalance!!.setOnClickListener {v ->
            onButtonShowPopupWindowClick(v)
        }
    }

    @SuppressLint("InflateParams")
    fun onButtonShowPopupWindowClick(view: View){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_balance, null)
        val tvGoldBal = popupView.findViewById<View>(R.id.tvGoldBal) as TextView
        val tvShilBal = popupView.findViewById<View>(R.id.tvShilBal) as TextView
        val tvDolrBal = popupView.findViewById<View>(R.id.tvDolrBal) as TextView
        val tvQuidBal = popupView.findViewById<View>(R.id.tvQuidBal) as TextView
        val tvPenyBal = popupView.findViewById<View>(R.id.tvPenyBal) as TextView

        tvGoldBal.text = balance!!["GOLD"] as String
        tvShilBal.text = balance!!["SHIL"] as String
        tvDolrBal.text = balance!!["DOLR"] as String
        tvQuidBal.text = balance!!["QUID"] as String
        tvPenyBal.text = balance!!["PENY"] as String

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

        popupView.setOnTouchListener { v, _ ->
            lp.alpha = 1f
            window.attributes = lp
            popupWindow.dismiss()
            return@setOnTouchListener true
        }
    }

    private fun getBalance(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid!!).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "getBalance: Success")
                        val userData = document.toObject(User::class.java)
                        balance = userData!!.balance
                    } else {
                        Log.w(tag, "getBalance: Fail")
                    }
                }
    }
}
