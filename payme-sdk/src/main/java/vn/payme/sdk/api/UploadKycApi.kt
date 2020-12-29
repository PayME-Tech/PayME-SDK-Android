package vn.payme.sdk.api

import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.model.Env
import java.nio.charset.StandardCharsets
import java.util.HashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ResponseHander() {
    var status: Boolean = false
    var code: Int? = null
    var message: String? = null
    var data: JSONObject? = null
    var path: String? = null

    init {

    }

}

class UploadKycApi {
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it, "scheduler").apply { isDaemon = true }
    }

    suspend fun delay(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): String =
        suspendCoroutine { cont ->
            val paymentApi = PaymentApi()
            paymentApi.getBalance(onSuccess = {

            }, onError = { j, f, d ->
                cont.resume("HIEUY")

            })
        }

    suspend fun uploadFileCoroutine(file: ByteArray): ResponseHander = suspendCoroutine { cont ->
        uploadFile(file, { responseHander ->
            cont.resume(responseHander)


        })


    }

    private fun urlStaticENV(env: Env): String {
        if (env == Env.SANDBOX) {
            return "https://sbx-static.payme.vn/Upload"
        }
        return "https://static.payme.vn/Upload"
    }

    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-fe.payme.vn/"
        }
        return "https://fe.payme.vn/"
    }

    suspend fun upLoadKYC(
        imageFront: ByteArray?,
        imageBackSide: ByteArray?,
        imageFade: ByteArray?,
        video: ByteArray?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main + Job()).launch {
            var urlImageFront: String? = null
            var urlImageBack: String? = null
            var urlImageFace: String? = null
            var urlVideo: String? = null
            println("CALLREPONSE1")
            val responseImageFront = uploadFileCoroutine(imageFront!!)
            println("REPONSE1" + responseImageFront.status)
            if (imageFront != null) {


                if (responseImageFront.status) {
                    urlImageFront = responseImageFront.path

                } else {
                    onError(
                        responseImageFront?.data,
                        responseImageFront?.code,
                        responseImageFront?.message!!
                    )

                }

            }
            if (imageBackSide != null) {

                val responseImageBack = uploadFileCoroutine(imageBackSide!!)
                println("REPONSE2" + responseImageBack.status)

                if (responseImageBack.status) {
                    urlImageBack = responseImageBack.path

                } else {
                    onError(
                        responseImageBack.data,
                        responseImageBack.code,
                        responseImageBack.message!!
                    )

                }
                println("responseImageBack" + responseImageBack.toString())


            }
            if (video != null) {
                println("GOI 3")

                val responseVideo = uploadFileCoroutine(video!!)
                println("REPONSE3" + responseVideo.status)

                if (responseVideo.status) {
                    urlVideo = responseVideo.path
                } else {
                    onError(responseVideo.data, responseVideo.code, responseVideo.message!!)
                }


            }
            if (imageFade != null) {
                println("GOI 4")

                val responseImageFade = uploadFileCoroutine(imageFade!!)
                println("REPONSE4" + responseImageFade.status)

                if (responseImageFade.status) {
                    urlVideo = responseImageFade.path
                } else {
                    onError(
                        responseImageFade.data,
                        responseImageFade.code,
                        responseImageFade.message!!
                    )

                }

            }

            uploadKycInfo(urlImageFront, urlImageBack, urlImageFace, urlVideo, onSuccess, onError)
        }

    }

    private fun uploadFile(
        file: ByteArray?,
        onResult: (ResponseHander) -> Unit
    ) {


        val responseHander = ResponseHander()
        responseHander.message =
            "Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !"
        val queue = Volley.newRequestQueue(PayME.context)
        val domain = urlStaticENV(PayME.env!!)
        val b = object : VolleyMultipartRequest(
            Method.POST,
            domain,
            { response ->
                val a = response.data
                val b = String(a, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(b)
                println("REPONSE" + jsonObject.toString())
                val code = jsonObject.getInt("code")
                if (jsonObject.getInt("code") == 1000) {
                    responseHander.code = code
                    responseHander.status = true
                    responseHander.message = null
                    responseHander.data = jsonObject
                    val arrayJson = jsonObject.getJSONArray("data")
                    val jsonObject = arrayJson.getJSONObject(0)
                    var path = jsonObject.optString("path")
                    responseHander.path = path
                    onResult(responseHander)
//                            continuation.resume(responseHander)

                } else {
                    val message = jsonObject.getString("message")

                    responseHander.code = code
                    responseHander.message = message
                    responseHander.data = jsonObject
                    responseHander.status = false
                    onResult(responseHander)


//                            continuation.resume(responseHander)


                }


            },
            { error ->
                val message =
                    "Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !"
                responseHander.code = null
                responseHander.message = message
                responseHander.data = null
                responseHander.status = false
                onResult(responseHander)

//                        continuation.resume(responseHander)


            }
        ) {

            override fun getByteData(): MutableMap<String, DataPart> {

                val params: MutableMap<String, DataPart> = HashMap()
                params.put(
                    "files", DataPart(
                        "file_Payme_Identify_1.jpg",
                        file,
                        "image/jpeg"
                    )
                )



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
        imageFront: String?,
        imageBackSide: String?,
        face: String?,
        video: String?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        println("imageFront" + imageFront)
        println("imageBackSide" + imageBackSide)
        println("face" + face)
        println("video" + video)
        val url = urlFeENV("sandbox")
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val kycInput: MutableMap<String, Any> = mutableMapOf()
        val image: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation Mutation(\$kycInput: KYCInput!) {\n" +
                "  Account {\n" +
                "    KYC(input: \$kycInput) {\n" +
                "      succeeded\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        if (imageBackSide != null && imageFront != null) {
            image["back"] = imageBackSide!!
            image["front"] = imageFront!!
            kycInput["image"] = image!!
        }
        if (video != null) {
            kycInput["video"] = video!!
        }
        if (face != null) {
            kycInput["face"] = face!!
        }

        variables["kycInput"] = kycInput
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, url, path, PayME.token, params)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
            onExpired = {
                println("401")

            })


    }

}