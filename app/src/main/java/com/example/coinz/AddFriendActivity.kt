package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendActivity : AppCompatActivity() {

    private var etFriendName: EditText? = null
    private var btnSearch: Button? = null
    private var recyclerView: RecyclerView? = null
    private var myAdapter: FriendAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null

    private var db: FirebaseFirestore? = null

    private var friends = ArrayList<Friend>()
    private val tag = "AddFriendActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        db = FirebaseFirestore.getInstance()

        etFriendName = findViewById(R.id.etFriendName)
        btnSearch = findViewById(R.id.btnSearch)

        recyclerView = findViewById(R.id.friend_list)
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@AddFriendActivity)
        recyclerView!!.layoutManager = layoutManager

        myAdapter = FriendAdapter(this@AddFriendActivity, friends)

        if (etFriendName!!.text.isEmpty()){
            Toast.makeText(this@AddFriendActivity, "Please enter the name", Toast.LENGTH_SHORT).show()
        } else {
            val query = db!!.collection("users").whereEqualTo("name", etFriendName!!.text.toString())
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    for (document in task.result!!){
                        val searchUser = document.toObject(User::class.java)
                        friends.add(Friend(searchUser.name!!, searchUser.email!!, searchUser.age!!, searchUser.gender!!, searchUser.todayStep))
                    }

                    myAdapter!!.notifyDataSetChanged()

                    Log.d(tag, "SearchFriend: Success")
                } else {
                    Log.w(tag, "SearchFriend: Fail")
                }
            }
        }
    }
}
