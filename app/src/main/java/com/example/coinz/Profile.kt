package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*


class Profile : AppCompatActivity() {

    private var etName: EditText? = null
    private var etAge: EditText? = null
    private var rgGender: RadioGroup? = null
    private var btnSubmit: Button? = null
    private var mDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var tvNavUserName: TextView? = null
    private var tvNotify: TextView? = null
    private val TAG = "Profile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        etName = findViewById<View>(R.id.etName) as EditText
        etAge = findViewById<View>(R.id.etAge) as EditText
        rgGender = findViewById<View>(R.id.rgGender) as RadioGroup
        btnSubmit = findViewById<View>(R.id.btnSubmit) as Button
        tvNotify = findViewById<View>(R.id.tvNotify) as TextView
        mDatabase = FirebaseDatabase.getInstance().getReference("Users")
        mAuth = FirebaseAuth.getInstance()
        var userReal = mAuth?.currentUser

        // initialize information
        var userRef = mDatabase!!.child(userReal!!.uid)
        userRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                var userClass = snapshot.getValue(User::class.java)
                etName!!.setText(userClass?.name)
                etAge!!.setText(userClass?.age!!.toString())
                when (userClass.gender) {
                    "Male" -> rgGender!!.check(R.id.rbtnMale)
                    "Female" -> rgGender!!.check(R.id.rbtnFemale)
                    "Unknown" -> rgGender!!.check(R.id.rbtnUnknown)
                }
            }

        })


        btnSubmit?.setOnClickListener {
            if (etName!!.text.isEmpty()){
                tvNotify!!.text = "Please enter the name"
            } else if (etAge!!.text.isEmpty()) {
                tvNotify!!.text = "Please enter age"
            } else {
                var gender = if (rgGender!!.checkedRadioButtonId == R.id.rbtnMale) "Male" else
                    if (rgGender!!.checkedRadioButtonId == R.id.rbtnFemale) "Female" else "Unknown"
                var user = User(etName!!.text.toString(), userReal.email!!, etAge!!.text.toString().toInt(), gender)
                var userUpdate = mapOf<String, User>(Pair(userReal.uid, user))
                mDatabase!!.updateChildren(userUpdate).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "update successful")
                        userReal.updateProfile(UserProfileChangeRequest.Builder()
                                .setDisplayName(etName!!.text.toString())
                                .build())
                                .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful){
                                Log.d(TAG, "UserProfileUpdated: success")
                            } else {
                                Log.w(TAG, "UserProfileUpdated: fail")
                            }
                        }

                        var intent = Intent()
                        intent.putExtra("name", etName!!.text.toString())
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        Log.w(TAG, "fail to update")
                        tvNotify!!.text = "Can not update profile"
                    }
                }
            }
        }
    }
}
