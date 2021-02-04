package com.minhkhoa.androidpaymesdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LogActivity : AppCompatActivity() {
    lateinit var recycleviewLog: RecyclerView
    private val broadcast: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        registerReceiver(broadcast, IntentFilter("abcdef"))

        recycleviewLog = findViewById(R.id.recycleviewLog)
        val layoutManager = LinearLayoutManager(applicationContext)
        recycleviewLog.layoutManager = layoutManager
        recycleviewLog.itemAnimator = DefaultItemAnimator()
        var adapter = AdapterRecycleviewLog(WindowService.dataList)
        recycleviewLog.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcast)
    }
}