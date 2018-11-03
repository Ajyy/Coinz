package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendActivity : AppCompatActivity() {

    private var etFriendName: EditText? = null
    private var btnSearch: Button? = null
    private var tvSearchInf: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecyclerView.Adapter<FriendAdapter.ViewHolder>? = null
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
        tvSearchInf = findViewById(R.id.tvSearchInf)

        recyclerView = findViewById(R.id.friendSearch)
        recyclerView!!.setHasFixedSize(true)

        myAdapter = FriendAdapter(this@AddFriendActivity, friends)

        layoutManager = LinearLayoutManager(this@AddFriendActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager

        recyclerView!!.adapter = myAdapter

        btnSearch!!.setOnClickListener {
            friends.clear()
            if (etFriendName!!.text.isEmpty()){
                tvSearchInf!!.text = "Please enter the name"
            } else {
                val query = db!!.collection("users").whereEqualTo("name", etFriendName!!.text.toString())
                query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        for (document in task.result!!){
                            val searchUser = document.toObject(User::class.java)
                            friends.add(Friend(document.id, searchUser.name!!, searchUser.email!!, searchUser.age!!, searchUser.gender!!, searchUser.todayStep))
                        }

                        myAdapter!!.notifyDataSetChanged()
                        Log.d(tag, "${myAdapter!!.itemCount}")

                        Log.d(tag, "SearchFriend: Success")
                        tvSearchInf!!.text = "Find ${task.result!!.size()} people"
                    } else {
                        tvSearchInf!!.text = "Fail to Search"
                        Log.w(tag, "SearchFriend: Fail")
                    }
                }
            }
        }
    }
}
