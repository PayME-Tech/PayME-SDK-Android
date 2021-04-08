package vn.payme.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.zxing.client.android.Intents
import kotlinx.android.synthetic.main.webview_activity.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.api.CryptoAES
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.credit.CameraTakeProfileCreditActivity
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.InfoPayment
import vn.payme.sdk.model.JsObject
import java.net.URLEncoder


internal class PaymeWaletActivity : AppCompatActivity() {
    lateinit private var loading: ConstraintLayout
    lateinit private var loadingProgressBar: ProgressBar
    private lateinit var cameraManager: CameraManager
    private lateinit var myWebView: WebView
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    lateinit var containerErrorNetwork: ConstraintLayout
    private var checkTimeoutLoadWebView = false
    private val REQUEST_CODE_TAKE_PICKTURE = 321

    companion object {
        var image: String = ""
    }

    private fun backScreen(): Unit {
        runOnUiThread {
            finish()
        }
    }
    fun takeImage(): Unit {
        val intent = Intent(this, CameraTakeProfileCreditActivity::class.java)
        this?.startActivity(intent)
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

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }


    override fun onCreate(savedInstanceState: Bundle?) {


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
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        loading = findViewById(R.id.loading)

        buttonBack = findViewById(R.id.buttonBack)
        buttonNext = findViewById(R.id.buttonNext)
        containerErrorNetwork = findViewById(R.id.containerErrorNetwork)

        myWebView.clearCache(true);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearSslPreferences();
        loadingProgressBar.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(Color.parseColor(PayME.colorApp.startColor), PorterDuff.Mode.SRC_ATOP)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        buttonBack?.setOnClickListener {
            finish()
        }
        buttonNext?.setOnClickListener {
            containerErrorNetwork.visibility = View.GONE
            loading?.visibility = View.VISIBLE
            myWebView.reload()

        }

        buttonBack?.background = PayME.colorApp.backgroundColorRadiusAlpha

        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                if (errorCode == -2) {
                    loading.visibility = View.GONE
                    containerErrorNetwork?.visibility = View.VISIBLE
                }
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (loading?.visibility != View.GONE) {
                    loading?.visibility = View.GONE
                }
                checkTimeoutLoadWebView = true


            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Thread {
                    try {
                        Thread.sleep(30000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    if (!checkTimeoutLoadWebView) {
                        runOnUiThread {
                            loading.visibility = View.GONE
                            containerErrorNetwork?.visibility = View.VISIBLE
                        }
                    }
                }.start()
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

        val jsObject: JsObject =
            JsObject(
                this,
                back = { backScreen() },
                takeImage = { takeImage() },
                this.supportFragmentManager,
                cameraManager
            )
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        var action: String = PayME.action.toString()
        val showLog = if (PayME.showLog) 1 else 0

        var data: JSONObject = JSONObject(
            """{
                      connectToken:  '${PayME.connectToken}',
                      appToken: '${PayME.appToken}',
                      publicKey: '${PayME.publicKey}',
                      privateKey: '${PayME.appPrivateKey}',
                      xApi: '${PayME.appID}',
                      env: '${PayME.env.toString()}',
                      showLog: '${showLog}',
                      clientId: '${PayME.clientId}',
                      amount:${PayME.amount},
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}'],
                      dataInit:${PayME.dataInit?.toString()},
                      partner : {
                        type:'ANDROID'
                      },
                      actions:{
                        type:${action},
                        serviceCode:${PayME.service?.code},
                        amount:${PayME.amount}
                      }
                    }"""
        )

        val cryptoAES = CryptoAES()
        val xAPIData = cryptoAES.encrytAESDataWebview("LkaWasflkjfqr2g3", data.toString())
        val encode: String = URLEncoder.encode(xAPIData, "utf-8")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)
        if (PayME.env == Env.DEV) {
            myWebView.loadUrl("https://sbx-sdk2.payme.com.vn/active/${encode}")
        } else if (PayME.env == Env.SANDBOX) {
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")

        } else {
            myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")
        }
        if (!isNetworkConnected()) {
            containerErrorNetwork?.visibility = View.VISIBLE
        }


    }

    fun checkScanQr(contents: String) {
        val paymentApi = PaymentApi()
        loading?.visibility = View.VISIBLE
        paymentApi.postCheckDataQr(contents,
            onSuccess = { jsonObject ->
                loading?.visibility = View.GONE
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Detect = Payment.optJSONObject("Detect")
                val action = Detect.optString("action")
                val message = Detect.optString("message")
                val note = Detect.optString("note")
                val amount = Detect.optInt("amount")
                val orderId = Detect.optString("orderId")
                val storeId = Detect.optLong("storeId")
                val succeeded = Detect.optBoolean("succeeded")
                val type = Detect.optString("type")

                if (!succeeded) {
                    loading?.visibility = View.GONE
                    var popup: PayMEQRCodePopup = PayMEQRCodePopup()
                    popup.show(this.supportFragmentManager, "ModalBottomSheet")
                } else {
                    val infoPayment =
                        InfoPayment(action, amount, note, orderId, storeId, type, PayME.extraData)

                    PayME.pay(
                        this.supportFragmentManager, infoPayment, true, null, null, null
                    )
                }
            },
            onError = { jsonObject, code, message ->
                loading?.visibility = View.GONE
                var popup: PayMEQRCodePopup = PayMEQRCodePopup()
                popup.show(this.supportFragmentManager, "ModalBottomSheet")
            }
        )
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
        if (myEven.type === TypeCallBack.onExpired) {
            finish()
        }
        if (myEven.type === TypeCallBack.onTakeImageResult) {

                if (myEven.value != null) {
                    image = myEven.value.toString()
                }
                val injectedJS = "       const script = document.createElement('script');\n" +
                        "          script.type = 'text/javascript';\n" +
                        "          script.async = true;\n" +
                        "          script.text = 'nativeAndroidSendBase64Image()';\n" +
                        "          document.body.appendChild(script);\n" +
                        "          true; // note: this is required, or you'll sometimes get silent failures\n"
                myWebView.evaluateJavascript("(function() {\n" + injectedJS + ";\n})();", null)
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
