package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity(){
    private var progressBar: ProgressBar? = null
    private var etUserName: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvInf: TextView? = null
    private var tvFPass: TextView? = null

    private val tag = "LoginActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progressBar)
        etUserName = findViewById(R.id.etUserName)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvFPass = findViewById(R.id.tvFPass)
        tvInf = findViewById(R.id.tvInf)

        // Listener to update UI
        User.mAuth.addAuthStateListener {
            if (User.mAuth.currentUser != null){
                User.userAuth = User.mAuth.currentUser
                Log.d(tag, "onAuthStateChanged:signed_in:" + User.userAuth!!.uid)
                updateUI(User.userAuth)
            } else {
                Log.d(tag, "onAuthStateChanged:signed_out")
            }
        }

        // Login or Sign up
        btnLogin!!.setOnClickListener {
            if (etUserName!!.text.isEmpty()&&etPassword!!.text.isEmpty()){
                tvInf!!.text = "Please enter your email and password"
            } else if (etUserName!!.text.isEmpty()) {
                tvInf!!.text = "Please enter your email"
            } else if (etPassword!!.text.isEmpty()) {
                tvInf!!.text = "Please enter your password"
            } else if (!etUserName!!.text.contains("@")) {
                tvInf!!.text = "Wrong email format"
            } else if (etPassword!!.text.toString().length < 6){
                tvInf!!.text = "The length of password should be larger than six"
            } else {
                progressBar!!.visibility = View.VISIBLE
                validate(etUserName!!.text.toString(), etPassword!!.text.toString())
            }
        }

        tvFPass!!.setOnClickListener{
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        tvInf!!.text = ""
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun validate(userName: String, password: String){
        User.mAuth.signInWithEmailAndPassword(userName, password).addOnCompleteListener {task ->
            if (task.isSuccessful){
                Log.d(tag, "signInWithEmail: success")
             } else {
                Log.d(tag, "signInWithEmail: fail")
                signUp(etUserName?.text.toString(), etPassword?.text.toString())
            }
        }
    }

    private fun signUp(userName: String, password: String){
        User.mAuth.createUserWithEmailAndPassword(userName, password).addOnCompleteListener { task1 ->
            if (task1.isSuccessful){
                val addUser = User()
                addUser.email = User.userAuth!!.email
                User.userDb.document(User.userAuth!!.uid).
                        set(addUser, SetOptions.merge()).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                Log.d(tag, "Add to database: success")
                                // updateUI(user)
                            } else {
                                User.userAuth!!.delete().addOnCompleteListener { task3 ->
                                    if (task3.isSuccessful) {
                                        Log.d(tag, "Add to database: fail")
                                        tvInf!!.text = "Can not sign up"
                                    }
                                }

                                Log.d(tag, "Add to database: Fail")
                                progressBar!!.visibility = View.GONE
                            }
                }

                Log.d(tag, "createUserWithEmail: success")
            } else {
                progressBar!!.visibility = View.GONE
                try {
                    throw task1.exception!!
                } catch (existEmail: FirebaseAuthUserCollisionException){
                    Log.d(tag, "createUserWithEmail: exist_email")
                    tvInf!!.text = "Wrong password"
                } catch (e: Exception) {
                    Log.d(tag, "createUserWithEmail: " + e.message)
                }
            }
        }
    }
}
