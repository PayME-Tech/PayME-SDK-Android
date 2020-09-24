package vn.payme.sdk.model

import android.webkit.JavascriptInterface
import org.json.JSONObject


public class JsObject {
    @JavascriptInterface
    public  fun onSuccess(jsonObject: JSONObject){

    }

    @JavascriptInterface
    public  fun onError(string: String){
        println("adasadasdadsasasddd")

    }
}