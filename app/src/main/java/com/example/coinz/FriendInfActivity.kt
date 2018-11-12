package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendInfActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecyclerView.Adapter<FriendAdapter.ViewHolder>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    private var friendInf = ArrayList<Friend>()

    private var user = FirebaseAuth.getInstance().currentUser
    private var db = FirebaseFirestore.getInstance()

    private val tag = "FriendInfActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_inf)

        recyclerView = findViewById(R.id.friendInf)
        recyclerView!!.setHasFixedSize(true)

        myAdapter = FriendAdapter(this@FriendInfActivity, friendInf)

        layoutManager = LinearLayoutManager(this@FriendInfActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager

        recyclerView!!.adapter = myAdapter

        db.collection("users").document(user!!.uid).collection("invitations").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.w(tag, "Get invited friend list: Success")

                        for (document1 in task1.result!!){
                            db.collection("users").document(document1.id).get()
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful){
                                            val friend = task2.result!!.toObject(User::class.java)
                                            friendInf.add(Friend(document1.id, friend!!.name!!, friend.email!!, friend.age!!,
                                                    friend.gender!!, friend.todayStep, isAccept = document1.data["isAccept"] as Boolean))

                                            Log.d(tag, "Get invited friend: Success")
                                        } else {
                                            Log.w(tag, "Get invited friend: Fail")
                                        }
                                    }
                        }

                        myAdapter!!.notifyDataSetChanged()
                    } else {
                        Log.w(tag, "Get invited friend list: Fail")
                    }
                }
    }
}
