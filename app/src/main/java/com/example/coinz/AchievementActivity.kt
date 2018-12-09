package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementActivity : AppCompatActivity() {
    private var rvAchieveList: RecyclerView?  = null
    private var myAdapter: AchievementAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null
    private var tvAchieveInf: TextView? = null

    private var achievements = ArrayList<Achievement>()
    private var userData: User? = null

    private val tag = "AchievementActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        title = "Achievement"

        tvAchieveInf = findViewById(R.id.tvAchieveInf)

        rvAchieveList = findViewById(R.id.rvAchieveList)
        rvAchieveList!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@AchievementActivity, LinearLayoutManager.VERTICAL, false)
        rvAchieveList!!.layoutManager = layoutManager

        myAdapter = AchievementAdapter(this@AchievementActivity, achievements)
        rvAchieveList!!.adapter = myAdapter

        userData!!.getData()
        getAchievement()
    }

    private fun getAchievement(){
        Achievement.achieveDb.get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!){
                            val achievement = document.toObject(Achievement::class.java)
                            if (achievement.id in userData!!.achievements){
                                achievement.isGet = true
                            }

                            achievements.add(achievement)
                        }

                        tvAchieveInf!!.text = "Find ${task.result!!.size()} achievements"
                        myAdapter!!.notifyDataSetChanged()
                        Log.d(tag, "get achievements: Success")
                    } else {
                        Log.d(tag, "get achievements: Fail")
                    }
                }
    }
}
