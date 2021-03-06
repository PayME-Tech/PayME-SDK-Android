package vn.payme.sdk

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.api.CryptoAES
import vn.payme.sdk.component.Button
import vn.payme.sdk.credit.CameraTakeProfileCreditActivity
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.JsObject
import vn.payme.sdk.store.Store
import java.net.URLEncoder


class PayMEOpenSDKPopup : DialogFragment() {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        PayMEOpenSDKPopup.isVisible = true
        setStyle(STYLE_NO_FRAME,R.style.DialogStyle);
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if(isVisible) return
        super.show(manager, tag)
    }
    companion object {
        var image: String = ""
        var isVisible  = false
    }

    private fun backScreen(): Unit {
            dismiss()
    }

    fun takeImage(): Unit {
//        val intent = Intent(this, CameraTakeProfileCreditActivity::class.java)
//        this?.startActivity(intent)
        val cameraTakeProfileCreditActivity = CameraTakeProfileCreditActivity()
        cameraTakeProfileCreditActivity.show(parentFragmentManager,null)
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
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
            R.layout.webview_activity,
            container, false
        )

        EventBus.getDefault().register(this)
//        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        WebView(requireContext()).clearCache(true)
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }


        dialog?.window?.setStatusBarColor(Color.TRANSPARENT);
        dialog?.window?.setBackgroundDrawable(Store.config.colorApp.backgroundColor);

        myWebView = v.findViewById(R.id.webview)
        loadingProgressBar =  v.findViewById(R.id.loadingProgressBar)
        loading =  v.findViewById(R.id.loading)

        buttonBack =  v.findViewById(R.id.buttonBack)
        buttonNext =  v.findViewById(R.id.buttonNext)
        header =  v.findViewById(R.id.header)
        buttonClose =  v.findViewById(R.id.buttonClose)
        containerErrorNetwork =  v.findViewById(R.id.containerErrorNetwork)


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
            dismiss()
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
                        if (isVisible){
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
            fragmentManager?.let {
                JsObject(
                    back = { backScreen() },
                    showButtonClose={b->
                        showButtonClose(b)
                    },
                    onScanQR={
                        val payme = PayME()
                        payme.openScanQR(parentFragmentManager,PayME.onSuccess,PayME.onError)
                    },
                    takeImage = { takeImage() },
                    it,
                )
            }!!
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
                      language: '${Store.config.language.toString().toLowerCase()}',
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
            myWebView.loadUrl("${domain}")
        }
        myWebView.loadUrl("${domain}active/${encode}")
        if (!isNetworkConnected()) {
            containerErrorNetwork?.visibility = View.VISIBLE
        }
        return  v
    }



    override fun onDestroy() {
        myWebView.removeAllViews();
        myWebView.destroy()
        PayMEOpenSDKPopup.isVisible = false
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }

    @Subscribe
    fun onText(myEven: MyEven) {

        if (myEven.type == TypeCallBack.onReload) {
            this.myWebView.reload()
        }
        if (myEven.type == TypeCallBack.onExpired) {
            dismiss()
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

    }
    fun  showButtonClose(isShow: Boolean){
            if(isShow == true){
                header.visibility = View.VISIBLE
            }else{
                header.visibility = View.GONE
            }


    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                if(myWebView.url==domain){
                    dismiss()
                }else if (myWebView.canGoBack()) {
                    myWebView.goBack()
                } else {
                    dismiss()
                }
            }
        }
    }


}
