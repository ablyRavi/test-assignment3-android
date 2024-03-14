package com.example.alarmmanagerdemo

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.alarmmanagerdemo.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmAdapter
    private var timeList: ArrayList<AlarmModel> = ArrayList()
    private lateinit var shared: PrefrenceShared
    var RQS_1 = 1
    private var title:String = ""
    private var body:String = ""
    lateinit var alarmManager: AlarmManager
    lateinit var cancelPendingIntent:PendingIntent

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        shared = PrefrenceShared(this@MainActivity)
        getSharedValue()
        initView()

        val intentFilter = IntentFilter()
        intentFilter.addAction("alarmAwake")
        registerReceiver(broadcastReceiver, intentFilter, null, null, Context.RECEIVER_NOT_EXPORTED)

        if (intent.getStringExtra("title") !=null && intent.getStringExtra("body") !=null) {
            title = intent.getStringExtra("title")!!
            body = intent.getStringExtra("body")!!
            binding.edtValue.setText(title)
            binding.edtNameValue.setText(body)
            Log.d("time", "Inside Oncreate title is -> $title and Body is -> $body")
        }
        if (intent.getStringExtra("action") !=null) {
            val status = intent.getStringExtra("action")
            if (status == "cancel") {
//                val intent = Intent(this,MyService::class.java)
               cancelAlarm()
//                stopService(intent)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MainScope().launch {
                    Log.d("time", "Calling Create Notification from Permissions")
                    ManageNotification(this@MainActivity).createNotificationChannel(title,body)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initView() {
        initializeClickListener()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeClickListener() {
        binding.apply {

            val cal = Calendar.getInstance()
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                    cal.set(Calendar.SECOND, 0)
//                    cal.set(Calendar.MILLISECOND, 0)
                    Log.d("time", "Time is ==> ${cal.time}")
                    Log.d(
                        "time",
                        "Format Time is ==> ${
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
                        }"
                    )
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)

                    val date = cal.time.toString()

                    val model = AlarmModel(time,false, date,binding.edtValue.text.toString().trim(),binding.edtNameValue.text.toString().trim())
                    timeList.distinctBy { it.time }
                    timeList.add(model)

                    val sortedList = timeList.sortedWith(compareBy { it.time })
                    timeList.clear()
                    timeList.addAll(sortedList)

                    // Save List to Shared...
                    val response = Gson().toJson(timeList)
                    shared.setString(Constants.TIME_LIST, response)
//                timeList = sortedList as ArrayList<AlarmModel>
                    Log.d(
                        "time",
                        "After Pick Time, Saving List to shared is ==> ${shared.getString(Constants.TIME_LIST)}"
                    )
                    adapter.notifyDataSetChanged()

//                    val milliseconds = convertTimeIntoMillis(model.time)
//                    // Register the BroadcastReceiver
//                    val filter = IntentFilter("alarmAwake") // Replace "your_action_here" with your actual action
//                    registerReceiver(broadcastReceiver, filter,null,null,Context.RECEIVER_EXPORTED)
//                    setAlarm(milliseconds)

                    binding.edtValue.setText("")
                    binding.edtNameValue.setText("")
                }
            tvPickTime.setOnClickListener {

                if (binding.edtValue.text.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please enter value!", Toast.LENGTH_SHORT).show()
                } else if (binding.edtNameValue.text.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please enter Name!", Toast.LENGTH_SHORT).show()
                } else {
                    TimePickerDialog(
                        this@MainActivity,
                        timeSetListener,
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                }

            }

            btnSubmit.setOnClickListener {
                if (adapter.timeList.isNotEmpty()) {
                    adapter.timeList.forEach {
                        if (!it.alarmStatus) {
                            it.alarmStatus = true
                            val millisSecond = convertTimeIntoMillis(it.time)
                            if (millisSecond > System.currentTimeMillis()) {
                                setAlarm(millisSecond,it)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Time List is empty, Please select time for Alarm", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeRecyclerView() {
        adapter = AlarmAdapter(this@MainActivity, timeList)
        binding.recTimeList.adapter = adapter
        binding.recTimeList.setHasFixedSize(true)
    }

    private fun getSharedValue() {
        if (shared.getString(Constants.TIME_LIST).isNotEmpty()) {
            val response = shared.getString(Constants.TIME_LIST)
            val type = object : TypeToken<ArrayList<AlarmModel>>() {}.type
            val list: ArrayList<AlarmModel> = Gson().fromJson(response, type)
            Log.d("time", "After pick time, List is ==> ${list}")
            if (list.isNotEmpty()) {
                timeList.clear()
                timeList.addAll(list)
            }
        }
        initializeRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAlarm(targetCal: Long, alarmModel: AlarmModel) {

//        // Register the BroadcastReceiver
//        val filter = IntentFilter("alarmAwake") // Replace "your_action_here" with your actual action
//        registerReceiver(broadcastReceiver, filter,null,null,Context.RECEIVER_EXPORTED) // Register receiver multi times to distinguish from each other...

//    private fun setAlarm(targetCal: Calendar) {
//        Log.d("time", "Alarm is set@ ${targetCal}")
//        val intent = Intent(this, MyAlarmReciever::class.java)
//        intent.putExtra("title", binding.edtValue.text.toString().trim())
//        intent.putExtra("body", binding.edtNameValue.text.toString().trim())
//        val pendingIntent = PendingIntent.getBroadcast(
//            this,
//            RQS_1,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        RQS_1++
//        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//        alarmManager[AlarmManager.RTC_WAKEUP, targetCal] = pendingIntent

//        val intent = Intent(this, MyService::class.java)
//        intent.putExtra("title",alarmModel.title)
//        intent.putExtra("body",alarmModel.body)
//        intent.putExtra("time",targetCal)
//        startService(intent)


        Log.d("time", "Alarm is set@ ${targetCal}")
        val intent = Intent(this, MyAlarmReciever::class.java)
        intent.putExtra("title", alarmModel.title)
        intent.putExtra("body", alarmModel.body)
        val interval = 1000 * 10 * 1/1000
        cancelPendingIntent = PendingIntent.getBroadcast(this, RQS_1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        RQS_1++
//        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal, pendingIntent)

//        if (alarmManager.canScheduleExactAlarms()) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCal, pendingIntent)
//        } else {
//            alarmManager[AlarmManager.RTC_WAKEUP, targetCal] = pendingIntent
//        }
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,targetCal,cancelPendingIntent)
//        alarmManager[AlarmManager.RTC_WAKEUP, targetCal] = cancelPendingIntent
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,targetCal,interval.toLong(),
//            cancelPendingIntent)

//        Handler(Looper.getMainLooper()).postDelayed({
//            cancelAlarm()
//        },interval.toLong())
//        alarmManager.canScheduleExactAlarms()
    }

    fun cancelAlarm() {
        if (::alarmManager.isInitialized) {
            val intent = Intent(this, MyService::class.java)
            cancelPendingIntent = PendingIntent.getBroadcast(this, RQS_1, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(cancelPendingIntent)
            stopService(intent)
        } else {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, MyService::class.java)
            cancelPendingIntent = PendingIntent.getBroadcast(this, RQS_1, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(cancelPendingIntent)
            stopService(intent)
        }

    }

    fun displayAlarmNotification(title:String,body:String) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    MainScope().launch {
                        Log.d("time", "Calling Create Notification from R")
                    ManageNotification(this@MainActivity).createNotificationChannel(title,body)
                    }
                } else {
                    this.requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
            } else {
                MainScope().launch {
                    Log.d("time", "Calling Create Notification from below R")
                ManageNotification(this@MainActivity).createNotificationChannel(title,body)
                }
            }
        }

    private var broadcastReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (intent.action == "alarmAwake") {
                    title = intent.getStringExtra("title")!!
                    body = intent.getStringExtra("body")!!
                    if (title.isNotEmpty() && body.isNotEmpty()) {
                        displayAlarmNotification(title,body)
                    }
                }
            }
        }
    }

    private fun convertTimeIntoMillis(time:String) : Long {

        // Get today's date
        val calendar = Calendar.getInstance()

        // Parse the time string
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val cTime = timeFormat.parse(time)

        // Set the time component of the calendar
        calendar.apply {
            cTime?.let {
                set(Calendar.HOUR_OF_DAY, it.hours)
                set(Calendar.MINUTE, it.minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val milliseconds = calendar.timeInMillis
        println(milliseconds)
        return milliseconds
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra("title") !=null && intent.getStringExtra("body") !=null) {
            title = intent.getStringExtra("title")!!
            body = intent.getStringExtra("body")!!
            binding.edtValue.setText(title)
            binding.edtNameValue.setText(body)
            Log.d("time", "Inside onNewIntent title is -> $title and Body is -> $body")
        }

        if (intent?.getStringExtra("action") !=null) {
            val status = intent.getStringExtra("action")
            if (status == "cancel") {
//                val intent = Intent(this,MyService::class.java)
                cancelAlarm()
//                stopService(intent)
            }
        }
    }

}