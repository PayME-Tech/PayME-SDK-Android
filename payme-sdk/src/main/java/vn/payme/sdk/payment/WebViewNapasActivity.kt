package vn.payme.sdk.payment

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.PaymeWaletActivity
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.store.Store

class WebViewNapasActivity : DialogFragment() {
    private lateinit var buttonClose: ImageView
    private var message: String? = null
    private var state: String = ""
    private var trans_id: String? = null
    private var onResult: Boolean? = false
    val loading = SpinnerDialog()

    var count = 0
    fun checkVisa(){
        loading.show(parentFragmentManager,null)
        loopCallApi()
    }

    fun loopCallApi(){
        count++
        val paymentApi  = PaymentApi()
        paymentApi.checkVisa(onSuccess = {jsonObject ->
            val HistoryList = jsonObject.optJSONObject("HistoryList")
            val items = HistoryList.optJSONArray("items")
            val item = items.optJSONObject(0)
            if(item!=null){
                val state = item.optString("state")
                if(state=="SUCCEEDED"){
                    onResult = true
                    loading.dismiss()
                    dismiss()
                }else{
                    if(count==5){
                        this.state= state
                        message  = state
                        onResult = true
                        loading.dismiss()
                    }else if(count<5){
                        GlobalScope.launch {
                            delay(5000)
                            loopCallApi()
                        }
                    }
                }
            }else{
                this.state= "FAILED"
                onResult = true
                loading.dismiss()
            }

        },onError = {jsonObject, i, s ->
            message  = s
            onResult = true
            loading.dismiss()

        })

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.isCancelable = false
        val v: View = inflater.inflate(
            R.layout.confirm_otp_webview_napas,
            container, false
        )
        val myWebView: WebView = v.findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        buttonClose =  v.findViewById(R.id.buttonClose)
        buttonClose.setOnClickListener {
            PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
            dismiss()
        }
        val form = if(Store.paymentInfo.methodSelected?.data?.issuer=="VISA"||Store.paymentInfo.methodSelected?.data?.issuer=="JCB"||Store.paymentInfo.methodSelected?.data?.issuer=="MASTERCARD") "<html><body onload=\"document.forms[0].submit();\">${arguments?.getString("html")}</html>," else arguments?.getString("html")
        myWebView.loadDataWithBaseURL("x-data://base", form!!, "text/html", "UTF-8", null);
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                println("url"+url)
                val checkSuccess = url.contains("https://payme.vn/web/?success=true")
                val checkError = url.contains("https://payme.vn/web/?success=false")
                val checkVisa = url.contains("https://payme.vn/web")
                if (checkSuccess || checkError) {
                    val uri: Uri = Uri.parse(url)
                    val messageResult = uri.getQueryParameter("message")
                    val transIdResult = uri.getQueryParameter("trans_id")
                    if (checkError) {
                        message = messageResult
                    }
                    trans_id = transIdResult
                    onResult = true
                    dismiss()
                }else{
                    if (checkVisa) {
                        checkVisa()
                    }

                }

                super.onPageStarted(view, url, favicon)
            }
        })
        return v

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        EventBus.getDefault().register(this)
        setStyle(STYLE_NO_FRAME,R.style.DialogStyle);
    }
    @Subscribe
    fun close(event : MyEven){
        if(event.type == TypeCallBack.onExpired){
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        if (onResult == true) {
            val bundle: Bundle = Bundle()
            if (trans_id != null) {
                Store.paymentInfo.transaction = trans_id!!
            }
            if (message != null) {
                if (!Store.config.disableCallBackResult) {
                    PayME.onError(null, ERROR_CODE.PAYMENT_ERROR, message!!)
                }
                bundle.putString("message", message)
                bundle.putString("state", state)
            } else {
                val data =
                    JSONObject("""{payment:{transaction:${Store.paymentInfo.transaction}}}""")
                if (!Store.config.disableCallBackResult) {
                    PayME.onSuccess(data)
                }
            }
            if (Store.paymentInfo.isShowResultUI) {
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