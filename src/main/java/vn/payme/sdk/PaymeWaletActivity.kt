package vn.payme.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.model.JsObject
import vn.payme.sdk.model.MyEven


internal class PaymeWaletActivity : AppCompatActivity() {
   private fun backScreen(): Unit {

        runOnUiThread {
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val html: String = "<!DOCTYPE html><html><body>\n" +
                "      <button onclick=\"onClick()\">Click me</button>\n" +
                "      <script>\n" +
                "      function onClick() {\n" +
                "       window.injectedObject.onSuccess('{a:1,b:2}')" +
                "      }\n" +
                "      </script>\n" +
                "      </body></html>\n"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        val jsObject: JsObject = JsObject(back = { backScreen() })
        myWebView.addJavascriptInterface(jsObject, "injectedObject")
//        myWebView.loadUrl("https://sbx-sdk.payme.com.vn/?deviceId=${PayME.deviceId}")



        myWebView.loadData(html, "text/html", "UTF-8")




    }


}
