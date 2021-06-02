package vn.payme.sdk.api

import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.store.Store
import java.nio.charset.StandardCharsets
import java.util.HashMap
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
   private suspend fun uploadFileCoroutine(file: ByteArray): ResponseHander = suspendCoroutine { cont ->
        uploadFile(file, { responseHander ->
            cont.resume(responseHander)
        })
    }
    suspend fun upLoadKYC(
        imageFront: ByteArray?,
        imageBackSide: ByteArray?,
        imageFace: ByteArray?,
        video: ByteArray?,
        identifyType: String?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main + Job()).launch {
            var urlImageFront: String? = null
            var urlImageBack: String? = null
            var urlImageFace: String? = null
            var urlVideo: String? = null
            if (imageFront != null) {

                val responseImageFront = uploadFileCoroutine(imageFront!!)
                if (responseImageFront.status) {
                    urlImageFront = responseImageFront.path

                } else {
                    onError(
                        responseImageFront?.data,
                        responseImageFront?.code,
                        responseImageFront?.message!!
                    )
                    return@launch

                }

            }
            if (imageBackSide != null) {

                val responseImageBack = uploadFileCoroutine(imageBackSide!!)

                if (responseImageBack.status) {
                    urlImageBack = responseImageBack.path

                } else {
                    onError(
                        responseImageBack.data,
                        responseImageBack.code,
                        responseImageBack.message!!
                    )
                    return@launch


                }


            }
            if (video != null) {

                val responseVideo = uploadFileCoroutine(video!!)

                if (responseVideo.status) {
                    urlVideo = responseVideo.path
                } else {
                    onError(responseVideo.data, responseVideo.code, responseVideo.message!!)
                    return@launch

                }


            }
            if (imageFace != null) {

                val responseImageFace = uploadFileCoroutine(imageFace!!)

                if (responseImageFace.status) {
                    urlImageFace = responseImageFace.path
                } else {
                    onError(
                        responseImageFace.data,
                        responseImageFace.code,
                        responseImageFace.message!!
                    )
                    return@launch


                }

            }


            uploadKycInfo(urlImageFront, urlImageBack, urlImageFace, urlVideo,identifyType, onSuccess, onError)
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
        val b = object : VolleyMultipartRequest(
            Method.POST,
            ENV_API.API_STATIC,
            { response ->
                val a = response.data
                val b = String(a, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(b)
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
        identifyType: String?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val kycInput: MutableMap<String, Any> = mutableMapOf()
        val image: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation KYCMutation(\$kycInput: KYCInput!) {\n" +
                "  Account {\n" +
                "    KYC(input: \$kycInput) {\n" +
                "      succeeded\n" +
                "      message\n" +
                "    }\n" +
                "  }\n" +
                "}"
        if (imageBackSide != null && imageFront != null) {
            image["back"] = imageBackSide!!
            image["front"] = imageFront!!
            if(CameraKycActivity.updateOnlyIdentify){
                kycInput["identifyIC"] = image!!
            }else{
                kycInput["image"] = image!!

            }
        }
        if (video != null) {
            kycInput["video"] = video!!
        }
        if (face != null) {
            kycInput["face"] = face!!
        }
        if(identifyType!=null){
            kycInput["identifyType"] = identifyType!!
        }
        kycInput["clientId"] = Store.config.clientId
        variables["kycInput"] = kycInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
          )


    }

}