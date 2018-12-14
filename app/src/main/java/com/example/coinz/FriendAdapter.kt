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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

// Friend adapter
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

        // Get the avatar of the friends
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
            // Only FriendInfActivity need the following things
            viewHolder.tvFriInf!!.visibility = View.GONE
            viewHolder.btnAccept!!.visibility = View.GONE
            viewHolder.btnReject!!.visibility = View.GONE
        } else {
            if (friends[i].isAccepted == -1L){
                viewHolder.btnAccept!!.setOnClickListener {
                    updateInviteInf(friends[i].uid, friends[i].name, friends[i].email, 1)
                    viewHolder.btnAccept!!.visibility = View.GONE
                    viewHolder.btnReject!!.visibility = View.GONE
                    viewHolder.tvFriInf!!.text = "Accepted"
                }

                viewHolder.btnReject!!.setOnClickListener {
                    updateInviteInf(friends[i].uid, friends[i].name, friends[i].email,0)
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

            when {
                friends[i].isVerified == 0L -> viewHolder.tvFriInf!!.text = "Respond: Rejected"
                friends[i].isVerified == 1L -> viewHolder.tvFriInf!!.text = "Respond: Accepted"
                friends[i].isVerified == -1L -> viewHolder.tvFriInf!!.text = "Wait for Accept"
            }
        }

        if (context.javaClass.simpleName == "FriendActivity"){
            val totalBal = String.format("%.4f", friends[i].totalBal)
            viewHolder.tvTotalBal.text = "Total Balance: $totalBal"
        } else {
            viewHolder.tvTotalBal.visibility = View.GONE
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        var tvFriendName: TextView = itemView.findViewById(R.id.tvFriendName)
        var tvTotalBal: TextView = itemView.findViewById(R.id.tvTotalBal)
        var tvFriInf: TextView? = itemView.findViewById(R.id.tvFriInf)
        var btnAccept: Button? = itemView.findViewById(R.id.btnAccept)
        var btnReject: Button? = itemView.findViewById(R.id.btnReject)

        init {
            when {
                context.javaClass.simpleName == "AddFriendActivity" -> itemView.setOnClickListener {
                    val friend = itemView.tag as Friend
                    User.userDb.document(friend.uid).collection("invitation")
                            .add(mapOf("id" to User.userAuth!!.uid,"isAccepted" to -1, "name" to User.userAuth!!.displayName, "email" to User.userAuth!!.email)).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Add Friend to database: Success")
                                } else {
                                    Log.d(tag, "Add Friend to database: Fail", task.exception)
                                }
                            }

                    User.userDb.document(User.userAuth!!.uid).collection("invite")
                            .add(mapOf("id" to friend.uid, "isVerified" to -1, "name" to friend.name, "email" to User.userAuth!!.email)).addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    Log.d(tag, "Add Friend to database: Success")
                                    val activity = context as Activity
                                    Toast.makeText(context, "Invitation sent", Toast.LENGTH_LONG).show()
                                    activity.finish()
                                } else {
                                    Log.d(tag, "Add Friend to database: Fail", task.exception)
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

    // Update the information when player accept or reject
    private fun updateInviteInf(friendId: String, friName: String, friEmail: String, isAccepted: Int){
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
                                                Log.w(tag, "Update invitation information: Fail", task2.exception)
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail", task1.exception)
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
                                                Log.w(tag, "Update invitation information: Fail", task2.exception)
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail", task1.exception)
                        }
                    }

            User.userDb.document(User.userAuth!!.uid).collection("friends").document(friendId).set(mapOf("name" to friName, "email" to friEmail))
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            Log.d(tag, "Add friend: Success")
                        } else {
                            Log.w(tag, "Add friend: Fail", task1.exception)
                        }
                    }

            User.userDb.document(friendId).collection("friends").document(User.userAuth!!.uid).set(mapOf("name" to User.userAuth!!.displayName, "email" to friEmail))
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful){
                            Log.d(tag, "Add friend: Success")
                        } else {
                            Log.w(tag, "Add friend: Fail", task1.exception)
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
                                                Log.w(tag, "Update invitation information: Fail", task2.exception)
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail", task1.exception)
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
                                                Log.w(tag, "Update invitation information: Fail", task2.exception)
                                            }
                                        }
                            }
                        } else {
                            Log.w(tag, "Update invitation information: Fail", task1.exception)
                        }
                    }
        }
    }
}
