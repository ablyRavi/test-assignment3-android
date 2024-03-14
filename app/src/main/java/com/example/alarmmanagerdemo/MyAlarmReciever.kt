package com.example.alarmmanagerdemo

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat


class MyAlarmReciever : BroadcastReceiver() {

    var v: Vibrator? = null
    var ct: Context? = null
    var title: String? = null
    var body: String? = null


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("time", "Inside Broadcast OnReceive")
//        ct = context
////        Toast.makeText(context, "OnReceive alarm test", Toast.LENGTH_SHORT).show()
//        v = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        v!!.vibrate(3000)

        try {
            title = intent?.getStringExtra("title")
            body =  intent?.getStringExtra("body")
            val bIntent = Intent(context,MyService::class.java)
            bIntent.setAction("alarmAwake")
            bIntent.putExtra("title",title)
            bIntent.putExtra("body",body)
//            context.sendBroadcast(bIntent)
//            Toast.makeText(context, title, Toast.LENGTH_LONG).show()
            Log.d("time", "Calling startService Service")
//            val  myService = MyService()
//            myService.startService(bIntent)
            context?.startService(bIntent)
        } catch (e: Exception) {
            Log.d("time", "Exception inside OnReceive => ${e.message}")
            e.printStackTrace()
        }
    }
}