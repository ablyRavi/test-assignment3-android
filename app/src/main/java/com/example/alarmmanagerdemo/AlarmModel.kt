package com.example.alarmmanagerdemo

data class AlarmModel(
    var time:String,
    var status:Boolean,
    var date:String,
    var title:String,
    var body:String,
    var alarmStatus:Boolean=false
)
