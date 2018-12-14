package com.example.coinz

import android.annotation.SuppressLint
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

// This activity is used when player needs to choose some coins or check the coins
class ChooseCoinActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var rvCoinChoose: RecyclerView? = null
    private var myAdapter: CoinAdapter? = null
    private var tvCoinInf: TextView? = null

    // The first element in inf is coin's type
    // The second element in inf is the information for judging which activity start this activity
    private var inf: Array<String>? = null
    private var coins = ArrayList<Coin>()
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

        myAdapter = CoinAdapter(this@ChooseCoinActivity, coins, inf!![1])
        rvCoinChoose!!.adapter = myAdapter
        getAllCoins()
    }

    // Get all coins for the specific coin's type
    private fun getAllCoins(){
        User.userDb.document(User.userAuth!!.uid).collection("balance_"+inf!![0]).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        coins.clear()
                        for (document in task.result!!){
                            coins.add(document.toObject(Coin::class.java))
                        }

                        @SuppressLint("SetTextI18n")
                        tvCoinInf!!.text = "Find ${coins.size} coins whose type is ${inf!![0]}"

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
            // Get the coins which have been checked
            val checkPoints = ArrayList<Coin>()
            for (coin in coins){
                if (coin.checked!!){
                    checkPoints.add(coin)
                }
            }

            val intent: Intent = if (inf!![1] == "spare"){
                Intent(this@ChooseCoinActivity, SpareExchangeActivity::class.java)
            } else {
                Intent(this@ChooseCoinActivity, DepositSubmitActivity::class.java)
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
