package vn.payme.sdk.model

import android.webkit.JavascriptInterface
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME


public class JsObject(val back : ()->Unit) {
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
    public  fun onClose(){
        try {
            back()
        } catch (e: Exception) {
            println(e)
        }
    }
}