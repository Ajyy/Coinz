package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

// This activity is used to add friend
class AddFriendActivity : AppCompatActivity() {

    private var etFriendName: EditText? = null
    private var btnSearch: Button? = null
    private var tvSearchInf: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var myAdapter: RecyclerView.Adapter<FriendAdapter.ViewHolder>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    private var searchFriend = ArrayList<Friend>()
    private var friends: ArrayList<Friend>? = null
    private val tag = "AddFriendActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        title = "Add Friend"

        // Get friends player has already added
        @Suppress("UNCHECKED_CAST")
        friends = intent.getSerializableExtra("friendsList") as ArrayList<Friend>

        etFriendName = findViewById(R.id.etFriendName)
        btnSearch = findViewById(R.id.btnSearch)
        tvSearchInf = findViewById(R.id.tvSearchInf)

        recyclerView = findViewById(R.id.friendSearch)
        recyclerView!!.setHasFixedSize(true)

        myAdapter = FriendAdapter(this@AddFriendActivity, searchFriend)

        layoutManager = LinearLayoutManager(this@AddFriendActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = layoutManager

        recyclerView!!.adapter = myAdapter

        btnSearch!!.setOnClickListener {
            getFriendData()
        }
    }

    private fun getFriendData(){
        searchFriend.clear()
        if (etFriendName!!.text.isEmpty()){
            tvSearchInf!!.text = getString(R.string.add_friend_search_info_blank)
        } else {
            // Query by friend's name
            val query = User.userDb.whereEqualTo("name", etFriendName!!.text.toString())
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    // FriendNum is used to count the number of people program find
                    var friendNum = 0
                    var size = task.result!!.size()
                    for (document in task.result!!){
                        val searchUserName = document["name"] as String
                        val searchUserVeri = document["verified"] as Boolean
                        val searchUserEmail = document["email"] as String

                        var isExist = false
                        for (friend in friends!!){
                            // Check the email whether verified
                            if (!searchUserVeri){
                                continue
                            }

                            // Check the people we find whether are already added
                            if (friend.uid == document.id){
                                friendNum+=1
                                isExist = true
                                break
                            }
                        }

                        // If these people are not the player's friend, check whether search player-self
                        if (!isExist) {
                            if (document.id != User.userAuth!!.uid){
                                searchFriend.add(Friend(uid = document.id, name = searchUserName, email = searchUserEmail))
                            } else {
                                size-=1
                            }
                        }
                    }

                    myAdapter!!.notifyDataSetChanged()
                    Log.d(tag, "SearchFriend: Success")

                    if (friendNum == 0){
                        tvSearchInf!!.text = "Find $size people"
                    } else {
                        tvSearchInf!!.text = "Find $size people, and $friendNum of them is/are your friends"
                    }
                } else {
                    tvSearchInf!!.text = "Fail to Search"
                    Log.w(tag, "SearchFriend: Fail")
                }
            }
        }
    }
}
