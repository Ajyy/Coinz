package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DepositActivity : AppCompatActivity() {

    private var user = FirebaseAuth.getInstance().currentUser
    private var db = FirebaseFirestore.getInstance()

    private var userClass: User? = null

    private var tag = "DepositActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)
        title = "Central Bank"

        db.collection("users").document(user!!.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userClass = documentSnapshot.toObject(User::class.java)
                        Log.d(tag, "get user balance: success")
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.d(tag, "get user balance: fail", exception.cause)
                    Toast.makeText(this@DepositActivity, "Please check your internet", Toast.LENGTH_SHORT)
                    finish()
                }
    }
}
