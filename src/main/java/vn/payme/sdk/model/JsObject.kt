package vn.payme.sdk.model

import android.webkit.JavascriptInterface
import org.greenrobot.eventbus.EventBus


public class JsObject(val back : ()->Unit) {
    @JavascriptInterface
    public  fun onSuccess(string: String){
        try {
            var even : EventBus = EventBus.getDefault()
            var myEven : MyEven = MyEven(TypeCallBack.onSuccess,string)
            even.post(myEven)
        } catch (e: Exception) {
            println(e)

        }



    }

    @JavascriptInterface
    public  fun onError(string: String){
        try {
            var even : EventBus = EventBus.getDefault()
            var myEven : MyEven = MyEven(TypeCallBack.onError,string)
            even.post(myEven)
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