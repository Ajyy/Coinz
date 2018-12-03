package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BalanceActivity : AppCompatActivity() {

    private var tvGold: TextView? = null
    private var btnShil: Button? = null
    private var btnDolr: Button? = null
    private var btnQuid: Button? = null
    private var btnPeny: Button? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

    private var userData: User? = null
    private var tag = "BalanceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        title = "Balance"

        tvGold = findViewById(R.id.tvGold)
        btnShil = findViewById(R.id.btnShil)
        btnDolr = findViewById(R.id.btnDolr)
        btnQuid = findViewById(R.id.btnQuid)
        btnPeny = findViewById(R.id.btnPeny)

        db.collection("users").document(user!!.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Get user inf: Success")

                        userData = task.result!!.toObject(User::class.java)
                        tvGold!!.text = "GOLD: "+userData!!.gold
                    } else {
                        Log.w(tag, "Get user inf: Fail")
                    }
                }

        btnPeny!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayListOf("PENY", "balance"))
            startActivity(intent)
        }

        btnQuid!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayListOf("QUID", "balance"))
            startActivity(intent)
        }

        btnShil!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayListOf("SHIL", "balance"))
            startActivity(intent)
        }

        btnDolr!!.setOnClickListener {
            val intent = Intent(this@BalanceActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayListOf("DOLR", "balance"))
            startActivity(intent)
        }
    }
}
