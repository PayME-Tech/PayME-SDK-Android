package vn.payme.sdk.model

import android.app.Activity
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentManager
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import vn.payme.sdk.AnyOrientationCaptureActivity
import vn.payme.sdk.PayME
import vn.payme.sdk.kyc.CameraKycActivity


public class JsObject(
    val activity: Activity,
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
    public  fun  onScanQr(){
        IntentIntegrator(activity).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            captureActivity = AnyOrientationCaptureActivity::class.java
            setPrompt("")
            setCameraId(0)
            setRequestCode(5)
            setBeepEnabled(true)
            setOrientationLocked(false)
            initiateScan()
        }


    }
    @JavascriptInterface
    public  fun  onKyc(){
        val intent = Intent(PayME.context, CameraKycActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        PayME.context?.startActivity(intent)
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