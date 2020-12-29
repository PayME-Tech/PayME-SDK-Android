package vn.payme.sdk

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
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
        var kycFade = false

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
//        val intent = Intent(context, PaymeWaletActivity::class.java)
        PayME.kycVideo= true
        PayME.kycIdenity= false
        PayME.kycFade= false

        val intent = Intent(context, CameraKycActivity::class.java)
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
//        val paymePayment: PopupSelectTypeIndentify = PopupSelectTypeIndentify()
//        paymePayment.show(
//            fragmentManager,
//            "ModalBottomSheet"
//        )
        val paymePayment: PaymePayment = PaymePayment()
        paymePayment.show(
            fragmentManager,
            "ModalBottomSheet"
        )

    }



    public fun isConnected(): Boolean {
        return false
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
//        val url = urlFeENV("sandbox")
//        val path = "/graphql"
//        val params: MutableMap<String, Any> = mutableMapOf()
//        val variables: MutableMap<String, Any> = mutableMapOf()
//        val query= "query Query(\$feedFeedId: BigInt!) {\n" +
//                "  Feed(feedId: \$feedFeedId) {\n" +
//                "    feedId\n" +
//                "  }\n" +
//                "}"
//        println("STRINGTEST"+query.toString())
//        params["query"] =query
//
//        variables["feedFeedId"] = 10
//        params["variables"] = variables
//        val request = NetworkRequest(context!!, url, path, PayME.token, params)
//        request.setOnRequestCrypto(
//            onError = onError,
//            onSuccess = onSuccess,
//            onExpired = {
//                println("401")
//
//            })


    }
}


