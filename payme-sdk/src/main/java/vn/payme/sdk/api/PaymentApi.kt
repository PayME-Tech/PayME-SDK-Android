package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.model.Method
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal class PaymentApi {


    fun getTransferMethods(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Transfer/GetMethods"
        val params: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
           )

    }

    fun getBalance(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "query Query {\n" +
                "  Wallet {\n" +
                "    balance\n" +
                "    cash\n" +
                "    lockCash\n" +
                "  }\n" +
                "}"
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun genConnectToken(
        userId: String,
        phone: String?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Internal/ConnectToken/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        params["userId"] = userId
        if (phone!!.length > 0) {
            params["phone"] = phone!!
        }
        val tz = TimeZone.getTimeZone("UTC")
        val df: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset

        df.setTimeZone(tz)
        val nowAsISO: String = df.format(Date())
        println("nowAsISO" + nowAsISO)
        params["timestamp"] = nowAsISO
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun postTransferAppWallet(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Transfer/AppWallet/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        data["orderId"] = PayME.orderId!!
        data["content"] = PayME.content!!
        params["connectToken"] = PayME.connectToken
        params["amount"] = PayME.amount
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
           )

    }

    fun postTransferNapas(
        method: Method,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Transfer/Napas/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        data["orderId"] = PayME.orderId!!
        data["content"] = PayME.content!!
        params["connectToken"] = PayME.connectToken
        params["amount"] = PayME.amount
        params["linkedId"] = method.linkedId!!
        params["bankCode"] = method.bankCode!!
        params["destination"] = "AppPartner"
        params["returnUrl"] = "https://sbx-fe.payme.vn/"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun postCheckDataQr(
        dataQR: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Pay/PayWithQRCode"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = PayME.connectToken
        params["data"] = dataQR
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
          )

    }

    fun postTransferPVCB(
        method: Method,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Transfer/PVCBank/Generate"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        data["orderId"] = PayME.orderId!!
        data["content"] = PayME.content!!
        params["connectToken"] = PayME.connectToken
        params["amount"] = PayME.amount
        params["linkedId"] = method.linkedId!!
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
           )

    }

    fun postTransferPVCBVerify(
        transferId: String,
        OTP: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Transfer/PVCBank/Verify"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        data["orderId"] = PayME.orderId!!
        data["content"] = PayME.content!!
        params["connectToken"] = PayME.connectToken
        params["transferId"] = transferId
        params["OTP"] = OTP
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
            )

    }
}