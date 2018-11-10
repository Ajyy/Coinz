package com.example.coinz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class FriendAdapter(private val context: Context, private val friends: ArrayList<Friend>): RecyclerView.Adapter<FriendAdapter.ViewHolder>(){

    private var mStorageReference = FirebaseStorage.getInstance().reference
    private var db = FirebaseFirestore.getInstance()
    private var mAuth = FirebaseAuth.getInstance()
    private var user = mAuth.currentUser

    private val tag = "FriendActivity"

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): FriendAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.friend_list, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(viewHolder: FriendAdapter.ViewHolder, i: Int) {
        viewHolder.itemView.tag = friends[i]

        val pathReference = mStorageReference.child("images/"+friends[i].email+".jpg")
        pathReference.downloadUrl
                .addOnSuccessListener { filePath ->
                    Picasso.get().load(filePath).into(viewHolder.ivPicture)
                    Log.d(tag, "down avatar: success")
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "down avatar: failure\n"+exception.message)
                }

        viewHolder.tvFriendName.text = friends[i].name
        if (context.javaClass.simpleName == "FriendActivity"){
            viewHolder.tvVerify!!.text = if(friends[i].isVerified) "" else "Waiting for Accept"
        } else {
            viewHolder.tvVerify!!.visibility = View.GONE
        }

        if (context.javaClass.simpleName != "FriendInfActivity"){
            viewHolder.btnAccept!!.visibility = View.GONE
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        var tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)
        var tvVerify: TextView? = itemView.findViewById(R.id.tvVerify)
        var btnAccept: Button? = itemView.findViewById(R.id.btnAccept)

        init {
            when {
                context.javaClass.simpleName == "AddFriendActivity" -> itemView.setOnClickListener {
                    val friend = itemView.tag as Friend
                    db.collection("users").document(friend.uid).collection("invitations").document(user!!.uid)
                            .set(mapOf("isAccept" to false), SetOptions.merge()).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Add Friend to database: Success")
                                    // updateUI(user)
                                } else {
                                    Log.d(tag, "Add Friend to database: Fail")
                                }
                            }

                    db.collection("users").document(user!!.uid).collection("friends").document(friend.uid)
                            .set(mapOf("isVerified" to false), SetOptions.merge()).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Add Friend to database: Success")
                                    val activity = context as Activity
                                    activity.finish()
                                    // updateUI(user)
                                } else {
                                    Log.d(tag, "Add Friend to database: Fail")
                                }
                            }
                }

                context.javaClass.simpleName == "FriendActivity" -> itemView.setOnClickListener {
                    val friend = itemView.tag as Friend
                    if (friend.isVerified){
                        val intent = Intent(context, FriendProfile::class.java)
                        intent.putExtra("friend", friend)
                        context.startActivity(intent)
                    }
                }

                context.javaClass.simpleName == "FriendInfActivity" -> itemView.setOnClickListener {

                }
            }
        }
    }
}