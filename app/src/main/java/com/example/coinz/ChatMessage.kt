package com.example.coinz

import java.util.Date

class ChatMessage{
    var messageText: String? = null
    var messageTime: Long = Date().time
    var messageUserName: String? = null
}