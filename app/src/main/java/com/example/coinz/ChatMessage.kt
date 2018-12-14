package com.example.coinz

import java.util.Date

// Coin class, please check document to see detail information
class ChatMessage{
    var messageText: String? = null
    var messageTime: Long = Date().time
    var messageUserName: String? = null
}