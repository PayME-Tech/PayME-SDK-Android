package vn.payme.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import org.json.JSONObject
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.JsObject
import java.net.URLEncoder


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
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        var data: JSONObject = JSONObject(
            """{
                      connectToken:  '${PayME.connectToken}',
                      appToken: '${PayME.appToken}',
                      clientInfo: {
                        clientId: '${PayME.deviceId}',
                        platform: 'ANDROID',
                        appVersion: '${PayME.appVersion}', 
                        sdkVesion: '${PayME.sdkVerSion}', 
                        sdkType: 'native',
                        appPackageName: '${PayME.appPackageName}' 
                      },
                      partner: 'ANDROID',
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}']
                    }"""
        )
        val encode :String = URLEncoder.encode(data.toString(),"utf-8")
        if(PayME.env===Env.SANDBOX){
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")

        }else{
            myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")

        }

//        myWebView.loadData(html, "text/html", "UTF-8")


    }


}
