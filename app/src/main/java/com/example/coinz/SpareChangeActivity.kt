package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpareChangeActivity : AppCompatActivity() {

    private var etSpareChange: EditText? = null
    private var btnSpareSearch: Button? = null
    private var rvSpareFriends: RecyclerView? = null
    private var tvSpareInf: TextView? = null
    private var myAdapter: FriendAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? =null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

    private var spareFriends = ArrayList<Friend>()
    private var tag = "SpareChangeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spare_change)

        title = "Central Bank"

        etSpareChange = findViewById(R.id.etSpareName)
        btnSpareSearch = findViewById(R.id.btnSpareSearch)
        rvSpareFriends = findViewById(R.id.rvSpareFriends)
        tvSpareInf = findViewById(R.id.tvSpareInf)

        layoutManager = LinearLayoutManager(this@SpareChangeActivity, LinearLayoutManager.VERTICAL, false)
        rvSpareFriends!!.layoutManager = layoutManager

        myAdapter = FriendAdapter(this@SpareChangeActivity, spareFriends)
        rvSpareFriends!!.adapter = myAdapter

        btnSpareSearch!!.setOnClickListener{
            getFriendData()
        }
    }

    private fun getFriendData(){
        if (!etSpareChange!!.text.isEmpty()){
            val name = etSpareChange!!.text.toString()
            spareFriends.clear()

            db.collection("users").document(user!!.uid).collection("friends").whereEqualTo("name", name).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            for (document in task.result!!){
                                spareFriends.add(Friend(uid = document.id, name = document["name"] as String))
                            }

                            tvSpareInf!!.text = "Find ${task.result!!.size()} friend(s)"

                            myAdapter!!.notifyDataSetChanged()
                            Log.d(tag, "search friend: success")
                        } else {
                            Log.d(tag, "search friend: fail")
                        }
                    }
        }
    }
}
