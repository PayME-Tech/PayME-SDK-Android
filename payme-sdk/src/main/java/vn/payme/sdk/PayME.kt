package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
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
        lateinit var publicKey: String
        var connectToken: String = ""
        lateinit var action: Action
        var amount: Int = 0
        var balance: Int = 0
        var content: String? = null
        var clientId: String = ""
        var handShake: String? = ""
        var accessToken: String? = ""
        var orderId: String? = null
        var extraData: String? = null
        var infoPayment: InfoPayment? = null
        var clientInfo: ClientInfo = ClientInfo()
        var env: Env? = null
        var configColor: Array<String>? = null
        lateinit var context: Context
        lateinit var onSuccess: ((JSONObject) -> Unit)
        lateinit var onError: ((String) -> Unit)
        lateinit var onPay: ((String) -> Unit)
        lateinit var onClose: (() -> Unit)
        lateinit var colorApp: ColorApp
        lateinit var methodSelected: Method
         var numberAtmCard = ""
         var transaction = ""

        //KYC
        var kycIdenity = false
        var kycVideo = false
        var kycFace = false
        fun showError(message:String){
            Toasty.error(PayME.context, message, Toast.LENGTH_SHORT, true).show();
        }

        public fun pay(
            fragmentManager: FragmentManager,
            infoPayment: InfoPayment,
            onSuccess: (JSONObject) -> Unit,
            onError: (String) -> Unit,
            onClose: () -> Unit,
        ) {
            Companion.infoPayment = infoPayment
            Companion.onClose = onClose
            if (amount != null) {
                Companion.amount = amount
            } else {
                Companion.amount = 0
            }
            val paymePayment: PaymePayment = PaymePayment()
            paymePayment.show(
                fragmentManager,
                "ModalBottomSheet"
            )

        }

    }

    public fun initAccount(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")
        val dataRegisterClientInfo = pref.getString("dataRegisterClientInfo", "")

        if (clientId?.length!! <= 0 || !dataRegisterClientInfo.equals(PayME.clientInfo.getClientInfo().toString()+PayME.env.toString())) {
            accountApi.registerClient(
                onSuccess = { jsonObject ->
                    val Client = jsonObject?.optJSONObject("Client")
                    val Register = Client?.optJSONObject("Register")
                    val clientId = Register?.optString("clientId")
                    PayME.clientId = clientId.toString()
                    this.getAccountInfo(onSuccess = { jsonObject ->
                        onSuccess(jsonObject)
                    }, onError = { jsonObject, code, message ->
                        onError(jsonObject, code, message)
                    })
                },
                onError = { jsonObject, code, message ->
                    onError(jsonObject, code, message)
                }
            )
        } else {
            PayME.clientId = clientId
            this.getAccountInfo(onSuccess = { jsonObject ->
                onSuccess(jsonObject)
            }, onError = { jsonObject, code, message ->
                onError(jsonObject, code, message)

            })

        }
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
        ENV_API.updateEnv()





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
        PayME.kycVideo = true
        PayME.kycIdenity = true
        PayME.kycFace = true

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
        infoPayment: InfoPayment,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit,
        onClose: () -> Unit,
    ) {
        PayME.pay(fragmentManager, infoPayment, onSuccess, onError, onClose)

    }


    public fun getAccountInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        accountApi.intAccount(onSuccess = { jsonObject ->
            onSuccess(jsonObject)
            val OpenEWallet = jsonObject.getJSONObject("OpenEWallet")
            val Init = OpenEWallet.getJSONObject("Init")

            val isExistInMainWallet = Init.optBoolean("isExistInMainWallet")
//            Cần phải Register hay không, hay chỉ Login của người dùng ( false -> gọi register, true -> gọi login)

            val succeeded = Init.optBoolean("succeeded")

//            Kết quả (có tồn tại account hay chưa )

            val kyc = Init.optJSONObject("kyc")
            if (kyc != null) {
                val state = kyc.optString("kyc")
                //           APPROVED
//            Đã duyệt
//             REJECTED
//            Đã từ chối
//            PENDING
//            Chờ duyệt
//            CANCELED
//            Đã huỷ
//            BANNED
//            Bị ban do sai nhìu lần
            }


            val accessToken = Init.optString("accessToken")
            val handShake = Init.optString("handShake")

            if (!accessToken.equals("null")) {
                PayME.accessToken = accessToken
            } else {
                PayME.accessToken = ""
            }

            PayME.handShake = handShake

            println("jsonObject" + jsonObject)

        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
            println("jsonObject" + jsonObject)
            println("code" + code)
            println("message" + message)

        })
    }

    public fun getWalletInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getBalance(onSuccess, onError)
    }
}


