package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser



class LoginInterface : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var etUserName: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvInf: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_interface)
        mAuth = FirebaseAuth.getInstance()
        etUserName = findViewById<View>(R.id.etUserName) as EditText
        etPassword = findViewById<View>(R.id.etPassword) as EditText
        btnLogin = findViewById<View>(R.id.btnLogin) as Button
        tvInf = findViewById<View>(R.id.tvInf) as TextView

        var user: FirebaseUser? = mAuth?.currentUser
        if (user == null){
            finish()
            startActivity(Intent(this@LoginInterface, MainActivity::class.java))
        }

        btnLogin?.setOnClickListener(View.OnClickListener {  })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly
        var currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            tvInf!!.text = "Error: can not login!"
        }
    }

    private fun validate(userName: String, password: String){
        mAuth?.signInWithEmailAndPassword(userName, password)?.addOnCompleteListener {task ->
            if (task.isSuccessful){
                chec
            }
        }
    }
}
