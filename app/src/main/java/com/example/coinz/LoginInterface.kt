package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class LoginInterface : AppCompatActivity(){
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var etUserName: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvInf: TextView? = null
    private val TAG = "LoginInterface"
    private var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_interface)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().getReference("Users")
        etUserName = findViewById<View>(R.id.etUserName) as EditText
        etPassword = findViewById<View>(R.id.etPassword) as EditText
        btnLogin = findViewById<View>(R.id.btnLogin) as Button
        tvInf = findViewById<View>(R.id.tvInf) as TextView

        user = mAuth?.currentUser
        mAuth?.addAuthStateListener { mFirebaseAuth ->
            user = mFirebaseAuth.currentUser
            if (user != null){
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user!!.uid)
//                var emailRef = mDatabase?.child(user!!.uid)
//                emailRef?.addListenerForSingleValueEvent(object: ValueEventListener{
//                    override fun onDataChange(snapshot: DataSnapshot){
//                        if (!snapshot.exists()){
//
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {}
//                })
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            updateUI(user)
        }

        btnLogin?.setOnClickListener {
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
                validate(etUserName!!.text.toString(), etPassword!!.text.toString())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly
        updateUI(user)
        tvInf!!.text = ""
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            finish()
            startActivity(Intent(this@LoginInterface, MainActivity::class.java))
        }
    }

    private fun validate(userName: String, password: String){
        mAuth?.signInWithEmailAndPassword(userName, password)?.addOnCompleteListener {task ->
            if (task.isSuccessful){
                Log.d(TAG, "signInWithEmail: success");
             } else {
                Log.d(TAG, "signInWithEmail: fail")
                signUp(etUserName?.text.toString(), etPassword?.text.toString())
            }
        }
    }

    private fun signUp(userName: String, password: String){
        mAuth?.createUserWithEmailAndPassword(userName, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                mDatabase?.child(user!!.uid)?.setValue(User(email = user!!.email!!))?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Add to database: success")
                        updateUI(user)
                    } else {
                        mAuth?.currentUser!!.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Add to database: fail");
                                tvInf!!.text = "Can not sign up"
                            }
                        }

                        try {
                            throw task.exception!!
                        } catch (e: Exception) {
                            Log.d(TAG, "Add to database: fail: " + e.message);
                        }
                    }
                }
                Log.d(TAG, "createUserWithEmail: success")
            } else {
                try {
                    throw task.exception!!
                } catch (existEmail: FirebaseAuthUserCollisionException){
                    Log.d(TAG, "createUserWithEmail: exist_email");
                    tvInf!!.text = "Wrong password"
                } catch (e: Exception) {
                    Log.d(TAG, "createUserWithEmail: " + e.message);
                }
            }
        }
    }
}
