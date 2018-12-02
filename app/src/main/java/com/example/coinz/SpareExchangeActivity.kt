package com.example.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.time.temporal.ChronoUnit
import java.util.*

class SpareExchangeActivity : AppCompatActivity() {

    private var rgSpareType: RadioGroup? = null
    private var etSpareAmount: EditText? = null
    private var btnSubmitSpare: Button? = null
    private var tvSpareInf: TextView? = null

    private var db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser
    private var mDatabase = FirebaseDatabase.getInstance().reference

    private var friendData: User? = null
    private var userData: User? = null
    private var friendId: String? = null
    private var tag = "SpareExchangeActivity"
    private var now = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spare_exchange)

        title = "Central Bank"

        rgSpareType = findViewById(R.id.rgSpareType)
        etSpareAmount = findViewById(R.id.etSpareName)
        btnSubmitSpare = findViewById(R.id.btnSubmitSpare)
        tvSpareInf = findViewById(R.id.tvSpareInf)

        friendId = intent.getStringExtra("friendId")
        getData("friend", friendId!!)
        getData("user", user!!.uid)

        btnSubmitSpare!!.setOnClickListener {
            if (userData!!.limit >= 25){
                val coinType: String = when{
                    rgSpareType!!.checkedRadioButtonId == R.id.rbGold -> "GOLD"
                    rgSpareType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                    rgSpareType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                    rgSpareType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                    else  -> "PENY"
                }

                if (userData!!.balance[coinType]!! < etSpareAmount!!.text.toString().toDouble()){
                    tvSpareInf!!.text = "Amount should be smaller than your balance, balance: $userData!!.balance[coinType]"
                } else {
                    updateDeposit("friend", coinType)
                    updateDeposit("user", coinType)
                    userData!!.demandDeposit[coinType] = userData!!.demandDeposit[coinType]!!+etSpareAmount!!.text.toString().toDouble()
                    friendData!!.demandDeposit[coinType] = friendData!!.demandDeposit[coinType]!!+etSpareAmount!!.text.toString().toDouble()
                    userData!!.isExchange = true


                }

            } else {
                tvSpareInf!!.text = "You should deposit more than 25 coins"
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
        message.messageText = "I send you ${etSpareAmount!!.text.toString().toDouble()} $coinType from my spare change, " +
                "you can check you balance~"

        mDatabase.child(chatId).push().setValue(message)
    }
}
