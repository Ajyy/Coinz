package com.example.coinz

import android.app.Activity
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
import com.google.firebase.firestore.FirebaseFirestore


class FriendActivity : AppCompatActivity() {

    private var friends = ArrayList<Friend>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: FriendAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var tvFriendInf: TextView? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

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
            val intent = Intent(this@FriendActivity, AddFriendActivity::class.java)
            intent.putExtra("friendsList", friends)
            startActivityForResult(intent, addFriendActivity)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addFriendActivity){
            if (resultCode == Activity.RESULT_OK){
                friends.clear()
                getFriends()
                myAdapter!!.notifyDataSetChanged()
            } else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this@FriendActivity, "No data received", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == friendInfActivity){
            friends.clear()
            getFriends()
            myAdapter!!.notifyDataSetChanged()
        }
    }

    private fun getFriends(){
        val userDocRef = db.collection("users")
        userDocRef.document(user!!.uid).collection("friends").get()
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        Log.d(tag, "getFriend: Success")
                        for (document1 in task1.result!!){
                            if (document1!!.exists()){
                                val friend = document1.data
                                userDocRef.document(document1.id).get()
                                        .addOnCompleteListener{ task2 ->
                                            if (task2.isSuccessful){
                                                Log.d(tag, "getFriendUser: Success")

                                                val document2 = task2.result
                                                if (document2!!.exists()){
                                                    val userFriend = document2.toObject(User::class.java)
                                                    print(userFriend?.name)
                                                    friends.add(Friend(document2.id, userFriend!!.name, userFriend.email!!,
                                                            userFriend.age!!, userFriend.gender, userFriend.todayStep, friend["isVerified"] as Boolean))

                                                    if (myAdapter != null){
                                                        myAdapter!!.notifyDataSetChanged()
                                                    }

                                                } else {
                                                    Log.w(tag, "No such user")
                                                }
                                            } else {
                                                Log.w(tag, "getFriendUser: Fail")
                                            }
                                        }
                            } else {
                                Log.w(tag, "No friend")
                            }
                        }

                        tvFriendInf!!.text = "Find "+ task1.result!!.size()+" friend(s)"
                    } else {
                        Log.w(tag, "getFriend: Fail")
                    }
                }
    }
}
