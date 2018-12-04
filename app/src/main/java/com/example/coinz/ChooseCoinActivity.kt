package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChooseCoinActivity : AppCompatActivity() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var rvCoinChoose: RecyclerView? = null
    private var myAdapter: PointAdapter? = null
    private var tvCoinInf: TextView? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

    private var inf: Array<String>? = null
    private var points = ArrayList<Point>()
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
        tvCoinInf = findViewById(R.id.tvCoinInf)
        getAllCoins()

        myAdapter = PointAdapter(this@ChooseCoinActivity, points)
        rvCoinChoose!!.adapter = myAdapter
    }

    private fun getAllCoins(){
        db.collection("users").document(user!!.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        points.clear()
                        points.addAll(task.result!!.toObject(User::class.java)!!.balance[inf!![0]]!!)
                        Log.d(tag, "Get points: success")
                    } else {
                        Log.w(tag, "Get points: fail")
                    }
                }

        tvCoinInf!!.text = "Find ${points.size} coins"
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (inf!![1] == "spare"){
            val intent = Intent(this@ChooseCoinActivity, SpareExchangeActivity::class.java)

            if (points.size != 0){
                intent.putExtra("points", points)
                setResult(Activity.RESULT_OK, intent)
            } else {
                setResult(Activity.RESULT_CANCELED, intent)
            }

        } else if (inf!![1] == "deposit"){
            val intent = Intent(this@ChooseCoinActivity, DepositSubmitActivity::class.java)

            if (points.size != 0){
                intent.putExtra("points", points)
                setResult(Activity.RESULT_OK, intent)
            } else {
                setResult(Activity.RESULT_CANCELED, intent)
            }
        }

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
