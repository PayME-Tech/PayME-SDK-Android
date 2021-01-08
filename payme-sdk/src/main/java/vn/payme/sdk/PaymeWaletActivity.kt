package vn.payme.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.zxing.client.android.Intents
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.InfoPayment
import vn.payme.sdk.model.JsObject
import vn.payme.sdk.model.TypeCallBack
import java.net.URLEncoder


internal class PaymeWaletActivity : AppCompatActivity() {
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
        EventBus.getDefault().register(this)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager


        WebView(applicationContext).clearCache(true)
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        setContentView(R.layout.webview_activity)
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setBackgroundDrawable(PayME.colorApp.backgroundColor);

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
        var action: String = PayME.action.toString()
        var data: JSONObject = JSONObject(
            """{
                      connectToken:  '${PayME.connectToken}',
                      appToken: '${PayME.appToken}',
                      accessToken: '${PayME.accessToken}',
                      env: '${PayME.env.toString()}',
                      handShake: '${PayME.handShake}',
                      clientId: '${PayME.clientId}',
                      amount:${PayME.amount},
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}'],
                      partner : {
                        type:'ANDROID'
                      },
                      actions:{
                        type:${action},
                        amount:${PayME.amount}
                      }
                    }"""
        )

        val encode: String = URLEncoder.encode(data.toString(), "utf-8")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)
        if (PayME.env == Env.DEV) {
            println("https://sbx-sdk.payme.com.vn/active/${encode}")

            myWebView.loadUrl("https://sbx-sdk2.payme.com.vn/active/${encode}")
        } else if (PayME.env == Env.SANDBOX) {
            println("https://sbx-sdk.payme.com.vn/active/${encode}")
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")
        } else {
            myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")
        }


    }

    fun checkScanQr(contents: String) {
        val paymentApi = PaymentApi()
        lottie?.visibility = View.VISIBLE
        paymentApi.postCheckDataQr(contents,
            onSuccess = { jsonObject ->
                lottie?.visibility = View.GONE
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("OpenEWallet")
                val Detect = Payment.optJSONObject("Detect")
                val action = Detect.optString("Detect")
                val message = Detect.optString("message")
                val note = Detect.optString("message")
                val amount = Detect.optInt("amount")
                val orderId = Detect.optInt("amount")
                val storeId = Detect.optInt("amount")
                val succeeded = Detect.optBoolean("amount")
                val type = Detect.optString("type")

                if (!succeeded) {
                    lottie?.visibility = View.GONE
                    var popup: PayMEQRCodePopup = PayMEQRCodePopup()
                    popup.show(this.supportFragmentManager, "ModalBottomSheet")
                }
                val infoPayment = InfoPayment(action, amount, note, orderId, storeId, type)

                PayME.pay(this.supportFragmentManager, infoPayment,
                    onSuccess = {

                    }, onError = {


                    },
                    onClose = {

                    }
                )


            },
            onError = { jsonObject, code, message ->
                lottie?.visibility = View.GONE
                var popup: PayMEQRCodePopup = PayMEQRCodePopup()
                popup.show(this.supportFragmentManager, "ModalBottomSheet")
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5 && resultCode == Activity.RESULT_OK && data != null) {
            val contents = data.getStringExtra(Intents.Scan.RESULT)
            checkScanQr(contents.toString())
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy()


    }

    @Subscribe
    fun onText(myEven: MyEven) {
        if (myEven.type === TypeCallBack.onReload) {
            this.myWebView.reload()
        }
        if (myEven.type === TypeCallBack.onScan) {
            myEven.value?.let { checkScanQr(it) }
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
