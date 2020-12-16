package vn.payme.sdk.api

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

        imageFront: String,
        imageBackSide: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit

    ) {
        val url = urlStaticENV("sandbox")
        val path = "/Upload"
        var params = JSONObject()
        params.putOpt("files", imageFront)
        params.putOpt("files", imageBackSide)
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, null, params)
        request.setOnRequest(
            onStart = {

            },
            onError = onError,
            onFinally = {
            },
            onSuccess = {
                val url = urlFeENV("sandbox")
                val path = "/v1/Account/Kyc"
                val params: MutableMap<String, Any> = mutableMapOf()
                val image: MutableMap<String, Any> = mutableMapOf()
                image["front"] = "front"
                image["back"] = "back"
                params["image"] = image
                params["connectToken"] = PayME.connectToken.toString()
                params["identifyType"] = "CMND"
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


            },
            onExpired = {

            })

    }

}