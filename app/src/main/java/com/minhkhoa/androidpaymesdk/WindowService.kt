package com.minhkhoa.androidpaymesdk

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.*
import com.localazy.quicknote.windows.registerDraggableTouchListener
import java.io.BufferedReader
import java.io.InputStreamReader


class WindowService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var containerButtonShowLog: RelativeLayout
    private lateinit var buttonShowLog: Button

    var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        20, 20,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
                or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 0),
        PixelFormat.TRANSLUCENT
    )
//    params.gravity = Gravity.BOTTOM or Gravity.START

    companion object {
        var dataList = arrayListOf<String>()
        var showLog = false
    }

    private fun getCurrentDisplayMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm
    }


    private fun calculateSizeAndPosition(
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics()
        // We have to set gravity for which the calculated position is relative.
        params.gravity = Gravity.BOTTOM or Gravity.LEFT
        params.width = (widthInDp * dm.density).toInt()
        params.height = (heightInDp * dm.density).toInt()
    }

    private fun setPosition(x: Int, y: Int) {
        params.x = x
        params.y = y
        update()
    }


    private fun update() {
        try {
            windowManager.updateViewLayout(containerButtonShowLog, params)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        initView()
        super.onCreate()
    }

    private fun initView() {

        containerButtonShowLog = LayoutInflater.from(applicationContext)
            .inflate(R.layout.button_overlay, null) as RelativeLayout
        buttonShowLog = containerButtonShowLog.findViewById(R.id.buttonShowLog)

        initWindowLayer()

        buttonShowLog.registerDraggableTouchListener(
            initialPosition = { Point(params.x, params.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )
        buttonShowLog.setOnClickListener {
            if (showLog) {
                showLog = false
                sendBroadcast(Intent("abcdef"))
            } else {
                showLog = true
                dataList.clear()
                val process: Process = Runtime.getRuntime().exec("logcat -d")
                val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream)
                )

                var line: String? = ""
                while (bufferedReader.readLine().also { line = it } != null) {
                    dataList.add(line!!)
                }
                dataList.reverse()

                startActivity(Intent(this, LogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }

        }


    }

    private fun initWindowLayer() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        calculateSizeAndPosition(50, 50)
        windowManager.addView(containerButtonShowLog, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        sendBroadcast(Intent("abcdef"))
        windowManager.removeViewImmediate(containerButtonShowLog)
    }
}