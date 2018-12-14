package com.example.coinz

import com.google.firebase.firestore.FirebaseFirestore

// Achievement class, please check document to see detail information
class Achievement(var id: Long = 0, var title: String = "", var intro: String = "", var get: Boolean = false){
    companion object {
        // The collection reference to "achievements" collection
        var achieveDb = FirebaseFirestore.getInstance().collection("achievements")
    }
}
