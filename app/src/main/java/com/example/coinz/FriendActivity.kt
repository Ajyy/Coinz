package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem

class FriendActivity : AppCompatActivity() {

    private var friends = ArrayList<Friend>()
    private var recyclerView: RecyclerView? = null
    private var myAdapter: FriendAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null

    private val addFriendActivity = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        recyclerView = findViewById(R.id.friend_list)
        recyclerView!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this@FriendActivity)
        recyclerView!!.layoutManager = layoutManager

        myAdapter = FriendAdapter(this@FriendActivity, friends)

        recyclerView!!.adapter = myAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.friend_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        startActivityForResult(Intent(this@FriendActivity, AddFriendActivity::class.java), addFriendActivity)
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addFriendActivity){
            if (resultCode == Activity.RESULT_OK){

            } else if (resultCode == Activity.RESULT_CANCELED){

            }
        }
    }
}
