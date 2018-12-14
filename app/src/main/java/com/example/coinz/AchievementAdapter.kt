package com.example.coinz

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

// Achievement adapter class
class AchievementAdapter(private val achievements: ArrayList<Achievement>): RecyclerView.Adapter<AchievementAdapter.ViewHolder>(){
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AchievementAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.achievement_list, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(viewHolder: AchievementAdapter.ViewHolder, i: Int) {
        // Set achievement information
        viewHolder.tvAchieveTitle.text = achievements[i].title
        viewHolder.tvAchieveIntro.text = achievements[i].intro
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvAchieveTitle: TextView = itemView.findViewById(R.id.tvAchieveTitle)
        var tvAchieveIntro: TextView = itemView.findViewById(R.id.tvAchieveIntro)
    }
}