package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME

class UploadApi {
    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-static.payme.vn"
        }
        return "https://wam.payme.vn"
    }

    fun uploadImage(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit,
        imageFront: String,
        imageBackSide: String,

        ) {
        val url = urlFeENV("sandbox")
        val path = "/Upload"
        var params = JSONObject()
        params.putOpt("files", imageFront)
        params.putOpt("files", imageBackSide)
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, null,params)
        request.setOnRequest(
            onStart = {

            },
            onError = onError,
            onFinally = {
            },
            onSuccess = onSuccess,
            onExpired = {

            })

    }
}