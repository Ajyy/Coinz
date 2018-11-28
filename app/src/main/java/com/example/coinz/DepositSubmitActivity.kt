package com.example.coinz

import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.util.*


class DepositSubmitActivity : AppCompatActivity(){

    private var rgCoinType: RadioGroup? = null
    private var etAmount: EditText? = null
    private var btnCalendar: Button? = null
    private var btnSubmitDeposit: Button? = null
    private var tvDepositInf: TextView? = null

    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit_submit)

        rgCoinType = findViewById(R.id.rgCoinType)
        etAmount = findViewById(R.id.etAmount)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnSubmitDeposit = findViewById(R.id.btnSubmitDeposit)
        tvDepositInf = findViewById(R.id.tvDepositInf)

        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, year: Int, monthofYear: Int, dayofMonth: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthofYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayofMonth)
            btnCalendar!!.text = "$monthofYear/$dayofMonth/$year"
            datePicker.minDate = System.currentTimeMillis() - 1000
        }

        type = intent.getStringExtra("type")
        if (type == "demand"){
            btnCalendar!!.isEnabled = false
        } else {
            btnCalendar!!.setOnClickListener {v ->
                DatePickerDialog(this@DepositSubmitActivity, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        btnSubmitDeposit!!.setOnClickListener {

            if (type == "time"){
                val now = Calendar.getInstance()
                if ("${now.get(Calendar.MONTH)}/${now.get(Calendar.DAY_OF_MONTH)}/${now.get(Calendar.YEAR)}" == btnCalendar!!.text){
                    if (etAmount!!.toString().toDouble() != 0.0){

                    } else {
                        tvDepositInf!!.text = "Amount should be larger than 0"
                    }
                } else {
                    tvDepositInf!!.text = "Please choose the expired date"
                }
            } else {

            }
        }
    }
}
