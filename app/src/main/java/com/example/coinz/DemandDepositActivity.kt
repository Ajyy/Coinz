package com.example.coinz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
    private var user = FirebaseAuth.getInstance().currentUser
    private val tag = "DemandDepositActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demand_deposit)

        title = "Central Bank"

        btnDemandDeposit = findViewById(R.id.btnDemandDeposit)
        btnDemandBalance = findViewById(R.id.btnDemandBalance)
        btnDemandDeposit!!.setOnClickListener {
            val intent1 = Intent(this@DemandDepositActivity, DepositSubmitActivity::class.java)
            intent1.putExtra("type", "demand")
            startActivity(intent1)
        }

        btnDemandBalance!!.setOnClickListener {v ->
            getBalance(v)
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

        tvGoldBal.text = "GOLD: "+balance!!["GOLD"].toString()
        tvShilBal.text = "SHIL: "+balance!!["SHIL"].toString()
        tvDolrBal.text = "DOLR: "+balance!!["DOLR"].toString()
        tvQuidBal.text = "QUID: "+balance!!["QUID"].toString()
        tvPenyBal.text = "PENY: "+balance!!["PENY"].toString()

        val width = 1000
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

    private fun getBalance(v: View){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "getBalance: Success")
                        val userData = document.toObject(User::class.java)
                        balance = userData!!.balance
                        onButtonShowPopupWindowClick(v)
                    } else {
                        Log.w(tag, "getBalance: Fail")
                    }
                }
    }
}
