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

        myAdapter = PointAdapter(this@ChooseCoinActivity, points, inf!![1])
        rvCoinChoose!!.adapter = myAdapter
        getAllCoins()
    }

    private fun getAllCoins(){
        db.collection("users").document(user!!.uid).collection("balance_"+inf!![0]).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        points.clear()
                        for (document in task.result!!){
                            points.add(document.toObject(Point::class.java))
                        }

                        tvCoinInf!!.text = "Find ${points.size} coins whose type is ${inf!![0]}"

                        if (rvCoinChoose!!.adapter != null){
                            myAdapter!!.notifyDataSetChanged()
                        }

                        Log.d(tag, "Get points: success")
                    } else {
                        Log.w(tag, "Get points: fail")
                    }
                }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (inf!![1] != "balance"){
            val checkPoints = ArrayList<Point>()
            for (point in points){
                if (point.isChecked!!){
                    checkPoints.add(point)
                }
            }

            val intent: Intent
            if (inf!![1] == "spare"){
                intent = Intent(this@ChooseCoinActivity, SpareExchangeActivity::class.java)
            } else {
                intent = Intent(this@ChooseCoinActivity, DepositSubmitActivity::class.java)
            }

            if (checkPoints.size != 0){
                intent.putExtra("points", checkPoints)
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
