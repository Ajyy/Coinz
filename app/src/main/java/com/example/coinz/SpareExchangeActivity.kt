package com.example.coinz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

// This activity is used to exchange coins with friends for spare change
class SpareExchangeActivity : AppCompatActivity() {
    private var rgSpareType: RadioGroup? = null
    private var btnChooseSpare: Button? = null
    private var btnSubmitSpare: Button? = null
    private var tvSpareInf: TextView? = null

    private var mDatabase = FirebaseDatabase.getInstance().reference

    private var friendData = User()
    private var userData = User()
    private var friendId: String? = null
    private var tvCoinInf: TextView? = null
    private var now = Calendar.getInstance()

    private val chooseCoinActivity = 2
    private var coins = ArrayList<Coin>()
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

        tvCoinInf!!.text = getString(R.string.spare_exchenge_inf_hint1)

        friendId = intent.getStringExtra("friendId")
        friendData.getData()
        userData.getData()

        rgSpareType!!.setOnClickListener{
            coins.clear()
            tvCoinInf!!.text = getString(R.string.spare_exchenge_inf_hint1)
        }

        // Choose spare change coins' type
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
                val sdf = SimpleDateFormat("MM/dd/yyyy")
                if (sdf.format(now!!.time) == userData.depositTime){
                    if (userData.limit >= 25){
                        // Check today user has deposit how many coins
                        if (!userData.exchange){
                            // Get coins' type
                            val coinType: String = when{
                                rgSpareType!!.checkedRadioButtonId == R.id.rbShil -> "SHIL"
                                rgSpareType!!.checkedRadioButtonId == R.id.rbDolr -> "DOLR"
                                rgSpareType!!.checkedRadioButtonId == R.id.rbQuid -> "QUID"
                                else  -> "PENY"
                            }

                            // Update deposit for friend and user
                            updateDeposit("friend", coinType)
                            updateDeposit("user", coinType)
                            userData.demandDeposit[coinType] = userData.demandDeposit[coinType]!!+totalValue
                            friendData.demandDeposit[coinType] = friendData.demandDeposit[coinType]!!+totalValue
                            userData.exchange = true

                            User.deleteBalance(coins, coinType, "self")

                            updateData("friend", coinType)
                            updateData("user", coinType)

                            Toast.makeText(this@SpareExchangeActivity, "Exchange Successfully", Toast.LENGTH_SHORT).show()

                            userData.addBalance(totalValue, coinType)
                            finish()
                        } else {
                            tvSpareInf!!.text = getString(R.string.you_have_exchanged_today)
                        }
                    } else {
                        @SuppressLint("SetTextI18n")
                        tvSpareInf!!.text = "You should deposit more than or equal to 25 coins, now: ${userData.limit}"
                    }
                } else {
                    tvSpareInf!!.text = getString(R.string.please_deposit_enough_money)
                }


            } else {
                tvSpareInf!!.text = getString(R.string.spare_exchenge_inf_hint1)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == chooseCoinActivity){
            if (resultCode == Activity.RESULT_OK){
                // Get the information for the checked coins
                coins = data!!.getSerializableExtra("points") as ArrayList<Coin>
                for (point in coins) if (point.checked!!) {
                    totalValue+= point.value!!
                    num++
                }
                tvCoinInf!!.text = "Coin number: $num Coin Value: $totalValue"
            }
        }
    }

    // Update demand deposit money
    private fun updateDeposit(type: String, coinType: String){
        val data: User = if (type == "friend") friendData else userData

        if (data.demandTime[coinType] != "no"){
            val lastCal = Calendar.getInstance()
            lastCal.set(data.demandTime[coinType]!!.substring(6, 10).toInt(), data.demandTime[coinType]!!.substring(0, 2).toInt(),
                    data.demandTime[coinType]!!.substring(3, 5).toInt())
            var num = ChronoUnit.DAYS.between(lastCal.toInstant(), now!!.toInstant())
            if (num == 0L){
                if (now!!.get(Calendar.DAY_OF_MONTH) != data.demandTime[coinType]!!.substring(3, 5).toInt()){
                    num++
                }
            } else if (num > 0){
                num++
            }
            val addValue = (0.35/360)*data.demandDeposit[coinType]!!*num
            data.demandDeposit[coinType] = data.demandDeposit[coinType]!!+addValue
            userData.addBalance(addValue, coinType)
        }
    }

    // Update user's demand deposit and exchange information
    private fun updateData(type: String, coinType: String){
        val id: String = if (type == "friend") friendId!! else User.userAuth!!.uid
        val data: User = if (type == "friend") friendData else userData

        User.userDb.document(id).update(
                "demandDeposit.$coinType", data.demandDeposit[coinType],
                "exchange", data.exchange
        )

        // Send to the chat room
        val chatId = if (friendId!! < User.userAuth!!.uid) friendId+User.userAuth!!.uid else User.userAuth!!.uid+friendId
        val message = ChatMessage()
        message.messageTime = Date().time
        message.messageUserName = userData.name
        message.messageText = "I send you $num $coinType whose value is $totalValue from my spare change, " +
                "you can check you balance~"

        mDatabase.child(chatId).push().setValue(message)
    }
}
