package com.example.coinz

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

// Coin adapter
class CoinAdapter(private var context: Context, private var coins: ArrayList<Coin>, private var type: String): RecyclerView.Adapter<CoinAdapter.ViewHolder>(){
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.coin_list, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = coins.size

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.itemView.tag = coins[i]
        when {
            // Set the image with the specific coin's type
            coins[i].type == "PENY" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_peny)}
            coins[i].type == "QUID" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_quid)}
            coins[i].type == "SHIL" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_shil)}
            coins[i].type == "DOLR" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_dolr)}
        }

        viewHolder.tvCoinValue.text = String.format("%.4f", coins[i].value)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var ivCoin: ImageView = itemView.findViewById(R.id.ivCoin)
        var tvCoinValue: TextView = itemView.findViewById(R.id.tvCoinValue)
        var cvCoin: CardView = itemView.findViewById(R.id.cvCoin)

        init {
            itemView.setOnClickListener {
                when {
                    type != "balance" ->{
                        // When the coin's cardView is clicked, the background will change
                        val point = itemView.tag as Coin
                        val colorStateList = ContextCompat.getColorStateList(context, R.color.colorPrimaryLight)
                        if (cvCoin.cardBackgroundColor == colorStateList){
                            cvCoin.setCardBackgroundColor(getColor(context, R.color.secondaryText))
                            point.checked = true
                        } else {
                            cvCoin.setCardBackgroundColor(getColor(context, R.color.colorPrimaryLight))
                            point.checked = false
                        }
                    }
                }
            }
        }
    }
}