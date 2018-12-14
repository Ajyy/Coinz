package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import android.widget.Toast

// This class is used to show the achievement
class AchievementActivity : AppCompatActivity() {
    private var rvAchieveList: RecyclerView?  = null
    private var myAdapter: AchievementAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null
    private var tvAchieveInf: TextView? = null

    private var achievements = ArrayList<Achievement>()
    private var userData = User()

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

        myAdapter = AchievementAdapter(achievements)
        rvAchieveList!!.adapter = myAdapter

        userData.getData()
        getAchievement()
    }

    // This method is used to get the achievement of user and check whether new achievements are gotten
    private fun getAchievement(){
        Achievement.achieveDb.get()
                .addOnCompleteListener {task1 ->
                    if (task1.isSuccessful) {
                        for (document in task1.result!!){
                            val achievement = document.toObject(Achievement::class.java)
                            if (achievement.id in userData.achievements){
                                // Add the achievement which has already gotten
                                achievement.get = true
                                achievements.add(achievement)
                            } else {
                                // Check new achievement
                                if (achievement.title == "Social Talent: Primary"){
                                    User.userDb.document(User.userAuth!!.uid).collection("friends").get()
                                            .addOnCompleteListener {task2 ->
                                                if (task2.isSuccessful){
                                                    // Check whether the conditions are satisified
                                                    if (task2.result!!.size() >= 1){
                                                        achievement.get = true
                                                        achievements.add(achievement)
                                                        userData.addAchievement(achievement.id)
                                                        Toast.makeText(this@AchievementActivity, "Congratulations!! Get achievement: " +
                                                                achievement.title, Toast.LENGTH_SHORT).show()
                                                    }

                                                    myAdapter!!.notifyDataSetChanged()

                                                    Log.d(tag, "get number of friends: Success")
                                                } else {
                                                    Log.d(tag, "get number of friends: Fail")
                                                }
                                            }
                                }
                            }
                        }

                        tvAchieveInf!!.text = "Find ${task1.result!!.size()} achievements"
                        myAdapter!!.notifyDataSetChanged()
                        Log.d(tag, "get achievements: Success")
                    } else {
                        Log.d(tag, "get achievements: Fail")
                    }
                }
    }
}
