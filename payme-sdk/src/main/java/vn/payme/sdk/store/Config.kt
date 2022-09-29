package vn.payme.sdk.store

import android.util.Base64
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.LANGUAGES
import vn.payme.sdk.model.*
import java.nio.charset.StandardCharsets

class Config(
    var appPrivateKey: String,
    var appToken: String = "",
    var publicKey: String,
    var connectToken: String = "",
    var showLog: Boolean = false,
    var env: Env? = null,
    configColor: Array<String>,
    var language: LANGUAGES = LANGUAGES.VI
) {
    var appID: Int
    var handShake: String? = ""
    var limitPayment: MaxminPayment = MaxminPayment(2000, 100000000)
    var limitPaymentPassword: MaxminPayment = MaxminPayment(2000, 4999999)
    var limitAll: MaxminPayment = MaxminPayment(2000, 100000000)
    var clientInfo: ClientInfo? = null
    var clientId : String = ""
    var configColor: Array<String>? = configColor
    var colorApp: ColorApp = ColorApp("#08941f", "#0eb92a")
    var openPayAndKyc: Boolean = true
    var kycIdentify: Boolean = false
    var kycVideo: Boolean = false
    var kycFace: Boolean = false
    var closeWhenDone: Boolean = false
    var disableCallBackResult: Boolean = false
    var enlableKycIdentify: Boolean = false
    var enlableKycVideo: Boolean = false
    var enlableKycFace: Boolean = false
    var creditSacomAuthLink: String = ""
    var sdkWebSecretKey: String = ""
    var scanModuleEnable: Boolean = false

    init {
        this.colorApp = colorApp
        if (clientId != null) {
            this.clientId = clientId
        }
        colorApp = ColorApp(configColor.get(0), configColor[1])
        clientInfo = ClientInfo(PayME.context)
        appID = getAppID(appToken)
    }

    private fun getAppID(appToken: String): Int {
        return try {
            val listId = appToken.split(".")
            val appID = Base64.decode(listId[1], Base64.DEFAULT)
            val appID_UTF_8 = String(appID, StandardCharsets.UTF_8)
            val jsonObject = JSONObject(appID_UTF_8)
            jsonObject.getInt("appId")
        } catch (e: Exception) {
            0
        }

    }


}