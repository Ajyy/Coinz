package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BalanceActivity : AppCompatActivity() {

    private var tvGold: TextView? = null
    private var tvShil: TextView? = null
    private var tvDolr: TextView? = null
    private var tvQuid: TextView? = null
    private var tvPeny: TextView? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

    private var tag = "BalanceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        title = "Balance"

        tvGold = findViewById(R.id.tvGold)
        tvShil = findViewById(R.id.tvShil)
        tvDolr = findViewById(R.id.tvDolr)
        tvQuid = findViewById(R.id.tvQuid)
        tvPeny = findViewById(R.id.tvPeny)

        db.collection("users").document(user!!.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Get user inf: Success")

                        val balance = task.result!!.toObject(User::class.java)!!.balance
                        tvGold!!.text = "GOLD: "+balance["GOLD"]
                        tvShil!!.text = "SHIL: "+balance["SHIL"]
                        tvDolr!!.text = "DOLR: "+balance["DOLR"]
                        tvQuid!!.text = "QUID: "+balance["QUID"]
                        tvPeny!!.text = "PENY: "+balance["PENY"]
                    } else {
                        Log.w(tag, "Get user inf: Fail")
                    }
                }
    }
}
