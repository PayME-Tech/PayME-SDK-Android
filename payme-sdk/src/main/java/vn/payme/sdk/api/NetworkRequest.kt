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
import java.nio.charset.Charset


internal class NetworkRequest(
    private val context: Context,
    private val url: String,
    private val path: String,
    private val token: String,
    private val params: MutableMap<String, Any>?,
) {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setOnRequestCrypto(
        onSuccess: (response: JSONObject) -> Unit,
        onError: (data: JSONObject?, code: Int?, message: String) -> Unit,
        onExpired: (() -> Unit)?
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

        val body: MutableMap<String, Any> = mutableMapOf()
        body["x-api-message"] = xAPIMessage

        val queue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
            Method.POST,
            url,
            JSONObject(body as Map<*, *>),
            Response.Listener { response ->
                println("Response${response.toString()}")

                try {
                    val jsonObject = JSONObject(response.toString())
                    val xAPIMessageResponse = jsonObject.getString("x-api-message")
                    val headers = jsonObject.getJSONObject("headers")
                    val xAPIActionResponse = headers.getString("x-api-action")
                    val xAPIKeyResponse = headers.getString("x-api-key")
                    val xAPIValidateResponse = headers.getString("x-api-validate")
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
                    val validateMD5 = cryptoAES.getMD5(validateString)
                    val result = cryptoAES.decryptAES(decryptKey, xAPIMessageResponse)
                    println("result" + result)
                    val json = result?.replace("\\\"","'");
                    val finalJSONObject = JSONObject(json?.substring(1,json?.length-1))

                    val data =  finalJSONObject.optJSONObject("data")
                    val errors =  finalJSONObject.optJSONArray("errors")

                    if(errors!=null){
                        val error = errors.getJSONObject(0)
                        val message = error.getString("message")
                        onError(data,-3,message)
                    }else if(data!=null){
                        onSuccess(data)
                    }

                    println("data"+data)
                    println("error"+errors)


                } catch (error: JSONException) {
                    error.printStackTrace()
                }
            },
            Response.ErrorListener { error ->

                onError(
                    null,
                    -2,
                    "Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !"
                )
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers: MutableMap<String, String> = mutableMapOf()
                headers["Authorization"] = token
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                headers["x-api-client"] = "app"
                headers["x-api-key"] = xAPIKey
                headers["x-api-action"] = xAPIAction
                headers["x-api-validate"] = xAPIValidate
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
