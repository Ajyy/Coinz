package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

// This activity is used for friend profile
class FriendProfile : AppCompatActivity() {

    private var ivFriendPicture: ImageView? = null
    private var tvFriendName: TextView? = null
    private var tvFriendEmail: TextView? = null
    private var tvFriendGender: TextView? = null
    private var tvFriendAge: TextView? = null

    private var mStorageReference = FirebaseStorage.getInstance().reference

    private val tag = "FriendProfile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        title = "Friend Profile"

        val intent = intent
        val friend = intent.getSerializableExtra("friend") as Friend

        ivFriendPicture = findViewById(R.id.ivFriendPicture)
        tvFriendName = findViewById(R.id.tvFriendName)
        tvFriendEmail = findViewById(R.id.tvFriendEmail)
        tvFriendGender = findViewById(R.id.tvFriendGender)
        tvFriendAge = findViewById(R.id.tvFriendAge)

        // Get friends' avatar
        val pathReference = mStorageReference.child("images/"+friend.email+".jpg")
        pathReference.downloadUrl
                .addOnSuccessListener { filePath ->
                    Picasso.get().load(filePath).resize(200, 200).into(ivFriendPicture)
                    Log.d(tag, "down avatar: success")
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "down avatar: failure\n"+exception.message)
                }

        tvFriendName!!.text = friend.name
        tvFriendEmail!!.text = friend.email
        tvFriendGender!!.text = friend.gender
        tvFriendAge!!.text = friend.age.toString()
    }
}
