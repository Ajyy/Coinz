package com.example.coinz

import com.google.firebase.firestore.FirebaseFirestore

class Achievement(var id: Int, var title: String, var intro: String, var isGet: Boolean = false){
    companion object {
        var achieveDb = FirebaseFirestore.getInstance().collection("achievements")
    }
}
