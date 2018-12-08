package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore


class Profile : AppCompatActivity() {

    private var etName: EditText? = null
    private var etAge: EditText? = null
    private var rgGender: RadioGroup? = null
    private var tvNotify: TextView? = null
    private var tvEmail: TextView? = null
    private var btnSubmit: Button? = null
    private var btnConfirm: Button? = null


    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private val tag = "Profile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        title = "Profile"

        etName = findViewById(R.id.etName)
        etAge = findViewById(R.id.etAge)
        rgGender = findViewById(R.id.rgGender)
        tvNotify = findViewById(R.id.tvNotify)
        tvEmail = findViewById(R.id.tvEmail)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnSubmit = findViewById(R.id.btnSubmit)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = mAuth?.currentUser

        // initialize user information
        initializeInf(user!!)

        // submit profile information
        btnSubmit!!.setOnClickListener {
            when {
                etName!!.text.isEmpty() -> tvNotify!!.text = "Please enter the name"
                etAge!!.text.isEmpty() -> tvNotify!!.text = "Please enter age"
                else -> {
                    updateProfile(user)
                }
            }
        }
    }

    private fun updateProfile(user: FirebaseUser){
        val gender = when {
            rgGender!!.checkedRadioButtonId == R.id.rbtnMale -> "Male"
            rgGender!!.checkedRadioButtonId == R.id.rbtnFemale -> "Female"
            else -> "Unknown"
        }
        db!!.collection("users").document(user.uid).update(
                "age", etAge!!.text.toString().toInt(),
                "name", etName!!.text.toString(),
                "gender", gender
        )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "update successful")
                        user.updateProfile(UserProfileChangeRequest.Builder()
                                .setDisplayName(etName!!.text.toString())
                                .build())
                                .addOnCompleteListener { task2 ->
                                    if (task2.isSuccessful){
                                        Log.d(tag, "UserProfileUpdated: success")
                                    } else {
                                        Log.w(tag, "UserProfileUpdated: fail")
                                    }
                                }

                        val intent = Intent()
                        intent.putExtra("name", etName!!.text.toString())
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        Log.w(tag, "fail to update")
                        tvNotify!!.text = "Can not update profile"
                    }
                }
    }

    private fun initializeInf(user: FirebaseUser){
        // initialize information

        // set email
        tvEmail!!.text = user.email

        // get data and set gender and name
        db!!.collection("users").document(user.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userClass = documentSnapshot.toObject(User::class.java)
                        etName!!.setText(userClass?.name)
                        etAge!!.setText(userClass?.age!!.toString())
                        when (userClass.gender) {
                            "Male" -> rgGender!!.check(R.id.rbtnMale)
                            "Female" -> rgGender!!.check(R.id.rbtnFemale)
                            "Unknown" -> rgGender!!.check(R.id.rbtnUnknown)
                        }

                        Log.d(tag, "initialize profile: success")
                    } else {
                        Log.d(tag, "User information does not exist")
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.d(tag, "get failed with ", exception.cause)
                }

        // set the verification button
        if (user.isEmailVerified){
            btnConfirm!!.text = "Verified"
            btnConfirm!!.isEnabled = false
            btnConfirm!!.elevation = 0.0F
        } else {
            btnConfirm!!.setOnClickListener{
                user.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Toast.makeText(this@Profile, "Verification Email Sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Profile, "Verification Email fail to send", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
