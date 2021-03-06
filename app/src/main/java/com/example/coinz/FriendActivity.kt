package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

// This activity is used for friends' list
class FriendActivity : AppCompatActivity() {
    private var friends = ArrayList<Friend>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: FriendAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var tvFriendInf: TextView? = null

    private val addFriendActivity = 2
    private val tag = "FriendActivity"
    private val friendInfActivity = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        title = "Friends' List"
        tvFriendInf = findViewById(R.id.tvFriendsInf)

        recyclerView = findViewById(R.id.friend_list)
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@FriendActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager
        getFriends()

        myAdapter = FriendAdapter(this@FriendActivity, friends)

        recyclerView!!.adapter = myAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.friend_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.inf_icon){
            startActivityForResult(Intent(this@FriendActivity, FriendInfActivity::class.java), friendInfActivity)
        } else {
            User.mAuth = FirebaseAuth.getInstance()
            User.userAuth = User.mAuth.currentUser
            if (User.userAuth!!.isEmailVerified){
                val intent = Intent(this@FriendActivity, AddFriendActivity::class.java)
                intent.putExtra("friendsList", friends)
                startActivityForResult(intent, addFriendActivity)
            } else {
                Toast.makeText(this@FriendActivity, "Please confirm your email first of all!", Toast.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addFriendActivity){
            friends.clear()
            getFriends()
            myAdapter!!.notifyDataSetChanged()
        } else if (requestCode == friendInfActivity){
            friends.clear()
            getFriends()
            myAdapter!!.notifyDataSetChanged()
        }
    }

    // Get friends from Firebase
    private fun getFriends(){
        User.userDb.document(User.userAuth!!.uid).collection("friends").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.d(tag, "getFriend: Success")
                        for (document1 in task1.result!!){
                            if (document1!!.exists()){
                                // Get detailed information
                                User.userDb.document(document1.id).get()
                                        .addOnCompleteListener{ task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "getFriendUser: Success")

                                                val document2 = task2.result
                                                if (document2!!.exists()){
                                                    val userFriend = document2.toObject(User::class.java)
                                                    friends.add(Friend(document2.id, userFriend!!.name, userFriend.email!!,
                                                            userFriend.age!!, userFriend.totalBal, userFriend.gender))

                                                    if (myAdapter != null){
                                                        myAdapter!!.notifyDataSetChanged()
                                                    }

                                                } else {
                                                    Log.w(tag, "No such user")
                                                }
                                            } else {
                                                Log.w(tag, "getFriendUser: Fail", task2.exception)
                                            }
                                        }
                            } else {
                                Log.w(tag, "No friend")
                            }
                        }

                        tvFriendInf!!.text = "Find "+ task1.result!!.size()+" friend(s)"
                    } else {
                        Log.w(tag, "getFriend: Fail", task1.exception)
                    }
                }
    }
}
