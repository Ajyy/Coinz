package com.example.coinz

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

// Record adapter
class RecordAdapter(private val records: ArrayList<Record>): RecyclerView.Adapter<RecordAdapter.ViewHolder>(){
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecordAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.record_list, viewGroup, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = records.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecordAdapter.ViewHolder, i: Int) {
        viewHolder.itemView.tag = records[i]
        if (records[i].type == "time"){
            viewHolder.tvRecordTitle.text = records[i].begin+" to "+records[i].end+": "+records[i].type
            viewHolder.tvRecordDetail.text = "Deposit: "+String.format("%.4f", records[i].deposit)+" "+records[i].coinType+" Finish: "+records[i].finish
        } else if (records[i].type == "demand"){
            viewHolder.tvRecordTitle.text = records[i].begin+": "+records[i].type
            viewHolder.tvRecordDetail.text = "Deposit: "+String.format("%.4f", records[i].deposit)+" "+records[i].coinType
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvRecordTitle: TextView = itemView.findViewById(R.id.tvRecordTitle)
        var tvRecordDetail: TextView = itemView.findViewById(R.id.tvRecordDetail)
    }
}