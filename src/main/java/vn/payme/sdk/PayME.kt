package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.json.JSONObject
import vn.payme.sdk.model.Action

class PayME {
    companion object {
        lateinit var appPrivateKey: String
        var appId: String? = ""
        var publicKey: String? = ""
        var connectToken: String? = ""
        lateinit var action: Action

        val deviceId: String = Settings.Secure.ANDROID_ID

        var amount: Int = 0
        var description: String? = null
        var extraData: JSONObject? = null

    }

    constructor(appId: String, publicKey: String, connectToken: String, appPrivateKey: String) {
        PayME.appId = appId
        PayME.appPrivateKey = appPrivateKey
        PayME.publicKey = publicKey
        PayME.connectToken = connectToken

    }


    public fun openWallet(
        context: Context,
        action: Action,
        amount: Int?,
        description: String?,
        extraData: JSONObject?,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Companion.action = action
        Companion.description = description
        Companion.extraData = extraData!!
        Companion.amount = amount!!

        val intent: Intent = Intent(context, PaymeWaletActivity::class.java)
        context.startActivity(intent)

    }

    public fun deposit(
        amount: Int,
        description: String?,
        extraData: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {

    }

    public fun withdraw(
        amount: Int,
        description: String?,
        extraData: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {

    }

    public fun pay(
        amount: Int,
        description: String?,
        extraData: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {

    }


    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-wam.payme.vn/"
        }

        return "https://wam.payme.vn/"
    }

    public fun isConnected(): Boolean {
        return false
    }

    public fun geWalletInfo(context: Context, onSuccess: (JSONObject) -> Unit,onError: (String) -> Unit) {
        val url = urlFeENV("sandbox")
        val path = "/Wallet/Information"
        val request = NetworkRequest(context, url, path, "", null)
                request.setOnRequestCrypto(
            onStart = {

            },
            onError = onError,
            onFinally = {

            },
            onSuccess = onSuccess,
            onExpired = {

            })


    }
    }


