package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileActivity : AppCompatActivity() {
    private var etName: EditText? = null
    private var etAge: EditText? = null
    private var rgGender: RadioGroup? = null
    private var tvNotify: TextView? = null
    private var tvEmail: TextView? = null
    private var btnSubmit: Button? = null
    private var btnConfirm: Button? = null

    private val tag = "ProfileActivity"

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

        val user = User.userAuth

        // initialize user information
        initializeInf(user!!)

        // submit profile information
        btnSubmit!!.setOnClickListener {
            when {
                etName!!.text.isEmpty() -> tvNotify!!.text = getString(R.string.user_profile_hint1)
                etAge!!.text.isEmpty() -> tvNotify!!.text = getString(R.string.user_profile_hint2)
                etAge!!.text.toString().toInt() <= 0 -> tvNotify!!.text = getString(R.string.user_profile_hint3)
                else -> {
                    updateProfile(user)
                }
            }
        }
    }

    private fun updateProfile(user: FirebaseUser){
        // Get the gender
        val gender = when {
            rgGender!!.checkedRadioButtonId == R.id.rbtnMale -> "Male"
            rgGender!!.checkedRadioButtonId == R.id.rbtnFemale -> "Female"
            else -> "Unknown"
        }

        // Update the user data in Firebase
        User.userDb.document(user.uid).update(
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
        User.userDb.document(user.uid).get()
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
                        Toast.makeText(this@ProfileActivity, "Verification Email Sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Verification Email fail to send", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
