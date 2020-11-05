package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.Method

class PaymentApi {
    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-wam.payme.vn"
        }
        return "https://wam.payme.vn"
    }
    fun getTransferMethods(onSuccess: (JSONObject) -> Unit, onError:  (JSONObject?,Int?,String)  -> Unit){
        val url = urlFeENV("sandbox")
        val path = "/v1/Transfer/GetMethods"
        val params: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, params)
        request.setOnRequestCrypto(
                onStart = {

                },
                onError = onError,
                onFinally = {
                },
                onSuccess = onSuccess,
                onExpired = {

                })

    }
    fun postTransferAppWallet(onSuccess: (JSONObject) -> Unit, onError:  (JSONObject?,Int?,String)  -> Unit){
        val url = urlFeENV("sandbox")
        val path = "/v1/Transfer/AppWallet/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["amount"] = PayME.amount
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, params)
        request.setOnRequestCrypto(
                onStart = {
                },
                onError = onError,
                onFinally = {
                },
                onSuccess = onSuccess,
                onExpired = {

                })

    }
    fun postTransferNapas(method:Method,onSuccess: (JSONObject) -> Unit, onError: (JSONObject?,Int?,String) -> Unit){
        val url = urlFeENV("sandbox")
        val path = "/v1/Transfer/Napas/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["amount"] = PayME.amount
        params["linkedId"] = method.linkedId!!
        params["bankCode"] = method.bankCode!!
        params["destination"] = "AppPartner"
        params["returnUrl"] = "https://sbx-fe.payme.vn/"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, params)
        request.setOnRequestCrypto(
                onStart = {
                },
                onError = onError,
                onFinally = {
                },
                onSuccess = onSuccess,
                onExpired = {

                })

    }
    fun postTransferPVCB(method:Method,onSuccess: (JSONObject) -> Unit, onError: (JSONObject?,Int?,String) -> Unit){
        val url = urlFeENV("sandbox")
        val path = "/v1/Transfer/PVCBank/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["amount"] = PayME.amount
        params["linkedId"] = method.linkedId!!
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, params)
        request.setOnRequestCrypto(
                onStart = {
                },
                onError = onError,
                onFinally = {
                },
                onSuccess = onSuccess,
                onExpired = {

                })

    }
    fun postTransferPVCBVerify(transferId:String,OTP:String,onSuccess: (JSONObject) -> Unit, onError: (JSONObject?,Int?,String) -> Unit){
        val url = urlFeENV("sandbox")
        val path = "/v1/Transfer/PVCBank/Verify"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["transferId"] = transferId
        params["OTP"] = OTP
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, url, path, PayME.appToken, params)
        request.setOnRequestCrypto(
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