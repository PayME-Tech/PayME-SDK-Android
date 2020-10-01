
package vn.payme.sdk

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.charset.Charset


internal class NetworkRequest(
    private val context: Context,
    private val url: String,
    private val path: String,
    private val token: String,
    private val params: MutableMap<String, Any>?
) {
    fun setOnRequest(
            onStart: (() -> Unit)?,
            onSuccess: (response: JSONObject) -> Unit,
            onError: ((String) -> Unit)?,
            onFinally: (() -> Unit)?,
            onExpired: (() -> Unit)?
    ) {
        if (onStart != null) {
            onStart()
        }
        val queue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
                Method.POST,
                url + path,
                JSONObject(params as Map<*, *>),
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (jsonObject.getInt("code") == 1000) {
                            onSuccess(jsonObject.getJSONObject("data"))
                        } else if (jsonObject.getInt("code") == 401) {
                            if (onExpired != null) {
                                onExpired()
                            }
                        } else {
                            val errorMessage = jsonObject.getJSONObject("data").getString("message")
                            if (onError != null) {
                                onError(errorMessage)
                            }
                        }
                        if (onFinally != null) {
                            onFinally()
                        }
                    } catch (error: JSONException) {
                        error.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    if (onFinally != null) {
                        onFinally()
                    }
                    Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
                }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers: MutableMap<String, String> = mutableMapOf()
                headers["Authorization"] = token
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(request)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setOnRequestCrypto(
        onStart: (() -> Unit)?,
        onSuccess: (response: JSONObject) -> Unit,
        onError: (String) -> Unit,
        onFinally: (() -> Unit)?,
        onExpired: (() -> Unit)?
    ) {
        val cryptoAES = CryptoAES()
        val cryptoRSA = CryptoRSA()

        val encryptKey = "10000000"
        val xAPIKey = cryptoRSA.encrypt(encryptKey)
        val xAPIAction = cryptoAES.encryptAES(encryptKey, path)
        val xAPIMessage = cryptoAES.encryptAES(encryptKey, JSONObject(params as Map<*, *>).toString())


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

        if (onStart != null) {
            onStart()
        }

        val body: MutableMap<String, Any> = mutableMapOf()
        body["x-api-message"] = xAPIMessage
        val queue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
                Method.POST,
                url,
                JSONObject(body as Map<*, *>),
                Response.Listener { response ->
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
                        val finalJSONObject = JSONObject(result)
                        if (finalJSONObject.getInt("code") == 1000) {
                            onSuccess(finalJSONObject.getJSONObject("data"))
                        } else if (finalJSONObject.getInt("code") == 401) {
                            if (onExpired != null) {
                                onExpired()
                            }
                        } else {
                            val errorMessage = finalJSONObject.getJSONObject("data").getString("message")
                            if (onError != null) {
                                onError(errorMessage)
                            }
                        }
                        if (onFinally != null) {
                            onFinally()
                        }
                    } catch (error: JSONException) {
                        error.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    if (onFinally != null) {
                        onFinally()
                    }
                    if (onError != null) {
                        onError("$error")
                    }
                    Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
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
                            Charset.forName(HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET)))
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
        queue.add(request)
    }


}
