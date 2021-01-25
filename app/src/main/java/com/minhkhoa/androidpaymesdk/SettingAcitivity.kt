package com.minhkhoa.androidpaymesdk;

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingAcitivity : AppCompatActivity(), LifecycleObserver {
    private val REQUEST_OVERLAY_PERMISSION = 101
    lateinit var wm: WindowManager
    lateinit var containerButtonShowLog: LinearLayout
    lateinit var buttonShowLog: FloatingActionButton
    lateinit var paymePref: SharedPreferences

    lateinit var inputToken: EditText
    lateinit var inputSecretKey: EditText
    lateinit var inputPublicKey: EditText
    lateinit var buttonSave: Button
    lateinit var checkboxShowLog: CheckBox


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_acitivity)

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        paymePref = getSharedPreferences("PaymePref", MODE_PRIVATE)

        inputToken = findViewById(R.id.inputToken)
        inputSecretKey = findViewById(R.id.inputSecretKey)
        inputPublicKey = findViewById(R.id.inputPublicKey)
        buttonSave = findViewById(R.id.buttonSave)
        checkboxShowLog = findViewById(R.id.checkboxShowLog)

        inputToken.setText(MainActivity.AppToken)
        inputSecretKey.setText(MainActivity.AppSecretKey)
        inputPublicKey.setText(MainActivity.PublicKey)

        containerButtonShowLog = LayoutInflater.from(this).inflate(R.layout.button_overlay, null) as LinearLayout
        buttonShowLog = containerButtonShowLog.findViewById(R.id.buttonShowLog)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        buttonSave.setOnClickListener {
            val token = inputToken.text.toString()
            val secretKey = inputSecretKey.text.toString()
            val publicKey = inputPublicKey.text.toString()
            if(token.length > 0 && secretKey.length > 0 && publicKey.length > 0){
                finish()
            }
        }

        buttonShowLog.setOnClickListener{
            val process: Process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream)
            )

            var log = StringBuilder()
            var line: String? = ""
            while (bufferedReader.readLine().also { line = it } != null) {
                log.append(line)
            }
            Log.d("Show log", log.toString())

        }
        checkboxShowLog.isChecked = MainActivity.showLog
        checkboxShowLog.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b -> //Code khi trạng thái check thay đổi
            paymePref.edit().putBoolean(ON_LOG, b).commit()
            MainActivity.showLog= b

        })

    }

    private fun initWindowLayer() {
        wm = windowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 0),
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.START
        wm.addView(containerButtonShowLog, params)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}