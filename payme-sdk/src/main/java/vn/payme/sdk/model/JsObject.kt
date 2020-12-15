package vn.payme.sdk.model

import android.hardware.camera2.CameraManager
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentManager
import org.json.JSONObject
import vn.payme.sdk.PayME



public class JsObject(
    val back: () -> Unit,
    val fragmentManager: FragmentManager,
    val cameraManager: CameraManager
) {
    @JavascriptInterface
    public fun onSuccess(string: String) {
        try {
            val json: JSONObject = JSONObject(string)
            PayME.onSuccess(json)

        } catch (e: Exception) {
            println(e)
        }
    }

    @JavascriptInterface
    public fun onFlash(status: Boolean) {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, status)
        } catch (e: java.lang.Exception) {
        }


    }

    @JavascriptInterface
    public fun onError(string: String) {
        try {
            PayME.onError(string)
        } catch (e: Exception) {
            println(e)

        }
    }

    @JavascriptInterface
    public fun onPay(type: String, amount: Int, content: String, orderId: String) {
        try {

//            val payme = PayME(
//                PayME.context,
//                PayME.appToken,
//                PayME.publicKey,
//                PayME.connectToken,
//                PayME.appPrivateKey,
//                PayME.configColor!!,
//                PayME.env!!
//            )
//            payme.pay( amount, content, orderId, "", onSuccess = {
//
//            }, onError = {
//
//            },
//                onClose = {
//
//                }
//            )
//            PayME.onPay(string)
        } catch (e: Exception) {
            println(e)

        }
    }

    @JavascriptInterface
    public fun onClose() {
        try {
            back()
        } catch (e: Exception) {
            println(e)
        }
    }
}