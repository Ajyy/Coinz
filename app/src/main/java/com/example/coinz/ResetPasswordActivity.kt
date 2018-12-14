package com.example.coinz

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

// This Activity is used to reset password
class ResetPasswordActivity : AppCompatActivity() {

    private var etReEmail: EditText? = null
    private var btnSendEmail: Button? = null
    private var tvReInf: TextView? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        title = "Reset Your Password"

        etReEmail = findViewById<View>(R.id.etReEmail) as EditText
        btnSendEmail = findViewById<View>(R.id.btnSendEmail) as Button
        tvReInf = findViewById<View>(R.id.tvReInf) as TextView

        mAuth = FirebaseAuth.getInstance()

        // send email and reset password by email
        btnSendEmail!!.setOnClickListener{
            if (etReEmail!!.text.isEmpty()){
                tvReInf!!.text = getString(R.string.reset_password_hint1)
            } else {
                User.userDb.whereEqualTo("email", etReEmail!!.text.toString()).get()
                        .addOnCompleteListener {task1 ->
                            if (task1.isSuccessful){
                                if (task1.result!!.isEmpty){
                                    tvReInf!!.text = getString(R.string.reset_password_hint2)
                                } else {
                                    mAuth!!.sendPasswordResetEmail(etReEmail!!.text.toString()).addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful){
                                            tvReInf!!.text = getString(R.string.reset_password_hint3)
                                        } else {
                                            tvReInf!!.text = getString(R.string.reset_password_hint4)
                                        }
                                    }
                                }
                            }
                        }
            }
        }
    }
}
