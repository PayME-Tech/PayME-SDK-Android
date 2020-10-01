package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import vn.payme.sdk.model.*
import java.lang.Exception

class PayME {
    companion object {
        lateinit var appPrivateKey: String
        var appToken: String = ""
        lateinit var publicKey: String

        var connectToken: String = ""
        lateinit var action: Action

        var deviceId: String? =  ""

        var amount: Int = 0
        var description: String? = null
        var extraData: JSONObject? = null
        var appVersion  : String = ""
        var sdkVerSion : String = BuildConfig.VERSION_NAME
        var appPackageName : String? = ""
        var env : Env? = null
        var configColor : Array<String>? = null

    }
    public var onSuccess : ((JSONObject) -> Unit)? = null
    public var onError : ((String) -> Unit)? = null

    constructor(context: Context,appToken: String, publicKey: String, connectToken: String, appPrivateKey: String,configColor : Array<String> ?,env: Env) {
        PayME.appToken = appToken
        PayME.appPrivateKey = appPrivateKey
        PayME.publicKey = publicKey
        PayME.connectToken = connectToken
        PayME.configColor  = configColor
        PayME.env = env
        Companion.appPackageName = context.packageName
        val packageInfo :PackageInfo = context.packageManager.getPackageInfo( context.packageName,0)
        PayME.appVersion = packageInfo.versionName

        Companion.deviceId = Settings.Secure.getString(context.contentResolver,Settings.Secure.ANDROID_ID)
        EventBus.getDefault().register(this)

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
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        }
        val intent: Intent = Intent(context, PaymeWaletActivity::class.java)
        context.startActivity(intent)
        this.onSuccess = onSuccess
        this.onError = onError
    }
    @Subscribe
    fun onText(myEven: MyEven){
        if(myEven.type===TypeCallBack.onClose){
            (onError!!)(myEven.value.toString())
        }
        else  if(myEven.type===TypeCallBack.onSuccess){
            val  json :JSONObject = JSONObject(myEven.value.toString())
            (onSuccess!!)(json)
        }


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
            return "https://sbx-wam.payme.vn"
        }

        return "https://wam.payme.vn"
    }

    public fun isConnected(): Boolean {
        return false
    }

    public fun geWalletInfo(context: Context, onSuccess: (JSONObject) -> Unit,onError: (String) -> Unit) {
        val url = urlFeENV("sandbox")
        val path = "/v1/Wallet/Information"
        val params: MutableMap<String, Any> = mutableMapOf()
        params["connectToken"] = connectToken.toString()

        val clientInfo: MutableMap<String, Any> = mutableMapOf()
        clientInfo["clientId"] = deviceId.toString()
        clientInfo["platform"] = "ANDROID"
        clientInfo["appVersion"] = appVersion.toString()
        clientInfo["sdkVesion"] = sdkVerSion.toString()
        clientInfo["sdkType"] = "native"
        clientInfo["appPackageName"] = appPackageName.toString()

        params["clientInfo"] = clientInfo

        println("Appptoken"+PayME.appToken)
        println("params"+params)
        val request = NetworkRequest(context, url, path, PayME.appToken, params)
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


