package vn.payme.sdk.store

import android.util.Base64
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.LANGUAGES
import vn.payme.sdk.model.*
import java.nio.charset.StandardCharsets

class Config {
    var appPrivateKey: String = ""
    var appToken: String = ""
    var publicKey: String
    var connectToken: String
    var appID: Int
    var handShake: String? = ""
    var showLog: Boolean = false
    var limitPayment: MaxminPayment = MaxminPayment(2000, 100000000)
    var limitAll: MaxminPayment = MaxminPayment(2000, 100000000)
    var clientInfo: ClientInfo? = null
    var clientId : String = ""
    var env: Env? = null
    var configColor: Array<String>? = null
    var colorApp: ColorApp = ColorApp("#08941f", "#0eb92a")
    var openPayAndKyc: Boolean = true
    var language: LANGUAGES = LANGUAGES.VN
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

    constructor(
        appPrivateKey: String,
        appToken: String = "",
        publicKey: String,
        connectToken: String = "",
        showLog: Boolean = false,
        env: Env? = null,
        configColor: Array<String>,
        language: LANGUAGES = LANGUAGES.VN,
    ) {

        this.appPrivateKey = appPrivateKey
        this.appToken = appToken
        this.publicKey = publicKey
        this.connectToken = connectToken
        this.showLog = showLog
        this.env = env
        this.configColor = configColor
        this.colorApp = colorApp
        this.language = language
        if (clientId != null) {
            this.clientId = clientId
        }
        colorApp = ColorApp(configColor.get(0), configColor[1])
        clientInfo = ClientInfo(PayME.context)
        appID = getAppID(appToken)
    }

    private fun getAppID(appToken: String): Int {
        try {
            val listId = appToken.split(".")
            val appID = Base64.decode(listId[1], Base64.DEFAULT)
            val appID_UTF_8 = String(appID, StandardCharsets.UTF_8)
            val jsonObject = JSONObject(appID_UTF_8)
            return jsonObject.getInt("appId")
        } catch (e: Exception) {
            return 0
        }

    }


}