package vn.payme.sdk.payment

import android.graphics.Bitmap
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.webview_activity.*
import vn.payme.sdk.R

class ConfirmOtpNapasFragment :Fragment() {
    private lateinit var buttonClose: ImageView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View? = inflater?.inflate(R.layout.confirm_otp_webview_napas, container, false)
        val myWebView: WebView = view!!.findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        buttonClose = view!!.findViewById(R.id.buttonClose)
//
//        val newHeight = activity?.window?.decorView?.measuredHeight
//        val viewGroupLayoutParams = myWebView.layoutParams
//        if (newHeight != null) {
//            viewGroupLayoutParams.height = newHeight - 700 ?: 0
//        }
//        myWebView.layoutParams = viewGroupLayoutParams

        buttonClose.setOnClickListener {

            val  fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container, SelectMethodFragment())
            fragment?.commit()
        }

        val form = arguments?.getString("form")
        println("form"+form)
        myWebView.loadDataWithBaseURL("x-data://base", form, "text/html", "UTF-8", null);
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // Here you can check your new URL.
                val checkSuccess = url.contains("https://sbx-fe.payme.vn/?success=true")
                val checkError = url.contains("https://sbx-fe.payme.vn/?success=false")
                if(checkSuccess){
                    val fragment = fragmentManager?.beginTransaction()
                    fragment?.replace(R.id.frame_container, ResultPaymentFragment())
                    fragment?.commit()
                }else if(checkError){
                    val uri: Uri = Uri.parse(url)
                    val message = uri.getQueryParameter("message")
                    val bundle: Bundle = Bundle()
                    bundle.putString("message",message)
                    val resultPaymentFragment: ResultPaymentFragment = ResultPaymentFragment()
                    resultPaymentFragment.arguments = bundle
                    val fragment = fragmentManager?.beginTransaction()
                    fragment?.replace(R.id.frame_container, resultPaymentFragment)
                    fragment?.commit()
                }

                super.onPageStarted(view, url, favicon)
            }
        })

        return view
    }

}