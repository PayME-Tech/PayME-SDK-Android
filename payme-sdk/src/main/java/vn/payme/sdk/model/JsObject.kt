package vn.payme.sdk.model

import android.app.Activity
import android.content.Intent
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
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeIdentify
import vn.payme.sdk.payment.PopupTakeVideo
import vn.payme.sdk.store.Store


public class JsObject(
    val activity: Activity,
    val back: () -> Unit,
    val showButtonClose: (boolean: Boolean) -> Unit,
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
     fun showButtonCloseNapas(boolean: Boolean) {
        try {
            showButtonClose(boolean)
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
            val message = data.optString("message")
            if(status=="SUCCEEDED"){
                PayME.onSuccess(data)
            }else{
                PayME.onError(data,ERROR_CODE.OTHER,message)

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
            val message = data.optString("message")
            if(status=="SUCCEEDED"){
                PayME.onSuccess(data)
            }else{
                PayME.onError(data,ERROR_CODE.OTHER,message)

            }
            back()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @JavascriptInterface
    fun onTransfer(string: String){
        try {
            val json: JSONObject = JSONObject(string)
            val data = json.getJSONObject("data")
            val status = data.getString("status")
            val message = data.optString("message")
            if(status=="SUCCEEDED"){
                PayME.onSuccess(data)
            }else{
                PayME.onError(data,ERROR_CODE.OTHER,message)

            }
            back()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @JavascriptInterface
    public fun onCommunicate(string: String) {
        println("onCommunicate"+string)
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
            back()
            PayME.onError(data, code, message)
            if(code==ERROR_CODE.EXPIRED){
                val payme = PayME()
                payme.logout()
            }

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
    public fun onUpdateIdentify() {
        Store.config.kycVideo = false
        Store.config.kycIdentify = true
        Store.config.kycFace = false
        val bundle: Bundle = Bundle()
        CameraKycActivity.updateOnlyIdentify  = true
        val intent = Intent(PayME.context, CameraKycActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        PayME.context?.startActivity(intent)
    }

    @JavascriptInterface
    public fun onKyc(kycVideo: Boolean, kycIdentity: Boolean, kycFace: Boolean) {

        val payme = PayME()
        payme.onPopupKyc(fragmentManager,kycVideo,kycIdentity,kycFace)


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