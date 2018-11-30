package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.firebase.ui.database.FirebaseListAdapter
import com.google.firebase.database.FirebaseDatabase

class ChatActivity : AppCompatActivity() {

    private var btnSubmitMessage: Button? = null
    private var etMessageText: EditText? = null
    private var messageList: ListView? = null
    private var myAdapter: FirebaseListAdapter<ChatMessage>? = null

    private var mDatabase = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        btnSubmitMessage = findViewById(R.id.btnSubmitMessage)
        etMessageText = findViewById(R.id.etMessageText)
        messageList = findViewById(R.id.messageList)

        btnSubmitMessage!!.setOnClickListener {
            val message = etMessageText
            mDatabase.push().setValue(ChatMessage(message!!.text.toString()))
            message.setText("")
            etMessageText = message
        }

        myAdapter = object: FirebaseListAdapter<ChatMessage>(this@ChatActivity, ChatMessage::class.java, R.layout.message_list, mDatabase){
            override fun populateView(v: View?, model: ChatMessage?, position: Int) {
                val tvMessageTime: TextView = findViewById(R.id.tvMessageTime)
                val tvMessage: TextView = findViewById(R.id.tvMessage)

                tvMessageTime.text = DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model!!.messageTime)
                tvMessage.text = model.messageText
            }
        }

        messageList!!.adapter = myAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.friend_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
