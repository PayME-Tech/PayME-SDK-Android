package vn.payme.sdk.model

import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import java.time.temporal.TemporalAmount


public class JsObject(val back : ()->Unit,val fragmentManager: FragmentManager) {
    @JavascriptInterface
    public  fun onSuccess(string: String){
        try {
            val  json :JSONObject = JSONObject(string)
            PayME.onSuccess(json)

        } catch (e: Exception) {
            println(e)
        }
    }

    @JavascriptInterface
    public  fun onError(string: String){
        try {
            PayME.onError(string)
        } catch (e: Exception) {
            println(e)

        }
    }
    @JavascriptInterface
    public  fun onPay(amount: Int){
        try {
            val  payme  = PayME(PayME.context,PayME.appToken,PayME.publicKey,PayME.connectToken,PayME.appPrivateKey,PayME.configColor!!,PayME.env!!)
            payme.pay(fragmentManager,amount,"","","",onSuccess = {

            },onError = {

            })
//            PayME.onPay(string)
        } catch (e: Exception) {
            println(e)

        }
    }

    @JavascriptInterface
    public  fun onClose(){
        try {
            back()
        } catch (e: Exception) {
            println(e)
        }
    }
}