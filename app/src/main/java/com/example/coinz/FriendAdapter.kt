package com.example.coinz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class FriendAdapter(private val context: Context, private val friends: ArrayList<Friend>): RecyclerView.Adapter<FriendAdapter.ViewHolder>(){

    private var mStorageReference = FirebaseStorage.getInstance().reference
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
                    viewHolder.ivPicture.setImageResource(R.mipmap.ic_launcher_round)
                }

        viewHolder.tvFriendName.text = friends[i].name
        if (context.javaClass.simpleName != "FriendInfActivity") {
            viewHolder.tvFriInf!!.visibility = View.GONE
            viewHolder.btnAccept!!.visibility = View.GONE
            viewHolder.btnReject!!.visibility = View.GONE
        } else {
            if (friends[i].isAccepted == -1L){
                viewHolder.btnAccept!!.setOnClickListener {
                    updateInviteInf(friends[i].uid, friends[i].name,1)
                    viewHolder.btnAccept!!.visibility = View.GONE
                    viewHolder.btnReject!!.visibility = View.GONE
                    viewHolder.tvFriInf!!.text = "Accepted"
                }

                viewHolder.btnReject!!.setOnClickListener {
                    updateInviteInf(friends[i].uid, friends[i].name,0)
                    viewHolder.btnAccept!!.visibility = View.GONE
                    viewHolder.btnReject!!.visibility = View.GONE
                    viewHolder.tvFriInf!!.text = "Rejected"
                }
            } else {
                viewHolder.btnAccept!!.visibility = View.GONE
                viewHolder.btnReject!!.visibility = View.GONE
                if (friends[i].isAccepted == 0L){
                    viewHolder.tvFriInf!!.text = "Rejected"
                } else if (friends[i].isAccepted == 1L) {
                    viewHolder.tvFriInf!!.text = "Accepted"
                }
            }

            if (friends[i].isVerified == 0L){
                viewHolder.tvFriInf!!.text = "Respond: Rejected"
            } else if (friends[i].isVerified == 1L) {
                viewHolder.tvFriInf!!.text = "Respond: Accepted"
            } else if (friends[i].isVerified == -1L) {
                viewHolder.tvFriInf!!.text = "Wait for Accept"
            }
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        var tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)
        var tvFriInf: TextView? = itemView.findViewById(R.id.tvFriInf)
        var btnAccept: Button? = itemView.findViewById(R.id.btnAccept)
        var btnReject: Button? = itemView.findViewById(R.id.btnReject)

        init {
            when {
                context.javaClass.simpleName == "AddFriendActivity" -> itemView.setOnClickListener {
                    val friend = itemView.tag as Friend
                    User.userDb.document(friend.uid).collection("invitation")
                            .add(mapOf("id" to User.userAuth!!.uid,"isAccepted" to -1, "name" to User.userAuth!!.displayName)).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Add Friend to database: Success")
                                } else {
                                    Log.d(tag, "Add Friend to database: Fail")
                                }
                            }

                    User.userDb.document(User.userAuth!!.uid).collection("invite")
                            .add(mapOf("id" to friend.uid, "isVerified" to -1, "name" to friend.name)).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Add Friend to database: Success")
                                    val activity = context as Activity
                                    Toast.makeText(context, "Invitation sent", Toast.LENGTH_LONG).show()
                                    activity.finish()
                                } else {
                                    Log.d(tag, "Add Friend to database: Fail")
                                }
                            }
                }

                context.javaClass.simpleName == "FriendActivity" -> itemView.setOnClickListener {
                    val friend = itemView.tag as Friend
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("friend", friend)
                    context.startActivity(intent)
                }

                context.javaClass.simpleName == "SpareChangeActivity" -> itemView.setOnClickListener{
                    val friend = itemView.tag as Friend
                    val friendId = friend.uid

                    val intent = Intent(context, SpareExchangeActivity::class.java)
                    intent.putExtra("friendId", friendId)
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun updateInviteInf(friendId: String, friName: String, isAccepted: Int){
        if (isAccepted == 1){
            User.userDb.document(User.userAuth!!.uid).collection("invitation").whereEqualTo("id", friendId).get()
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            for (document in task1.result!!){
                                User.userDb.document(User.userAuth!!.uid).collection("invitation").document(document.id).update("isAccepted", 1)
                                        .addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "Update invitation information: Success")
                                            } else {
                                                Log.w(tag, "Update invitation information: Fail")
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail")
                        }
                    }

            User.userDb.document(friendId).collection("invite").whereEqualTo("id", User.userAuth!!.uid).get()
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            for (document in task1.result!!){
                                User.userDb.document(friendId).collection("invite").document(document.id).update("isVerified", 1)
                                        .addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "Update invitation information: Success")
                                            } else {
                                                Log.w(tag, "Update invitation information: Fail")
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail")
                        }
                    }

            User.userDb.document(User.userAuth!!.uid).collection("friends").document(friendId).set(mapOf("name" to friName))
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            Log.d(tag, "Add friend: Success")
                        } else {
                            Log.w(tag, "Add friend: Fail")
                        }
                    }

            User.userDb.document(friendId).collection("friends").document(User.userAuth!!.uid).set(mapOf("name" to User.userAuth!!.displayName))
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            Log.d(tag, "Add friend: Success")
                        } else {
                            Log.w(tag, "Add friend: Fail")
                        }
                    }
        } else {
            User.userDb.document(User.userAuth!!.uid).collection("invitation").whereEqualTo("id", friendId).get()
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            for (document in task1.result!!){
                                User.userDb.document(User.userAuth!!.uid).collection("invitation").document(document.id).update("isAccepted", 0)
                                        .addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "Update invitation information: Success")
                                            } else {
                                                Log.w(tag, "Update invitation information: Fail")
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail")
                        }
                    }

            User.userDb.document(friendId).collection("invite").whereEqualTo("id", User.userAuth!!.uid).get()
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            for (document in task1.result!!){
                                User.userDb.document(friendId).collection("invite").document(document.id).update("isVerified", 0)
                                        .addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "Update invitation information: Success")
                                            } else {
                                                Log.w(tag, "Update invitation information: Fail")
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail")
                        }
                    }
        }
    }
}