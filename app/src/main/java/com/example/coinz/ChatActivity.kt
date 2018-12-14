package com.example.coinz

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.firebase.ui.database.FirebaseListAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// This activity is used to chat with friend
class ChatActivity : AppCompatActivity() {

    private var btnSubmitMessage: ImageView? = null
    private var etMessageText: EditText? = null
    private var messageList: ListView? = null
    private var myAdapter: FirebaseListAdapter<ChatMessage>? = null

    private var mDatabase: DatabaseReference? = null

    private var friend: Friend? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        friend = intent.getSerializableExtra("friend") as Friend

        val id = if (friend!!.uid < User.userAuth!!.uid) friend!!.uid+User.userAuth!!.uid else User.userAuth!!.uid+friend!!.uid
        mDatabase = FirebaseDatabase.getInstance().reference.child(id)

        btnSubmitMessage = findViewById(R.id.btnSubmitMessage)
        etMessageText = findViewById(R.id.etMessageText)
        messageList = findViewById(R.id.messageList)

        btnSubmitMessage!!.setOnClickListener {
            val message = etMessageText

            // Check whether the text has nothing
            if (message!!.text.toString() != ""){
                val chatMessage = ChatMessage()
                chatMessage.messageText = message.text.toString()
                chatMessage.messageUserName = User.userAuth!!.displayName
                // Push to real-time database and reset text to nothing
                mDatabase!!.push().setValue(chatMessage)
                message.setText("")
                etMessageText = message
            }
        }

        // Initialize the adapter
        myAdapter = object: FirebaseListAdapter<ChatMessage>(this@ChatActivity, ChatMessage::class.java, R.layout.message_list, mDatabase){
            override fun populateView(v: View?, model: ChatMessage?, position: Int) {
                val tvMessageTime: TextView = v!!.findViewById(R.id.tvMessageTime)
                val tvMessage: TextView = v.findViewById(R.id.tvMessage)

                @SuppressLint("SetTextI18n")
                tvMessageTime.text = model!!.messageUserName+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.messageTime)
                tvMessage.text = model.messageText
            }
        }

        messageList!!.adapter = myAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_inf, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.message_inf){
            // To see the profile of friend
            val intent = Intent(this@ChatActivity, FriendProfile::class.java)
            intent.putExtra("friend", friend)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}
