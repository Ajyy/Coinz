package com.example.coinz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class SpareExchangeActivity : AppCompatActivity() {

    private var rgSpareType: RadioGroup? = null
    private var btnChooseSpare: Button? = null
    private var btnSubmitSpare: Button? = null
    private var tvSpareInf: TextView? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser
    private var mDatabase = FirebaseDatabase.getInstance().reference

    private var friendData: User? = null
    private var userData: User? = null
    private var friendId: String? = null
    private var tvCoinInf: TextView? = null
    private var now = Calendar.getInstance()

    private var tag = "SpareExchangeActivity"
    private val chooseCoinActivity = 2
    private var coins = ArrayList<Point>()
    private var totalValue = 0.0
    private var num = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spare_exchange)

        title = "Central Bank"

        rgSpareType = findViewById(R.id.rgSpareType)

        btnChooseSpare = findViewById(R.id.btnChooseSpare)
        btnSubmitSpare = findViewById(R.id.btnSubmitSpare)
        tvSpareInf = findViewById(R.id.tvSpareInf)
        tvCoinInf = findViewById(R.id.tvCoinInf)

        tvCoinInf!!.text = "Please choose coins"

        friendId = intent.getStringExtra("friendId")
        getData("friend", friendId!!)
        getData("user", user!!.uid)

        rgSpareType!!.setOnClickListener{
            coins.clear()
            tvCoinInf!!.text = "Please choose coins"
        }

        btnChooseSpare!!.setOnClickListener {
            val coinType: String = when{
                rgSpareType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                rgSpareType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                rgSpareType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                else  -> "PENY"
            }

            val intent = Intent(this@SpareExchangeActivity, ChooseCoinActivity::class.java)
            intent.putExtra("inf", arrayOf(coinType, "spare"))
            startActivityForResult(intent, chooseCoinActivity)
        }

        btnSubmitSpare!!.setOnClickListener {
            if (coins.size != 0){
                if (userData!!.limit >= 25){
                    val coinType: String = when{
                        rgSpareType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                        rgSpareType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                        rgSpareType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                        else  -> "PENY"
                    }

                    updateDeposit("friend", coinType)
                    updateDeposit("user", coinType)
                    userData!!.demandDeposit[coinType] = userData!!.demandDeposit[coinType]!!+totalValue
                    friendData!!.demandDeposit[coinType] = friendData!!.demandDeposit[coinType]!!+totalValue
                    userData!!.isExchange = true

                    User.deleteBalance(coins, coinType)

                    updateInf("friend", coinType)
                    updateInf("user", coinType)

                    Toast.makeText(this@SpareExchangeActivity, "Exchange Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    tvSpareInf!!.text = "You should deposit more than 25 coins"
                }
            } else {
                tvSpareInf!!.text = "Please choose some coins"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == chooseCoinActivity){
            if (resultCode == Activity.RESULT_OK){
                coins = data!!.getSerializableExtra("points") as ArrayList<Point>
                for (point in coins) if (point.isChecked) {
                    totalValue+=point.value
                    num++
                }
                tvCoinInf!!.text = "Coin number: $num Coin Value: ${totalValue}"
            }
        }
    }

    private fun updateDeposit(type: String, coinType: String){
        val data: User = if (type == "friend") friendData!! else userData!!

        if (data.demandTime[coinType] != "no"){
            val lastCal = Calendar.getInstance()
            lastCal.set(data.demandTime[coinType]!!.substring(6, 10).toInt(), data.demandTime[coinType]!!.substring(0, 2).toInt(),
                    data.demandTime[coinType]!!.substring(3, 5).toInt())
            val num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
            data.demandDeposit[coinType] = data.demandDeposit[coinType]!! *(1+(0.35/360)*num)
        }
    }

    private fun getData(type: String, id: String){
        db.collection("users").document(id).get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        if (type == "friend") friendData = task.result!!.toObject(User::class.java)
                        else if (type == "user") userData = task.result!!.toObject(User::class.java)

                        Log.d(tag, "get friend data: Success")
                    } else {
                        Log.w(tag, "get friend data: fail")
                    }
                }
    }

    private fun updateInf(type: String, coinType: String){
        val id: String = if (type == "friend") friendId!! else user!!.uid
        val data: User = if (type == "friend") friendData!! else userData!!

        db.collection("user").document(id).update(
                "demandDeposit.$coinType", data.demandDeposit[coinType],
                "isExchange", data.isExchange
        )

        val chatId = if (friendId!! < user!!.uid) friendId+user!!.uid else user!!.uid+friendId
        val message = ChatMessage()
        message.messageTime = Date().time
        message.messageUserName = userData!!.name
        message.messageText = "I send you $num $coinType whose value is $totalValue from my spare change, " +
                "you can check you balance~"

        mDatabase.child(chatId).push().setValue(message)
    }
}
