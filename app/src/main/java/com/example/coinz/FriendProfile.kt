package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class FriendProfile : AppCompatActivity() {

    private var ivFriendPicture: ImageView? = null
    private var tvFriendName: TextView? = null
    private var tvFriendEmail: TextView? = null
    private var tvFriendGender: TextView? = null
    private var tvFriendAge: TextView? = null
    private var tvTodayStep: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        val intent = intent
        val friend = intent.getSerializableExtra("friend") as Friend

        ivFriendPicture = findViewById(R.id.ivFriendPicture)
        tvFriendName = findViewById(R.id.tvFriendName)
        tvFriendEmail = findViewById(R.id.tvFriendEmail)
        tvFriendGender = findViewById(R.id.tvFriendGender)
        tvFriendAge = findViewById(R.id.tvFriendAge)
        tvTodayStep = findViewById(R.id.tvTodayStep)

        ivFriendPicture!!.setImageBitmap(friend.picture)
        tvFriendName!!.text = friend.name
        tvFriendEmail!!.text = friend.email
        tvFriendGender!!.text = friend.gender
        tvFriendAge!!.text = friend.age.toString()
        tvTodayStep!!.text = friend.todaySteps.toString()
    }
}
