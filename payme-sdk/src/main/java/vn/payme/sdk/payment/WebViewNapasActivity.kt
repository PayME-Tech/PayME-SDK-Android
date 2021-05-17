package vn.payme.sdk.payment

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.store.Store

class WebViewNapasActivity : AppCompatActivity() {
    private lateinit var buttonClose: ImageView
    private  var message : String? = null
    private  var trans_id : String? = null
    private  var onResult : Boolean? = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_otp_webview_napas)
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        buttonClose = findViewById(R.id.buttonClose)
        buttonClose.setOnClickListener {
            PayME.onError(null, ERROR_CODE.USER_CANCELLED,"")
            finish()
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setBackgroundDrawable(Store.config.colorApp.backgroundColor);
        val form = intent.extras?.getString("html")
        myWebView.loadDataWithBaseURL("x-data://base", form!!, "text/html", "UTF-8", null);
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                val checkSuccess = url.contains("https://payme.vn/web/?success=true")
                val checkError = url.contains("https://payme.vn/web/?success=false")
                if(checkSuccess || checkError){
                    val uri: Uri = Uri.parse(url)
                    val messageResult = uri.getQueryParameter("message")
                    val transIdResult = uri.getQueryParameter("trans_id")
                    if(checkError){
                        message = messageResult
                    }
                    trans_id = transIdResult
                    onResult = true
                    finish()
                }

                super.onPageStarted(view, url, favicon)
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        if(onResult == true){
            val bundle: Bundle = Bundle()
            if(trans_id!=null){
                Store.paymentInfo.transaction = trans_id!!
            }
            if(message!=null){
                PayME.onError(null,ERROR_CODE.PAYMENT_ERROR, message!!)
                bundle.putString("message", message)
            }else{
                val data = JSONObject("""{transaction:${Store.paymentInfo.transaction}}""")
                PayME.onSuccess(data)
            }
            if(Store.paymentInfo.isShowResultUI){
                bundle.putBoolean("showResult", true)
                val paymePayment: PaymePayment = PaymePayment()
                paymePayment.arguments = bundle
                paymePayment.show(
                    PayME.fragmentManager,
                    "ModalBottomSheet"
                )
            }


        }


    }
}