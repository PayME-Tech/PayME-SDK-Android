package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.provider.Settings
import vn.payme.sdk.model.UserInfo

class PaymeModule {
    companion object {
        var transID: String? = ""
        var userID: String? = ""
        var tokenLink: String? = ""
        var action: String? = ""
        var money: Number? = 0
        var info: UserInfo? = null
        val deviceId : String = Settings.Secure.ANDROID_ID
        var appId:String? = ""
        var privateKey: String? = ""
    }


    public fun openLinkWallet(
        context: Context,
        transID: String?,
        userID: String?,
        tokenLink: String?,
        action: String?,
        money: Number?,
        info: UserInfo?
    ) {
        Companion.transID = transID
        Companion.userID = userID
        Companion.tokenLink = tokenLink
        Companion.action = action
        Companion.money = money
        Companion.info = info

        val intent: Intent = Intent(context, PaymeWaletActivity::class.java)
        context.startActivity(intent)
    }

    private fun urlFeENV(env: String?): String {
        if (env == "sandbox") {
            return "https://sbx-wam.payme.vn/"
        }

        return "https://wam.payme.vn/"
    }

    public fun getBalance(context: Context, tokenLink: String,  onSuccessData: ((Int,Int) -> Unit)) {
        val url = urlFeENV("sandbox")
        val path = "/Wallet/Information"
        val request = NetworkRequest(context, url, path, tokenLink, null)
        request.setOnRequestCrypto(
            onStart = {

            },
            onError = {

            },
            onFinally = {

            },
            onSuccess = {response->{
                val balance = response.getInt("balance")
                val detail = response.getJSONObject("detail")
                val cash = detail.getInt("cash")
                val lockCash = detail.getInt("lockCash")
                onSuccessData(cash,lockCash)
            }},
            onExpired = {

            })


    }
}