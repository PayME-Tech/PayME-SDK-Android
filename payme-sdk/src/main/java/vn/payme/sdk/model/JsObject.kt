package vn.payme.sdk.model

import android.app.Activity
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject
import vn.payme.sdk.AnyOrientationCaptureActivity
import vn.payme.sdk.PayME
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.kyc.TakePictureIdentifyFragment
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
    public fun onRegisterSuccess(accessToken: String, handShake: String) {
        try {
            println("accessToken"+accessToken)
            println("handShake"+handShake)
            PayME.accessToken = accessToken
            PayME.handShake = handShake

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
        println("kycVideo" + kycVideo)
        println("kycIdenity" + kycIdentity)
        println("kycFace" + kycFace)
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
            println(e)
        }
    }
}