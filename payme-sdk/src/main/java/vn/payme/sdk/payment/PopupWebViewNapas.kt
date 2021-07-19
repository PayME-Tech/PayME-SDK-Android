package vn.payme.sdk.payment

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.store.Store

class PopupWebViewNapas : DialogFragment() {
    private lateinit var buttonClose: ImageView
    val loading = SpinnerDialog()

    var count = 0
    fun checkVisa() {
        if (isVisible) {
            loading.show(parentFragmentManager, null)
            loopCallApi()
        }
    }

    fun loopCallApi() {
        count++
        val paymentApi = PaymentApi()
        paymentApi.checkVisa(onSuccess = { jsonObject ->
            val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
            val Payment = OpenEWallet.optJSONObject("Payment")
            val GetTransactionInfo = Payment.optJSONObject("GetTransactionInfo")
            val state = GetTransactionInfo.optString("state")
            val succeeded = GetTransactionInfo.optBoolean("succeeded")
            val transaction = GetTransactionInfo.optString("transaction")
            val message = GetTransactionInfo.optString("message")
            if (succeeded) {
                if (state == "SUCCEEDED") {
                    loading.dismiss()
                    onResult("", state)
                } else {
                    if (state == "PENDING") {
                        if (count == 6) {
                            loading.dismiss()
                            onResult(message, state)
                        } else if (count < 6) {
                            GlobalScope.launch {
                                delay(10000)
                                loopCallApi()
                            }
                        }
                    } else {
                        loading.dismiss()
                        onResult(message, state)
                    }
                }
            } else {
                loading.dismiss()
                onResult(message, "FAILED")
            }


        }, onError = { jsonObject, code, s ->
            loading.dismiss()
            onResult(s, "FAILED")

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.isCancelable = false
        val v: View = inflater.inflate(
            R.layout.payment_confirm_otp_webview_napas,
            container, false
        )
        val myWebView: WebView = v.findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        buttonClose = v.findViewById(R.id.buttonClose)
        buttonClose.setOnClickListener {
            if (!Store.config.disableCallBackResult) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
            }
            dismiss()
        }
        var form = ""
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD ||
            Store.paymentInfo.methodSelected?.data?.issuer == "VISA" ||
            Store.paymentInfo.methodSelected?.data?.issuer == "JCB" ||
            Store.paymentInfo.methodSelected?.data?.issuer == "MASTERCARD"
        ) {
            form = "<html><body onload=\"document.forms[0].submit();\">${
                arguments?.getString("html")
            }</html>,"
        } else {
            form = arguments?.getString("html")!!
        }
        myWebView.loadDataWithBaseURL("x-data://base", form!!, "text/html", "UTF-8", null);
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                val checkSuccess = url.contains("https://payme.vn/web/?success=true")
                val checkError = url.contains("https://payme.vn/web/?success=false")
                val checkVisa = url.contains("https://payme.vn/web")
                if (checkSuccess || checkError) {
                    val uri: Uri = Uri.parse(url)
                    val messageResult = uri.getQueryParameter("message")
                    val transIdResult = uri.getQueryParameter("trans_id")
                    if (isVisible) {
                        if (checkError) {
                            onResult(messageResult!!, "FAILED")
                        } else {
                            onResult("", "SUCCEEDED")
                        }
                    }
                } else {
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
        setStyle(STYLE_NO_FRAME, R.style.DialogStyle);
    }

    @Subscribe
    fun close(event: MyEven) {
        if (event.type == TypeCallBack.onExpired) {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun onResult(message: String?, state: String) {
        dismiss()
        val bundle: Bundle = Bundle()
        if (state == "SUCCEEDED") {
            val data = if (Store.paymentInfo.transaction == "" || Store.paymentInfo.transaction == "null")   JSONObject("""{payment:{}}}""")
            else  JSONObject("""{payment:{transaction:"${Store.paymentInfo.transaction}"}}""")
            if (!Store.config.disableCallBackResult) {
                PayME.onSuccess(data)
            }
        } else {
            bundle.putString("message", message)
            bundle.putString("state", state)
            if (!Store.config.disableCallBackResult) {
                val data = JSONObject("""{state:${state}}""")
                PayME.onError(data, ERROR_CODE.PAYMENT_ERROR, message)
            }
        }
        if (Store.paymentInfo.isShowResultUI) {
            bundle.putBoolean("showResult", true)
            val paymePayment: PopupPayment = PopupPayment()
            paymePayment.arguments = bundle
            paymePayment.show(
                PayME.fragmentManager,
                "ModalBottomSheet"
            )
        }
    }

}