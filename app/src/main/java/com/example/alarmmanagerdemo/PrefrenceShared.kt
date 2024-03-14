package com.example.alarmmanagerdemo

import android.app.Activity
import android.content.Context

class PrefrenceShared(activity: Activity) {

    private var activity:Activity
    private val SHARED_PREFRENCE = "ALARM_SHARED_PREFRENCE"
    val sharedPref = activity.getSharedPreferences(SHARED_PREFRENCE, Context.MODE_PRIVATE)


    init {
        this.activity = activity
    }

    fun setString(key:String,value:String) {

        val editor = sharedPref.edit()
        editor.putString(key,value)
        editor.apply()
    }

    fun getString(key: String) : String {
        return sharedPref.getString(key,"")!!
    }
}