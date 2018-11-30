package com.example.coinz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class AchievementAdapter(private val context: Context, private val achievements: ArrayList<Achievement>): RecyclerView.Adapter<AchievementAdapter.ViewHolder>(){
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AchievementAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.achievement_list, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(viewHolder: AchievementAdapter.ViewHolder, i: Int) {
        viewHolder.tvAchieveTitle.text = achievements[i].title
        viewHolder.tvAchieveIntro.text = achievements[i].intro
        if (achievements[i].isGet){
            viewHolder.tvIsGet.text = "Finish"
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvAchieveTitle: TextView = itemView.findViewById(R.id.tvAchieveTitle)
        var tvAchieveIntro: TextView = itemView.findViewById(R.id.tvAchieveIntro)
        var tvIsGet: TextView = itemView.findViewById(R.id.tvIsGet)
    }
}