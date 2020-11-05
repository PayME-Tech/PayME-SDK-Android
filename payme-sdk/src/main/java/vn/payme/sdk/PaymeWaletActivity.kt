package vn.payme.sdk

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import vn.payme.sdk.model.Action
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.JsObject
import java.net.URLEncoder


internal class PaymeWaletActivity : AppCompatActivity() {

    private fun backScreen(): Unit {

        runOnUiThread {
            onBackPressed()
        }
    }

    private fun checkCamera() {
        val hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA)
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 113)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val html: String = "<!DOCTYPE html><html><body>\n" +
                "      <button onclick=\"onClick()\">Click me</button>\n" +
                "      <script>\n" +
                "      function onClick() {\n" +
                "       window.messageHandlers.onSuccess('{a:1,b:2}')" +
                "      }\n" +
                "      </script>\n" +
                "      </body></html>\n"
        super.onCreate(savedInstanceState)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.webview_activity)
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.mediaPlaybackRequiresUserGesture = false
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                myWebView.loadUrl(url)
                return true

            }
        })
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true);


        myWebView.setWebChromeClient(object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(
                    "LOG WEB", consoleMessage?.message() + " -- From line "
                            + consoleMessage?.lineNumber() + " of "
                            + consoleMessage?.sourceId()
                );
                return super.onConsoleMessage(consoleMessage)
            }

            // Grant permissions for cam
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest) {
                checkCamera()
                runOnUiThread() {
                    request.grant(request.getResources());


                }

            }

        })


        val jsObject: JsObject = JsObject(back = { backScreen() }, this.supportFragmentManager)
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        var action: String = PayME.action.toString()

        var data: JSONObject = JSONObject(
            """{
                      connectToken:  '${PayME.connectToken}',
                      appToken: '${PayME.appToken}',
                      clientInfo: {
                        clientId: '${PayME.clientInfo.deviceId}',
                        platform: 'ANDROID',
                        appVersion: '${PayME.clientInfo.appVersion}', 
                        sdkVesion: '${PayME.clientInfo.sdkVerSion}', 
                        sdkType: 'native',
                        appPackageName: '${PayME.clientInfo.appPackageName}'
                      },
                      partner: 'ANDROID',
                      action:'${action}',
                      amount:${PayME.amount},
                      partnerTop:${statusBarHeight},
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}']
                    }"""
        )
        val encode: String = URLEncoder.encode(data.toString(), "utf-8")
        if (PayME.action == Action.TEST) {
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/test")

        } else {
            if (PayME.env === Env.SANDBOX) {
//            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/result?success=true&trans_id=0792609928&amount=10000")
                myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")
//                myWebView.loadUrl("https://vi.webcamtests.com")

            } else {
                myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")

            }
        }


    }


}
