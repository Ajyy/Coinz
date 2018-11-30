package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementActivity : AppCompatActivity() {
    private var rvAchieveList: RecyclerView?  = null
    private var myAdapter: AchievementAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null

    private var achievements = ArrayList<Achievement>()
    private var userClass: User? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance()
    private val tag = "AchievementActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        rvAchieveList = findViewById(R.id.rvAchieveList)
        rvAchieveList!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@AchievementActivity, LinearLayoutManager.VERTICAL, false)
        rvAchieveList!!.layoutManager = layoutManager

        myAdapter = AchievementAdapter(this@AchievementActivity, achievements)
        rvAchieveList!!.adapter = myAdapter

        getUserData()
        getAchievement()
    }

    private fun getUserData(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!. uid!!).get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        Log.d(tag, "get user data: Success")
                        val userData = document.toObject(User::class.java)
                        userClass = userData
                    } else {
                        Toast.makeText(this@AchievementActivity, "Please check your internet", Toast.LENGTH_SHORT)
                        finish()
                        Log.w(tag, "get user data: Fail")
                    }
                }
                .addOnFailureListener{ exception ->
                    Log.d(tag, "get failed with ", exception.cause)
                }
    }

    private fun getAchievement(){
        db.collection("achievements").get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!){
                            val achievement = document.toObject(Achievement::class.java)
                            if (achievement.id in userClass!!.achievements){
                                achievement.isGet = true
                            }

                            achievements.add(achievement)
                        }

                        myAdapter!!.notifyDataSetChanged()
                        Log.d(tag, "get achievements: Success")
                    } else {
                        Log.d(tag, "get achievements: Fail")
                    }
                }
    }
}
