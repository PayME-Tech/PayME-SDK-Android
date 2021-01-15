package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.payme.sdk.api.AccountApi
import vn.payme.sdk.api.ENV_API
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.evenbus.MyEven
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
        var action: Action? = null
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
        var dataInit: JSONObject? = null
        lateinit var onSuccess: ((JSONObject) -> Unit)
        lateinit var onError: (JSONObject?, Int?, String) -> Unit
        lateinit var onClose: (() -> Unit)
        var colorApp: ColorApp = ColorApp("#08941f","#0eb92a")
        lateinit var methodSelected: Method
        lateinit var fragmentManager: FragmentManager
        var numberAtmCard = ""
        var transaction = ""
        internal var openPayAndKyc: Boolean = true

        //KYC
        var kycIdenity = false
        var kycVideo = false
        var kycFace = false
        fun showError(message: String) {
            Toasty.error(PayME.context, message, Toast.LENGTH_SHORT, true).show();
        }

        internal fun onExpired() {
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
            var myEven2: MyEven = MyEven(TypeCallBack.onExpired, "")
            even.post(myEven)
            even.post(myEven2)
        }

        internal fun pay(
            fragmentManager: FragmentManager,
            infoPayment: InfoPayment,
            onSuccess: ((JSONObject) -> Unit)?,
            onError: ((JSONObject?, Int?, String) -> Unit)?,
        ) {
            PayME.fragmentManager = fragmentManager
            Companion.infoPayment = infoPayment
            if (onSuccess != null) {
                Companion.onSuccess = onSuccess
            }
            if (onError != null) {
                Companion.onError = onError
            }
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
        val paymentApi  = PaymentApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")

        val dataRegisterClientInfo = pref.getString("dataRegisterClientInfo", "")
        paymentApi.getSettings(onSuccess={jsonObject ->

        },
            onError={ jsonObject, code, message->

            }
        )

        if (clientId?.length!! <= 0 || !dataRegisterClientInfo.equals(
                PayME.clientInfo.getClientInfo().toString() + PayME.env.toString()
            )
        ) {
            accountApi.registerClient(
                onSuccess = { jsonObject ->
                    val Client = jsonObject?.optJSONObject("Client")
                    val Register = Client?.optJSONObject("Register")
                    val clientId = Register?.optString("clientId")
                    PayME.clientId = clientId.toString()
                    pref.edit().putString("clientId", clientId.toString()).commit()
                    pref.edit().putString(
                        "dataRegisterClientInfo",
                        PayME.clientInfo.getClientInfo().toString() + PayME.env.toString()
                    ).commit()
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
        onError: (JSONObject?, Int?, String) -> Unit
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
        onError: (JSONObject?, Int?, String) -> Unit
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
        onError: (JSONObject?, Int?, String) -> Unit
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
        onError: (JSONObject?, Int?, String) -> Unit,
    ) {
        PayME.pay(fragmentManager, infoPayment, onSuccess, onError)

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
            PayME.dataInit = Init
            val kyc = Init.optJSONObject("kyc")
            val appEnv = Init.optString("appEnv")
            if(appEnv==Env.DEV.toString()||appEnv==Env.SANDBOX.toString() ){
                openPayAndKyc = false
            }else{
                openPayAndKyc = true
            }
            if (kyc != null) {
                val state = kyc.optString("kyc")
            }
            val accessToken = Init.optString("accessToken")
            val handShake = Init.optString("handShake")
            if (!accessToken.equals("null")) {
                PayME.accessToken = accessToken
            } else {
                PayME.accessToken = ""
            }
            PayME.handShake = handShake
        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
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


