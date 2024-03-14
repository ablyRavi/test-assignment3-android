package com.example.alarmmanagerdemo

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.alarmmanagerdemo.databinding.AlarmItemViewBinding
import com.google.gson.Gson

class AlarmAdapter(private val context:Context,private val list:ArrayList<AlarmModel>) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

   var timeList = ArrayList<AlarmModel>()

    inner class AlarmViewHolder(v:AlarmItemViewBinding) :ViewHolder(v.root) {
        val binding : AlarmItemViewBinding = v
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = AlarmItemViewBinding.inflate(layoutInflater,parent,false)
        return AlarmViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {

        val model = list[position]
        holder.binding.apply {
            tvLabel.text = model.time

            if (model.status) {
                tvLabel.background = ContextCompat.getDrawable(context,R.drawable.tile_bg_selected)
            }

            tvLabel.setOnClickListener {
                if (model.status) {
                    it.background = ContextCompat.getDrawable(context,R.drawable.tile_bg)
                    model.status = false
                    timeList.remove(model)
                } else {
                    model.status = true
                    timeList.add(model)
                    it.background = ContextCompat.getDrawable(context,R.drawable.tile_bg_selected)

                }
                list[position].status = model.status
                val response = Gson().toJson(list)
                PrefrenceShared(context as Activity).setString(Constants.TIME_LIST,response)
                notifyDataSetChanged()
            }

        }
    }



}