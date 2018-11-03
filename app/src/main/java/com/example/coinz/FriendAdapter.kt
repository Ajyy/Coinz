package com.example.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class FriendAdapter(private val context: Context, private val friends: ArrayList<Friend>): RecyclerView.Adapter<FriendAdapter.ViewHolder>(){

    private var mStorageReference = FirebaseStorage.getInstance().reference

    private val tag = "FriendActivity"

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.friend_list, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
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
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        var tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)

        init {
            if (context.javaClass.simpleName == "AddFriendActivity"){
                itemView.setOnClickListener {

                }
            } else {
                itemView.setOnClickListener {
                    val intent = Intent(context, FriendProfile::class.java)
                    intent.putExtra("friend", itemView.tag as Friend)
                    context.startActivity(intent)
                }
            }
        }
    }
}