package com.minhkhoa.androidpaymesdk

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingAcitivity : AppCompatActivity(), LifecycleObserver {
    private val REQUEST_OVERLAY_PERMISSION = 101

    lateinit var paymePref: SharedPreferences

    lateinit var inputToken: EditText
    lateinit var inputSecretKey: EditText
    lateinit var inputPublicKey: EditText
    lateinit var inputPrivateKey: EditText
    lateinit var buttonSave: Button
    lateinit var checkboxShowLog: CheckBox
    lateinit var buttonRestore: Button

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
//        Toast.makeText(this, "hihi", Toast.LENGTH_SHORT).show()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        paymePref.edit().putBoolean(ON_LOG, false).commit()
        stopService(Intent(this, WindowService::class.java))
    }

    override fun onStart() {
        super.onStart()
        checkboxShowLog.isChecked = paymePref.getBoolean(ON_LOG, false)
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
        buttonRestore = findViewById(R.id.buttonRestore)
        inputPrivateKey = findViewById(R.id.inputPrivateKey)

        inputSecretKey.setText(MainActivity.SecretKey)
        inputPrivateKey.setText(MainActivity.PrivateKey)
        inputPublicKey.setText(MainActivity.PublicKey)
        inputToken.setText(MainActivity.AppToken)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        checkboxShowLog.isChecked = paymePref.getBoolean(ON_LOG, false)

        buttonSave.setOnClickListener {
            val token = inputToken.text.toString()
            val secretKey = inputSecretKey.text.toString()
            val publicKey = inputPublicKey.text.toString()
            val privateKey = inputPrivateKey.text.toString()
            if(token.length > 0 && secretKey.length > 0 && publicKey.length > 0 && privateKey.length > 0){
                paymePref.edit().putString(APP_TOKEN, token).commit()
                paymePref.edit().putString(SECRET_KEY, secretKey).commit()
                paymePref.edit().putString(PUBLIC_KEY, publicKey).commit()
                paymePref.edit().putString(PRIVATE_KEY, privateKey).commit()

                MainActivity.AppToken = token
                MainActivity.PublicKey = publicKey
                MainActivity.PrivateKey = privateKey
                MainActivity.SecretKey = secretKey
                finish()
            }
        }

        checkboxShowLog.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b -> //Code khi trạng thái check thay đổi

            MainActivity.showLog =b
            paymePref.edit().putBoolean(ON_LOG, b).commit()
        })

        buttonRestore.setOnClickListener {

            paymePref.edit().putString(APP_TOKEN, APP_TOKEN_DEFAULT).commit()
            paymePref.edit().putString(SECRET_KEY, SECRET_KEY_DEFAULT).commit()
            paymePref.edit().putString(PUBLIC_KEY, PUBLIC_KEY_DEFAULT).commit()
            paymePref.edit().putString(PRIVATE_KEY, PRIVATE_KEY_DEFAULT).commit()

            MainActivity.AppToken = APP_TOKEN_DEFAULT
            MainActivity.PublicKey = PUBLIC_KEY_DEFAULT
            MainActivity.PrivateKey = PRIVATE_KEY_DEFAULT
            MainActivity.SecretKey = SECRET_KEY_DEFAULT

            inputSecretKey.setText(MainActivity.SecretKey)
            inputPrivateKey.setText(MainActivity.PrivateKey)
            inputPublicKey.setText(MainActivity.PublicKey)
            inputToken.setText(MainActivity.AppToken)
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}