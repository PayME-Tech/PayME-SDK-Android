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
import android.widget.ImageView
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
import vn.payme.sdk.store.Store
import java.net.URLEncoder


internal class PaymeWaletActivity : AppCompatActivity() {
    lateinit private var loading: ConstraintLayout
    lateinit private var loadingProgressBar: ProgressBar
    private lateinit var cameraManager: CameraManager
    private lateinit var myWebView: WebView
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private lateinit var buttonClose: ImageView
    lateinit var containerErrorNetwork: ConstraintLayout
    lateinit var header: ConstraintLayout
    private var checkTimeoutLoadWebView = false
    private val REQUEST_CODE_TAKE_PICKTURE = 321
    var domain  = ""



    companion object {
        var image: String = ""
        var isVisible  = false
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
        isVisible  = true
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
        getWindow().setBackgroundDrawable(Store.config.colorApp.backgroundColor);


        myWebView = findViewById(R.id.webview)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        loading = findViewById(R.id.loading)

        buttonBack = findViewById(R.id.buttonBack)
        buttonNext = findViewById(R.id.buttonNext)
        header = findViewById(R.id.header)
        buttonClose = findViewById(R.id.buttonClose)
        containerErrorNetwork = findViewById(R.id.containerErrorNetwork)


        myWebView.clearCache(true);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearSslPreferences();
        loadingProgressBar.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )

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

        buttonBack?.background = Store.config.colorApp.backgroundColorRadiusAlpha

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
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        val jsObject: JsObject =
            JsObject(
                this,
                back = { backScreen() },
                showButtonClose={b->
                    showButtonClose(b)
                },
                takeImage = { takeImage() },
                this.supportFragmentManager,
                cameraManager
            )
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        var action: String = Store.paymentInfo.action.toString()
        val showLog = if (Store.config.showLog) 1 else 0
        val description = Store.paymentInfo.content
        var data: JSONObject = JSONObject(
            """{
                      connectToken:  '${Store.config.connectToken}',
                      appToken: '${Store.config.appToken}',
                      publicKey: '${Store.config.publicKey}',
                      privateKey: '${Store.config.appPrivateKey}',
                      xApi: '${Store.config.appID}',
                      env: '${Store.config.env.toString()}',
                      showLog: '${showLog}',
                      clientId: '${Store.config.clientId}',
                      amount:${Store.paymentInfo.amount},
                      configColor: ['${Store.config.configColor?.get(0)}', '${
                Store.config.configColor?.get(
                    1
                )
            }'],
                      dataInit:${Store.userInfo.dataInit?.toString()},
                      partner : {
                        type:'ANDROID'
                      },
                      actions:{
                        type:${action},
                        closeWhenDone:${Store.config.closeWhenDone},
                        serviceCode:${Store.paymentInfo.service?.code},
                        amount:${Store.paymentInfo.amount},
                        description:'${description}'
                      }
                    }"""
        )


        val cryptoAES = CryptoAES()
        val xAPIData = cryptoAES.encrytAESDataWebview("LkaWasflkjfqr2g3", data.toString())
        val encode: String = URLEncoder.encode(xAPIData, "utf-8")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)

        if (Store.config.env == Env.DEV) {
            domain = "https://dev-sdk.payme.com.vn/"
        } else if (Store.config.env == Env.SANDBOX) {
            domain = "https://sbx-sdk.payme.com.vn/"
        } else {
            domain = "https://sdk.payme.com.vn/"
        }
        buttonClose.setOnClickListener {
            header.visibility = View.GONE
            myWebView.loadUrl("${domain}active/${encode}")
        }
        myWebView.loadUrl("${domain}active/${encode}")
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
                        InfoPayment(
                            action,
                            amount,
                            note,
                            orderId,
                            storeId,
                            type,
                            Store.paymentInfo.extraData
                        )
                    val paymeSDK = PayME()
                    paymeSDK.payInSDK(
                        this.supportFragmentManager,
                        infoPayment,
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

        isVisible = false
        myWebView.removeAllViews();
        myWebView.destroy()
        EventBus.getDefault().unregister(this);
        super.onDestroy()


    }

    @Subscribe
    fun onText(myEven: MyEven) {
        if (myEven.type == TypeCallBack.onReload) {
            this.myWebView.reload()
        }
        if (myEven.type == TypeCallBack.onExpired) {
            finish()
        }
        if (myEven.type == TypeCallBack.onTakeImageResult) {
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
        if (myEven.type == TypeCallBack.onUpdateIdentify) {

            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onUpdateIdentify()';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            myWebView.evaluateJavascript("(function() {\n" + injectedJS + ";\n})();", null)
        }
        if (myEven.type == TypeCallBack.onScan) {
            myEven.value?.let { checkScanQr(it) }
        }
    }
    fun  showButtonClose(isShow: Boolean){
        runOnUiThread {
            if(isShow == true){
                header.visibility = View.VISIBLE
            }else{
                header.visibility = View.GONE
            }
        }


    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        println("this.myWebView.url"+this.myWebView.url)
        if(this.myWebView.url==domain){
            finish()

        }else if (this.myWebView.canGoBack()) {

            this.myWebView.goBack()
        } else {
            finish()
        }
        return true
    }


}
