package com.example.coinz

import java.util.Date

data class ChatMessage(var messageText: String, var messageTime: Long = Date().time)