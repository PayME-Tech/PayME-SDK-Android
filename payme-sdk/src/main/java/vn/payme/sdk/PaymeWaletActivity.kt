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
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.JsObject
import java.net.URLEncoder


internal class PaymeWaletActivity : AppCompatActivity() {
    private var cameraPermission: PermissionRequest? = null
    private val CAMERA_PERMISSION_REQUEST = 1111

    private fun backScreen(): Unit {

        runOnUiThread {
            onBackPressed()
        }
    }

    private fun checkCamera() {
        val hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA)
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 113)

        }else{
            cameraPermission!!.grant(cameraPermission!!.resources)

        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPermission!!.grant(cameraPermission!!.resources)

//        if (requestCode == CAMERA_PERMISSION_REQUEST) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            } else {
//                cameraPermission!!.deny()
//            }
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        checkCamera()
        super.onCreate(savedInstanceState)

        WebView(applicationContext).clearCache(true)
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();


        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.DKGRAY
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.webview_activity)
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.clearCache(true);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearSslPreferences();
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)


        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {

                super.onPageFinished(view, url)
            }
        })

        val webSettings: WebSettings = myWebView.getSettings()
        webSettings.javaScriptEnabled = true
        webSettings.setAppCacheEnabled(true)
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.setSupportMultipleWindows(true)
        webSettings.setGeolocationEnabled(true)
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.setGeolocationEnabled(true)

        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK


//        setContentView(myWebView)
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
                cameraPermission  = request

                checkCamera()

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
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}'],
                      partner : {
                        type:'ANDROID',
                        paddingTop:${statusBarHeight}
                      },
                      actions:{
                        type:${action},
                        amount:${PayME.amount}
                      }
                    }"""
        )

        val encode: String = URLEncoder.encode(data.toString(), "utf-8")
//        cookieManager.setCookie("https://sbx-sdk.payme.com.vn/active/${encode}","$cookieKey=$cookieValue")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)
        println("https://sbx-sdk.payme.com.vn/active/${encode}")



        if (PayME.env === Env.SANDBOX) {
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")

        } else {
            myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")
        }


    }


}
