package vn.payme.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.JsObject
import java.net.URLEncoder
import android.hardware.camera2.CameraManager
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.zxing.client.android.Intents
import vn.payme.sdk.api.PaymentApi
import java.lang.Exception


internal class PaymeWaletActivity : AppCompatActivity() {
    val html: String = "<!DOCTYPE html><html><body>\n" +
            "      <button onclick=\"onClick()\">Click me</button>\n" +
            "      <script>\n" +
            "      function onClick() {\n" +
            "       window.messageHandlers.onFlash(true)" +
            "      }\n" +
            "      </script>\n" +
            "      </body></html>\n"

    private var cameraPermission: PermissionRequest? = null
    private var lottie: LottieAnimationView? = null
    private lateinit var cameraManager: CameraManager
    private lateinit var myWebView: WebView


    private fun backScreen(): Unit {

        runOnUiThread {
            onBackPressed()
        }
    }


    fun convertPixelsToDp(px: Float): Float {
        return if (PayME.context != null) {
            val resources = PayME.context.resources
            val metrics = resources.displayMetrics
            px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
//        checkCamera()
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager


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

        myWebView = findViewById(R.id.webview)
        lottie = findViewById(R.id.loadingWeb)
        myWebView.clearCache(true);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearSslPreferences();
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)


        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                lottie?.visibility = View.GONE
            }
        })
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );


        val webSettings: WebSettings = myWebView.getSettings()
        webSettings.javaScriptEnabled = true
        webSettings.setAppCacheEnabled(true)
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.setSupportMultipleWindows(true)
        webSettings.setGeolocationEnabled(true)
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.databaseEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.setGeolocationEnabled(true)
        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        myWebView.setWebChromeClient(object : WebChromeClient() {

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(
                    "LOG WEB", consoleMessage?.message() + " -- From line "
                            + consoleMessage?.lineNumber() + " of "
                            + consoleMessage?.sourceId()
                );
                return super.onConsoleMessage(consoleMessage)
            }
        })
        val jsObject: JsObject =
            JsObject(this, back = { backScreen() }, this.supportFragmentManager, cameraManager)
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        println("VAO  DDDDDDDDDDDDD")
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
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}'],
                      partner : {
                        type:'ANDROID',
                        paddingTop:${convertPixelsToDp(statusBarHeight.toFloat())}
                      },
                      actions:{
                        type:${action},
                        amount:${PayME.amount}
                      }
                    }"""
        )

        val encode: String = URLEncoder.encode(data.toString(), "utf-8")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)
        println("https://sbx-sdk.payme.com.vn/active/${encode}")
        if (PayME.env === Env.SANDBOX) {
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")
//            myWebView.loadData(html, "text/html", "UTF-8");


        } else {
            myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5 && resultCode == Activity.RESULT_OK && data != null) {
            val contents = data.getStringExtra(Intents.Scan.RESULT)
            val paymentApi = PaymentApi()
            paymentApi.postCheckDataQr(contents.toString(),
                onSuccess = { jsonObject ->
                    val amount = jsonObject?.getInt("amount")
                    val content = jsonObject?.getString("content")
                    val orderId = jsonObject?.getString("orderId")
                    val payme = PayME(
                        PayME.context,
                        PayME.appToken,
                        PayME.publicKey,
                        PayME.connectToken,
                        PayME.appPrivateKey,
                        PayME.configColor!!,
                        PayME.env!!
                    )
                    payme.pay(this.supportFragmentManager, amount, content, orderId, "", onSuccess = {

                    }, onError = {


                    },
                        onClose = {

                        }
                    )


                },
                onError = { jsonObject, code, message ->
                     var popup: PayMEQRCodePopup = PayMEQRCodePopup()
                    popup.show(this.supportFragmentManager, "ModalBottomSheet")
                }
            )
//            Toast.makeText(this, contents, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5 && resultCode == Activity.RESULT_OK && data != null) {
            val contents = data.getStringExtra(Intents.Scan.RESULT)
            Toast.makeText(this, contents, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: Exception) {
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {

        if (this.myWebView.canGoBack()) {
            this.myWebView.goBack()
        } else {
            finish()
        }
        return true
    }


}
