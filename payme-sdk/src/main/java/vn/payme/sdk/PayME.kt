package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.payme.sdk.api.AccountApi
import vn.payme.sdk.api.ENV_API
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.model.*
import vn.payme.sdk.payment.PaymePayment
import java.security.Security


public class PayME(
    context: Context,
    appToken: String,
    publicKey: String,
    connectToken: String,
    appPrivateKey: String,
    configColor: Array<String>,
    env: Env
) {
    companion object {
        lateinit var appPrivateKey: String
        var appToken: String = ""
        var token: String =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MjEzNzMsImFjY291bnRJZCI6MjQyMjQzNjkyOCwic2NvcGUiOltdLCJjbGllbnRJZCI6IjdiYzRhZTZiYjJmZmUwMDEiLCJpYXQiOjE2MDg4NzkwNDZ9.5rQilr8-CMdfsUDqhGE8S8AUSUX1YUnLk8UXUqRGn5k"
        lateinit var publicKey: String
        var connectToken: String = ""
        lateinit var action: Action
        var kycInfo: KycInfo = KycInfo()
        var amount: Int = 0
        var content: String? = null
        var clientId: String = ""
        var handShake: String? = ""
        var accessToken: String? = ""
        var orderId: String? = null
        var extraData: String? = null
        var clientInfo: ClientInfo = ClientInfo()
        var env: Env? = null
        var configColor: Array<String>? = null
        lateinit var context: Context
        lateinit var onSuccess: ((JSONObject) -> Unit)
        lateinit var onError: ((String) -> Unit)
        lateinit var onPay: ((String) -> Unit)
        lateinit var onClose: (() -> Unit)
        lateinit var colorApp: ColorApp

        //KYC
        var kycIdenity = false
        var kycVideo = false
        var kycFace = false

    }


    init {
        PayME.appToken = appToken
        PayME.appPrivateKey = appPrivateKey
        PayME.publicKey = publicKey
        PayME.connectToken = connectToken
        PayME.configColor = configColor
        PayME.env = env
        PayME.context = context
        Companion.colorApp = ColorApp(configColor[0], configColor[1])
        Companion.clientInfo = ClientInfo(context)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        val accountApi = AccountApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")
        ENV_API.updateEnv()
        if (clientId?.length!! <= 0) {
            accountApi.registerClient(
                onSuccess = { jsonObject ->
                    val Client = jsonObject?.optJSONObject("Client")
                    val Register = Client?.optJSONObject("Register")
                    val clientId = Register?.optString("clientId")
                    pref.edit().putString("clientId", clientId).commit()
                    PayME.clientId = clientId.toString()
                    this.isConnected(onSuccess = { jsonObject ->
                        println("jsonObject" + jsonObject)
                    }, onError = { jsonObject, code, message ->
                        println("jsonObject" + jsonObject)
                        println("code" + code)
                        println("message" + message)

                    })
                },
                onError = { jsonObject, code, message ->
                    Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT).show()

                }
            )
        } else {
            PayME.clientId = clientId
            this.isConnected(onSuccess = { jsonObject ->
                println("jsonObjectDKYDDDD" + jsonObject)
            }, onError = { jsonObject, code, message ->
                println("jsonObjectDKYDDDD" + jsonObject)
                println("code" + code)
                println("message" + message)
            })

        }


    }


    public fun openWallet(
        action: Action,
        amount: Int?,
        content: String?,
        extraData: String?,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {

        Companion.action = action
        Companion.content = content
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        val intent = Intent(context, PaymeWaletActivity::class.java)
//        PayME.kycVideo = true
//        PayME.kycIdenity = false
//        PayME.kycFade = false

//        val intent = Intent(context, CameraKycActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(intent)
        Companion.onSuccess = onSuccess
        Companion.onError = onError
    }


    public fun deposit(
        amount: Int,
        content: String?,
        extraData: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Companion.content = Companion.content
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }

        this.openWallet(Action.DEPOSIT, amount, content, extraData, onSuccess, onError)

    }


    public fun withdraw(
        amount: Int,
        content: String?,
        extraData: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Companion.content = content
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        this.openWallet(Action.WITHDRAW, amount, content, extraData, onSuccess, onError)


    }

    public fun pay(
        fragmentManager: FragmentManager,
        amount: Int,
        content: String?,
        orderId: String?,
        extraData: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit,
        onClose: () -> Unit,
    ) {
        Companion.content = content
        Companion.extraData = extraData
        Companion.onClose = onClose
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        Companion.orderId = orderId
        val paymePayment: PaymePayment = PaymePayment()
        paymePayment.show(
            fragmentManager,
            "ModalBottomSheet"
        )

    }


    public fun isConnected(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        accountApi.intAccount(onSuccess = { jsonObject ->
            onSuccess(jsonObject)
            val OpenEWallet = jsonObject.getJSONObject("OpenEWallet")
            val Init = OpenEWallet.getJSONObject("Init")
            val accessToken = Init.optString("accessToken")
            val handShake = Init.optString("handShake")
            val succeeded = Init.optBoolean("succeeded")
            val isExistInMainWallet = Init.optBoolean("isExistInMainWallet")
            PayME.accessToken = accessToken
            PayME.handShake = handShake
            val kyc = Init.optJSONObject("kyc")
            if (kyc != null) {
                val state = kyc.optString("kyc")
            }
            println("jsonObject" + jsonObject)

        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
            println("jsonObject" + jsonObject)
            println("code" + code)
            println("message" + message)

        })
    }

    public fun genConnectToken(
        userId: String,
        phone: String?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.genConnectToken(userId, phone, onSuccess, onError)

    }

    public fun getWalletInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getBalance(onSuccess, onError)
    }
}


