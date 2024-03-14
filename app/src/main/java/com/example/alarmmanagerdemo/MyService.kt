package com.example.alarmmanagerdemo

import android.app.AlarmManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyService : Service() {
    var title = ""
    var body = ""
    lateinit var alarmManager: AlarmManager
    lateinit var mediaPlayer: MediaPlayer
    lateinit var job: Job
    lateinit var vibrator: Vibrator

    override fun onCreate() {
        super.onCreate()
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        Log.d("time", "Inside service, running onCreate")
    }

    lateinit var alert: Uri

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.getStringExtra("title") != null && intent.getStringExtra("body") != null) {
                title = intent.getStringExtra("title")!!
                body = intent.getStringExtra("body")!!
                Log.d(
                    "time",
                    "Inside service OnstartCommand, Title is => $title and Body is => $body "
                )
                // Start a coroutine
                job = GlobalScope.launch {
                    repeat(3) { // Repeat the calculation 5 times with a 2-second interval
                        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(400)
                        Log.d("time", "Vibratin is running")
                        delay(1000) // Delay for 2 seconds
                    }

                }
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer.create(baseContext, alert)
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.setVolume(100f, 100f)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener { mpp -> mpp.release() }
                }
                val bintent = Intent()
                bintent.action = "alarmAwake"
                bintent.putExtra("title", title)
                bintent.putExtra("body", body)
                Log.d("time", "Calling Send Broadcast...")
                sendBroadcast(bintent)
            }
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onDestroy() {
        super.onDestroy()
        Log.d("time", "Service Destroyed")
        alert = Uri.EMPTY
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop() // Stop MediaPlayer if it's playing
        }
        mediaPlayer.release() // Release MediaPlayer resources
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}