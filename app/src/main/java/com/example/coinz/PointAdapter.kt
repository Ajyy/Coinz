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

class PointAdapter(private var context: Context, private var points: ArrayList<Point>, private var type: String): RecyclerView.Adapter<PointAdapter.ViewHolder>(){
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.coin_list, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = points.size

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.itemView.tag = points[i]
        when {
            points[i].currency == "PENY" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_peny)}
            points[i].currency == "QUID" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_quid)}
            points[i].currency == "SHIL" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_shil)}
            points[i].currency == "DOLR" ->{viewHolder.ivCoin.setImageResource(R.drawable.ic_dolr)}
        }

        viewHolder.tvCoinValue.text = String.format("%.4f", points[i].value)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var ivCoin: ImageView = itemView.findViewById(R.id.ivCoin)
        var tvCoinValue: TextView = itemView.findViewById(R.id.tvCoinValue)
        var cvCoin: CardView = itemView.findViewById(R.id.cvCoin)

        init {
            itemView.setOnClickListener {
                when {
                    type != "balance" ->{
                        val point = itemView.tag as Point
                        val colorStateList = ContextCompat.getColorStateList(context, R.color.colorPrimaryLight)
                        if (cvCoin.cardBackgroundColor == colorStateList){
                            cvCoin.setCardBackgroundColor(getColor(context, R.color.secondaryText))
                            point.isChecked = true
                        } else {
                            cvCoin.setCardBackgroundColor(getColor(context, R.color.colorPrimaryLight))
                            point.isChecked = false
                        }
                    }
                }
            }
        }
    }
}