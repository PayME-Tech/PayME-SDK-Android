package vn.payme.sdk

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebMessage
import android.webkit.WebView
import vn.payme.sdk.model.JsObject


internal class PaymeWaletActivity : AppCompatActivity() {
    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        val html :String = "<!DOCTYPE html><html><body>\n" +
                "      <button onclick=\"onClick()\">Click me</button>\n" +
                "      <script>\n" +
                "      function onClick() {\n" +
                "       window.injectedObject.onError('ádasdasdd')" +
                "      }\n" +
                "      </script>\n" +
                "      </body></html>\n"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        val jsObject : JsObject = JsObject()

        myWebView.addJavascriptInterface(jsObject,"injectedObject")

//        myWebView.loadData(html,"text/html", "UTF-8")

        myWebView.loadUrl("https://sbx-sdk.payme.com.vn/?deviceId=${PayME.deviceId}")
//        val webMessage : WebMessage = WebMessage()

//        val webviewInterface : JsObject = JsObject()

//        myWebView.addJavascriptInterface(webviewInterface,"")
//        webviewInterface.onSuccess()




//        myWebView.createWebMessageChannel()

//        myWebView.postWebMessage()

    }



}
