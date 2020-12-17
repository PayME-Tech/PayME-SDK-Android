package vn.payme.sdk.api

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import org.json.JSONObject
import vn.payme.sdk.PayME

class UploadKycApi {
    private fun urlStaticENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-static.payme.vn"
        }
        return "https://static.payme.vn"
    }

    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-wam.payme.vn"
        }
        return "https://wam.payme.vn"
    }

    fun uploadKycInfo(
        typeIdentify: String,
        imageFront: String,
        imageBackSide: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        val url = urlFeENV("sandbox")
        val path = "/v1/Account/Kyc"
        val params: MutableMap<String, Any> = mutableMapOf()
        val image: MutableMap<String, Any> = mutableMapOf()
        image["front"] = imageFront
        image["back"] = imageBackSide
        params["image"] = image
        params["connectToken"] = PayME.connectToken.toString()
        params["identifyType"] = typeIdentify
        params["clientInfo"] = PayME.clientInfo.getClientInfo()

        val request =
            NetworkRequest(PayME.context!!, url, path, PayME.appToken, params, null)
        request.setOnRequestCrypto(
            onStart = {

            },
            onError = onError,
            onFinally = {

            },
            onSuccess = onSuccess,
            onExpired = {
                println("401")

            })



    }

}