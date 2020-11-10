package vn.payme.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.json.JSONObject
import vn.payme.sdk.api.NetworkRequest
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.model.*

public class PayME {
    companion object {
        lateinit var appPrivateKey: String
        var appToken: String = ""
        lateinit var publicKey: String
        var connectToken: String = ""
        lateinit var action: Action
        var amount: Int = 0
        var description: String? = null
        var transactionId: String? = null
        var extraData: String? = null
        var clientInfo: ClientInfo = ClientInfo()
        var env: Env? = null
        var configColor: Array<String>? = null
        lateinit var context: Context
        lateinit var onSuccess: ((JSONObject) -> Unit)
        lateinit var onError: ((String) -> Unit)
        lateinit var onPay: ((String) -> Unit)
        lateinit var colorApp: ColorApp

    }


    constructor(context: Context, appToken: String, publicKey: String, connectToken: String, appPrivateKey: String, configColor: Array<String>, env: Env) {
        PayME.appToken = appToken
        PayME.appPrivateKey = appPrivateKey
        PayME.publicKey = publicKey
        PayME.connectToken = connectToken
        PayME.configColor = configColor
        PayME.env = env
        PayME.context = context
        Companion.colorApp = ColorApp(configColor[0], configColor[1])
        Companion.clientInfo = ClientInfo(context)

    }


    public fun openWallet(
            action: Action,
            amount: Int?,
            description: String?,
            extraData: String?,
            onSuccess: (JSONObject) -> Unit,
            onError: (String) -> Unit
    ) {

        Companion.action = action
        Companion.description = description
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        val intent: Intent = Intent(context, PaymeWaletActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(intent)
        Companion.onSuccess = onSuccess
        Companion.onError = onError
    }


    public fun deposit(
            amount: Int,
            description: String?,
            extraData: String,
            onSuccess: (JSONObject) -> Unit,
            onError: (String) -> Unit
    ) {
        Companion.description = description
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }

        this.openWallet(Action.DEPOSIT, amount, description, extraData, onSuccess, onError)

    }


    public fun withdraw(
            amount: Int,
            description: String?,
            extraData: String,
            onSuccess: (JSONObject) -> Unit,
            onError: (String) -> Unit
    ) {
        Companion.description = description
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        this.openWallet(Action.WITHDRAW, amount, description, extraData, onSuccess, onError)


    }

    public fun pay(
        fragmentManager: FragmentManager,
            amount: Int,
            description: String?,
            transactionId: String?,
            extraData: String,
            onSuccess: (JSONObject) -> Unit,
            onError: (String) -> Unit
    ) {
        Companion.description = description
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        Companion.transactionId = transactionId
        val activity: Activity = PayME.context as AppCompatActivity
        val paymePayment: PaymePayment = PaymePayment()
        paymePayment.show(
            fragmentManager,
                "ModalBottomSheet")

    }


    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-wam.payme.vn"
        }
        return "https://wam.payme.vn"
    }

    public fun isConnected(): Boolean {
        return false
    }
    public  fun  genConnectToken (userId:String,phone:String?,onSuccess: (JSONObject) -> Unit, onError: (JSONObject?,Int?,String) -> Unit) {
        val paymentApi =  PaymentApi()
        paymentApi.genConnectToken(userId,phone,onSuccess,onError)

    }

    public fun getWalletInfo(onSuccess: (JSONObject) -> Unit, onError: (JSONObject?,Int?,String) -> Unit) {
        val url = urlFeENV("sandbox")
        val path = "/v1/Wallet/Information"
        val params: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = connectToken.toString()
        params["clientInfo"] = PayME.clientInfo.getClientInfo()

        val request = NetworkRequest(context!!, url, path, appToken, params)
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


