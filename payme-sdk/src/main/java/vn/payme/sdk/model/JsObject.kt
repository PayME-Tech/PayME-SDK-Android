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
import vn.payme.sdk.PaymeWaletActivity
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeIdentify
import vn.payme.sdk.payment.PopupTakeVideo
import vn.payme.sdk.store.Store


public class JsObject(
    val activity: Activity,
    val back: () -> Unit,
    val takeImage: () -> Unit,
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
    public fun onTakeImageDocument() {
        try {
            takeImage()
        } catch (e: Exception) {
            println(e)
        }
    }
    @JavascriptInterface
    public fun getImage(): String {
       return "data:image/png;base64," +PaymeWaletActivity.image
    }
    @JavascriptInterface
    fun onDeposit(string: String){
        try {
            val json: JSONObject = JSONObject(string)
            val data = json.getJSONObject("data")
            val status = data.getString("status")
            if(status=="SUCCEEDED"){
                PayME.onSuccess(null)
            }else{
                PayME.onError(null,null,"")

            }
            back()

        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @JavascriptInterface
    fun onWithdraw(string: String){
        try {
            val json: JSONObject = JSONObject(string)
            val data = json.getJSONObject("data")
            val status = data.getString("status")
            if(status=="SUCCEEDED"){
                PayME.onSuccess(null)
            }else{
                PayME.onError(null,null,"")

            }
            back()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    public fun onCommunicate(string: String) {
//        println("onCommunicate"+string)
        try {
            val json: JSONObject = JSONObject(string)
            val type = json.optString("type")
            val actions = json.optString("actions")
            if (actions == "onRegisterSuccess") {
                val dataInt = json.optJSONObject("data")
                val Init = dataInt.getJSONObject("Init")
                Store.userInfo.dataInit = Init
                val accessToken = Init.optString("accessToken")
                val handShake = Init.optString("handShake")
                val kyc = Init.optJSONObject("kyc")
                if (!accessToken.equals("null")) {
                    Store.userInfo.accessToken = accessToken
                } else {
                    Store.userInfo.accessToken = ""
                }
                if (kyc != null) {
                    val state = kyc.optString("state")
                    if (state == "APPROVED") {
                        Store.userInfo.accountKycSuccess = true
                    } else {
                        Store.userInfo.accountKycSuccess = false
                    }
                } else {
                    Store.userInfo.accountKycSuccess = false
                }
                Store.userInfo.accountActive = true
                Store.config.handShake = handShake
            } else if (actions == "onNetworkError") {

            }


        } catch (e: Exception) {
            e.printStackTrace()
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
//            println("onError" + jsonObject)
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
//        println("onKyc")
        Store.config.kycVideo = kycVideo
        Store.config.kycIdenity = kycIdentity
        Store.config.kycFace = kycFace
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
//            println("onClose")

            back()
        } catch (e: Exception) {
        }
    }
}