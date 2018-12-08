package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResetPasswordActivity : AppCompatActivity() {

    private var etReEmail: EditText? = null
    private var btnSendEmail: Button? = null
    private var tvReInf: TextView? = null

    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        title = "Reset Your Password"

        etReEmail = findViewById<View>(R.id.etReEmail) as EditText
        btnSendEmail = findViewById<View>(R.id.btnSendEmail) as Button
        tvReInf = findViewById<View>(R.id.tvReInf) as TextView

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // send email and reset password by email
        btnSendEmail!!.setOnClickListener{
            if (etReEmail!!.text.isEmpty()){
                tvReInf!!.text = "Enter You Email"
            } else {
                db!!.collection("users").whereEqualTo("email", etReEmail!!.text.toString()).get()
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful){
                                if (task.result!!.isEmpty){
                                    tvReInf!!.text = "This email is not registered"
                                } else {
                                    mAuth!!.sendPasswordResetEmail(etReEmail!!.text.toString()).addOnCompleteListener { task ->
                                        if (task.isSuccessful){
                                            tvReInf!!.text = "Reset Email Sent"
                                        } else {
                                            tvReInf!!.text = "Reset Email fail to send"
                                        }
                                    }
                                }
                            }
                        }
            }
        }
    }
}
