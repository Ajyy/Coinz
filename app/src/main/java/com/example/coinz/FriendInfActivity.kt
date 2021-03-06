package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView

// Friend information include invite, invitation and history information
class FriendInfActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecyclerView.Adapter<FriendAdapter.ViewHolder>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var tvFriendInfInf: TextView? = null

    private var friendInf = ArrayList<Friend>()
    private val tag = "FriendInfActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_inf)

        title = "Information"
        tvFriendInfInf = findViewById(R.id.tvFriendInfInf)

        recyclerView = findViewById(R.id.friendInf)
        recyclerView!!.setHasFixedSize(true)

        myAdapter = FriendAdapter(this@FriendInfActivity, friendInf)

        layoutManager = LinearLayoutManager(this@FriendInfActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager

        recyclerView!!.adapter = myAdapter

        var num = 0
        // Get the invitation information
        User.userDb.document(User.userAuth!!.uid).collection("invitation").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Get invited friend list: Success")

                        for (document in task.result!!){
                            friendInf.add(Friend(uid = document.data["id"] as String, isAccepted = document.data["isAccepted"] as Long, name = document.data["name"] as String, email = document.data["email"] as String))
                        }

                        num+= task.result!!.size()
                    } else {
                        Log.w(tag, "Get invited friend list: Fail", task.exception)
                    }
                }

        // Get the invite information
        User.userDb.document(User.userAuth!!.uid).collection("invite").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d(tag, "Get invited friend list: Success")

                        for (document in task.result!!){
                            friendInf.add(Friend(uid = document.data["id"] as String, isVerified = document.data["isVerified"] as Long, name = document.data["name"] as String, email = document.data["email"] as String))
                        }

                        myAdapter!!.notifyDataSetChanged()
                        num+= task.result!!.size()
                        tvFriendInfInf!!.text = "There is/are $num pieces of information"
                    } else {
                        Log.w(tag, "Get invited friend list: Fail", task.exception)
                    }
                }
    }
}
