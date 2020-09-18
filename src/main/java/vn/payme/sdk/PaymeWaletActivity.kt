package vn.payme.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class PaymeWaletActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        val myWebView: WebView = findViewById(R.id.webview)
    myWebView.settings.javaScriptEnabled = true
    myWebView.loadUrl("https://sdk.payme.com.vn/?deviceId=${PaymeModule.deviceId}&transID=${PaymeModule.transID}&userID=${PaymeModule.userID}&tokenLink=${PaymeModule.tokenLink}&action=${PaymeModule.action}&money=${PaymeModule.money}&userInfor=${PaymeModule?.info?.toJson()}")

    }

}
