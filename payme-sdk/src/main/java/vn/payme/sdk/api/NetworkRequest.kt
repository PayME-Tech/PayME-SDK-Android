package vn.payme.sdk.api

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import vn.payme.sdk.BuildConfig
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.store.Store
import java.nio.charset.Charset
import kotlin.random.Random


internal class NetworkRequest(
    private val context: Context,
    private val url: String,
    private val path: String,
    private val token: String,
    private val params: MutableMap<String, Any>?,
    private val isSecurity: Boolean,

    ) {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setOnRequestCrypto(
        onSuccess: (response: JSONObject) -> Unit,
        onError: (data: JSONObject?, code: Int, message: String?) -> Unit,
    ) {
        var checkErrorRSA = false
        try {
            val cryptoAES = CryptoAES()
            val cryptoRSA = CryptoRSA()
        } catch (e: Exception) {
            e.printStackTrace()
            checkErrorRSA = true
        }

        if (checkErrorRSA) {
            onError(null, ERROR_CODE.ERROR_KEY_ENCODE, "Vui lòng kiểm tra lại key mã hóa")
        } else {
            val cryptoRSA = CryptoRSA()
            val nextValues = Random.nextInt(0, 10000000)
            val encryptKey = nextValues.toString()
            val xAPIKey = cryptoRSA.encrypt(encryptKey)
            val cryptoAES = CryptoAES()
            val xAPIAction = cryptoAES.encryptAES(encryptKey, path)
            val xAPIMessage =
                cryptoAES.encryptAES(encryptKey, JSONObject(params as Map<*, *>).toString())
            val objectValidateRequest: MutableMap<String, String> = mutableMapOf()
            objectValidateRequest["xApiAction"] = xAPIAction
            objectValidateRequest["method"] = "POST"
            objectValidateRequest["accessToken"] = token
            objectValidateRequest["x-api-message"] = xAPIMessage
            var valueParams = ""
            for (key in objectValidateRequest.keys) {
                valueParams += objectValidateRequest[key]
            }
            valueParams += encryptKey
            val xAPIValidate = cryptoAES.getMD5(valueParams)
            var body: MutableMap<String, Any> = mutableMapOf()

            if (isSecurity) {
                body["x-api-message"] = xAPIMessage
            } else {
                body = params
            }

            var pathAPi = ""
            pathAPi = if (isSecurity) {
                url
            } else {
                url + path
            }

            val queue = Volley.newRequestQueue(context)
            val request = object : JsonObjectRequest(
                Method.POST,
                pathAPi,
                JSONObject(body as Map<*, *>),
                Response.Listener { response ->
                    try {
                        var finalJSONObject: JSONObject? = null
                        if (isSecurity) {
                            val jsonObject = JSONObject(response.toString())
                            val xAPIMessageResponse = jsonObject.getString("x-api-message")
                            val headers = jsonObject.getJSONObject("headers")
                            val xAPIActionResponse = headers.getString("x-api-action")
                            val xAPIKeyResponse = headers.getString("x-api-key")
                            val decryptKey = cryptoRSA.decrypt(xAPIKeyResponse)
                            val objectValidateResponse: MutableMap<String, String> = mutableMapOf()
                            objectValidateResponse["x-api-action"] = xAPIActionResponse
                            objectValidateResponse["method"] = "POST"
                            objectValidateResponse["accessToken"] = token
                            objectValidateResponse["x-api-message"] = xAPIMessageResponse
                            var validateString = ""
                            for (key in objectValidateResponse.keys) {
                                validateString += objectValidateResponse[key]
                            }
                            validateString += decryptKey
                            val result = cryptoAES.decryptAES(decryptKey, xAPIMessageResponse)
                            var dataRaw = ConvertJSON().toString(result)
                            finalJSONObject = JSONObject(dataRaw)
                            if (BuildConfig.DEBUG){
                                Log.d("HIEU", "PARAMS $params")
                                Log.d("HIEU", "RESPONSE $finalJSONObject")
                            }
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d("HIEU", "RESPONSE  $response$params")
                            }
                            finalJSONObject = JSONObject(response.toString())
                        }
                        val data = finalJSONObject?.optJSONObject("data")
                        val errors = finalJSONObject?.optJSONArray("errors")
                        if (errors != null) {
                            val error = errors.getJSONObject(0)
                            var code = ERROR_CODE.SYSTEM
                            val extensions = error.getJSONObject("extensions")
                            code = extensions.optInt("code")
                            val message = error.optString("message")
                            if(code==ERROR_CODE.EXPIRED){
                                val payme = PayME()
                                payme.logout()
                                PayME.onError(data, code, message)
                            }else{
                                onError(data, code, message)
                            }
                        } else if (data != null) {
                            onSuccess(data)
                        }
                    } catch (error: Exception) {
                        error.printStackTrace()
                        onError(
                            null,
                            ERROR_CODE.SYSTEM,
                            "Không thể kết nối tới server, vui lòng kiểm tra và thử lại. Xin cảm ơn !"
                        )
                    }
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    onError(
                        null,
                        ERROR_CODE.NETWORK,
                        "Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !"
                    )
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers: MutableMap<String, String> = mutableMapOf()
                    headers["Authorization"] = token
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json"
                    headers["language"] =  Store.config.language.toString().toLowerCase()
                    if (isSecurity) {
                        headers["x-api-client"] = Store.config.appID.toString()
                        headers["x-api-key"] = xAPIKey
                        headers["x-api-action"] = xAPIAction
                        headers["x-api-validate"] = xAPIValidate
                    }else{
                        headers["x-api-client"] = Store.config.appID.toString()
                    }
                    return headers
                }

                override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                    return try {
                        val jsonString = String(
                            response!!.data,
                            Charset.forName(
                                HttpHeaderParser.parseCharset(
                                    response.headers,
                                    PROTOCOL_CHARSET
                                )
                            )
                        )
                        val jsonResponse = JSONObject(jsonString)
                        jsonResponse.put("headers", JSONObject(response.headers as Map<*, *>))
                        Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response))
                    } catch (e: JSONException) {
                        Response.error<JSONObject>(ParseError(e))
                    } catch (je: JSONException) {
                        Response.error<JSONObject>(ParseError(je))
                    }
                }
            }
            val defaultRetryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            request.retryPolicy = defaultRetryPolicy
            queue.add(request)
        }
    }


}
