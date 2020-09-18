package com.payme.sdk.walet

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class PaymeWaletActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        val myWebView: WebView = findViewById(R.id.webview)
    myWebView.settings.javaScriptEnabled = true
    myWebView.loadUrl("https://sdk.payme.com.vn/?deviceId=${PaymeModul.deviceId}&transID=${PaymeModul.transID}&userID=${PaymeModul.userID}&tokenLink=${PaymeModul.tokenLink}&action=${PaymeModul.action}&money=${PaymeModul.money}&userInfor=${PaymeModul?.info?.toJson()}")

    }

}
