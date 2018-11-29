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

        if (context.javaClass.simpleName == "HistoryActivity"){
            viewHolder.tvRecordDetail.text = "Deposit: "+records[i].deposit+" "+records[i].coinType
            if (records[i].type == "time"){
                viewHolder.tvRecordTitle.text = records[i].begin+" to "+records[i].end+": "+records[i].type
            } else if (records[i].type == "demand"){
                viewHolder.tvRecordTitle.text = records[i].begin+": "+records[i].type
            }
        }else if (context.javaClass.simpleName == "TimeDepositActivity"){

        }

    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvRecordTitle: TextView = itemView.findViewById(R.id.tvRecordTitle)
        var tvRecordDetail: TextView = itemView.findViewById(R.id.tvRecordDetail)

        init {
            itemView.setOnClickListener {

            }
        }
    }
}