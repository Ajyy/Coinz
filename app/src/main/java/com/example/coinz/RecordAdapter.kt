package com.example.coinz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class RecordAdapter(private val context: Context, private val records: ArrayList<Record>): RecyclerView.Adapter<RecordAdapter.ViewHolder>(){
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecordAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.record_list, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = records.size

    override fun onBindViewHolder(viewHolder: RecordAdapter.ViewHolder, i: Int) {
        viewHolder.itemView.tag = records[i]

        viewHolder.tvRecordDetail.text = "Deposit: "+records[i].deposit
        viewHolder.tvRecordTime.text = records[i].begin+" to "+records[i].end+": "+records[i].type
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvRecordTime: TextView = itemView.findViewById(R.id.tvRecordTime)
        var tvRecordDetail: TextView = itemView.findViewById(R.id.tvRecordDetail)

        init {
            itemView.setOnClickListener {

            }
        }
    }
}