package vn.payme.sdk.model

import android.app.Activity
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentManager
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import vn.payme.sdk.AnyOrientationCaptureActivity
import vn.payme.sdk.PayME
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeIdentify
import vn.payme.sdk.payment.PopupTakeVideo


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
    public fun onCommunicate(string: String) {
        try {
            val json: JSONObject = JSONObject(string)
            val type = json.optString("type")
            val actions = json.optString("actions")
            if(actions=="onRegisterSuccess"){
                val dataInt = json.optJSONObject("data")
                val Init = dataInt.getJSONObject("Init")
                PayME.dataInit = Init
                val accessToken = Init.optString("accessToken")
                val handShake = Init.optString("handShake")
                val kyc = Init.optJSONObject("kyc")
                if (!accessToken.equals("null")) {
                    PayME.accessToken = accessToken
                } else {
                    PayME.accessToken = ""
                }
                if (kyc != null) {
                    val state = kyc.optString("state")
                    if (state == "APPROVED") {
                        PayME.accountKycSuccess = true
                    } else {
                        PayME.accountKycSuccess = false
                    }
                } else {
                    PayME.accountKycSuccess = false
                }
                PayME.accountActive = true
                PayME.handShake = handShake
            }else if(actions=="onNetworkError"){

            }


        } catch (e: Exception) {
            e.printStackTrace()
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
    public fun onError(data: String?) {
        try {
            val jsonObject = JSONObject(data)
            val code = jsonObject.optInt("code")
            val data = jsonObject.optJSONObject("data")
            val message = jsonObject.optString("message")
            val type = jsonObject.optString("type")
            println("jsonObject" + jsonObject)
            back()
            PayME.onError(data, code, message)
        } catch (e: Exception) {
            println(e)

        }
    }

    @JavascriptInterface
    public fun onScanQr() {
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
    public fun onKyc(kycVideo: Boolean, kycIdentity: Boolean, kycFace: Boolean) {
        PayME.kycVideo = kycVideo
        PayME.kycIdenity = kycIdentity
        PayME.kycFace = kycFace
        val bundle: Bundle = Bundle()
        bundle.putBoolean("openKycActivity", true)
        if (kycIdentity) {
            val popupTakeIdentify = PopupTakeIdentify()
            popupTakeIdentify.arguments = bundle
            popupTakeIdentify.show(fragmentManager, "ModalBottomSheet")
        } else if (kycFace) {
            val popupTakeFace = PopupTakeFace()
            popupTakeFace.arguments = bundle
            popupTakeFace.show(fragmentManager, "ModalBottomSheet")

        } else if (kycVideo) {
            val popupTakeVideo = PopupTakeVideo()
            popupTakeVideo.arguments = bundle
            popupTakeVideo.show(fragmentManager, "ModalBottomSheet")
        }


    }


    @JavascriptInterface
    public fun onClose() {
        try {
            back()
        } catch (e: Exception) {
        }
    }
}