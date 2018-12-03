package com.example.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChooseCoinActivity : AppCompatActivity() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var rvCoinChoose: RecyclerView? = null
    private var myAdapter: PointAdapter? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

    private var inf: Array<String>? = null
    private var points: ArrayList<Point>? = null
    private val tag = "ChooseCoinActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_coin)
        title = "Central Bank"

        inf = intent.getStringArrayExtra("inf")

        rvCoinChoose = findViewById(R.id.rvCoinChoose)
        rvCoinChoose!!.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this@ChooseCoinActivity, LinearLayoutManager.VERTICAL, false)
        rvCoinChoose!!.layoutManager = layoutManager
        getAllCoins()

        myAdapter = PointAdapter(this@ChooseCoinActivity, points!!)
        rvCoinChoose!!.adapter = myAdapter
    }

    private fun getAllCoins(){
        db.collection("users").document(user!!.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        points = task.result!!.toObject(User::class.java)!!.balance[inf!![0]]!!
                        Log.d(tag, "Get points: success")
                    } else {
                        Log.w(tag, "Get points: fail")
                    }
                }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val intent = Intent(this@ChooseCoinActivity, SpareExchangeActivity::class.java)
        intent.putExtra("points", points)
        startActivity(intent)
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (inf!![1] != "balance"){
            menuInflater.inflate(R.menu.choose_coin_check, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }
}
