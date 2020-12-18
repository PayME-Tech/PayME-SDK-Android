package vn.payme.sdk.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.Volley
import com.google.zxing.common.BitArray
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.HashMap

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

    fun uploadImage(
        context: Context,
        imageFront: ByteArray,
        imageBackSide:ByteArray,
        typeIdentify: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {


        val queue = Volley.newRequestQueue(context)
        val b = object : VolleyMultipartRequest(
            Method.POST,
            "https://sbx-static.payme.vn/Upload",
            { response ->
                val a = response.data
                val b = String(a, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(b)
                println("REPONSE"+jsonObject.toString())
                val code = jsonObject.getInt("code")
                if (jsonObject.getInt("code") == 1000) {
                    val arrayJson = jsonObject.getJSONArray("data")
                    val jsonObject = arrayJson.getJSONObject(0)
                    var path = jsonObject.optString("path")
                    uploadImage2(context,path,imageBackSide,typeIdentify,onSuccess,onError)
                } else {
                    val message = jsonObject.getString("message")
                    onError(jsonObject,code,message)
                }


            },
            { error ->
                onError(null,null,"Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !")
            }
        ) {

            override fun getByteData(): MutableMap<String, DataPart> {

                val params: MutableMap<String, DataPart> = HashMap()
                params.put("files", DataPart(
                    "file_Payme_Identify_1.jpg",
                    imageFront,
                    "image/jpeg"
                ))


                return params
            }
        }
        val defaultRetryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        b.retryPolicy = defaultRetryPolicy
        queue.add(b)
    }
    fun uploadImage2(
        context: Context,
        imageFront: String,
        imageBackSide:ByteArray,
        typeIdentify: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {


        val queue = Volley.newRequestQueue(context)
        val b = object : VolleyMultipartRequest(
            Method.POST,
            "https://sbx-static.payme.vn/Upload",
            { response ->
                val a = response.data
                val b = String(a, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(b)
                println("REPONSE"+jsonObject.toString())
                val code = jsonObject.getInt("code")
                if (jsonObject.getInt("code") == 1000) {
                    val arrayJson = jsonObject.getJSONArray("data")
                    val jsonObject = arrayJson.getJSONObject(0)
                    var path = jsonObject.optString("path")
                    uploadKycInfo(typeIdentify,imageFront,path,onSuccess,onError)
                } else {
                    val message = jsonObject.getString("message")
                    onError(jsonObject,code,message)
                }
            },
            { error ->
                onError(null,null,"Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !")
            }
        ) {

            override fun getByteData(): MutableMap<String, DataPart> {

                val params: MutableMap<String, DataPart> = HashMap()
                params.put("files", DataPart(
                    "file_Payme_Identify_2.jpg",
                    imageBackSide,
                    "image/jpeg"
                ))

                return params
            }
        }
        val defaultRetryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        b.retryPolicy = defaultRetryPolicy
        queue.add(b)
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