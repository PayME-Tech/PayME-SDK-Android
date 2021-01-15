package vn.payme.sdk.api

import android.content.Context
import android.os.Build
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
import vn.payme.sdk.model.ERROR_CODE
import java.nio.charset.Charset
import java.util.*


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
        onError: (data: JSONObject?, code: Int?, message: String) -> Unit,
    ) {
        println("REQUESSSSSSSSSSSSSSSSSSSS1${params.toString()}")
        val cryptoAES = CryptoAES()
        val cryptoRSA = CryptoRSA()
        val encryptKey = "10000000"
        val xAPIKey = cryptoRSA.encrypt(encryptKey)
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
        if (isSecurity) {
            pathAPi = url
        } else {
            pathAPi = url + path
        }
        println("pathAPi:" + pathAPi)


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
                        println("Response" + result)
                        val json = result?.replace("\\\"", "'");
                        finalJSONObject = JSONObject(json?.substring(1, json?.length - 1))
                    } else {
                        println("Response" + response.toString())


                        finalJSONObject = JSONObject(response.toString())
                    }
                    val data = finalJSONObject?.optJSONObject("data")
                    val errors = finalJSONObject?.optJSONArray("errors")

                    if (errors != null) {
                        val error = errors.getJSONObject(0)
                        var code = ERROR_CODE.SYSTEM

                        val extensions = error.getJSONObject("extensions")
                        if (extensions != null) {
                            code = extensions.optInt("code")
                            println("code1111111111"+code)

                        }
                        val message = error.optString("message")
                        onError(data, code, message)
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
                if (isSecurity) {
                    headers["x-api-client"] = "app"
                    headers["x-api-key"] = xAPIKey
                    headers["x-api-action"] = xAPIAction
                    headers["x-api-validate"] = xAPIValidate
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
