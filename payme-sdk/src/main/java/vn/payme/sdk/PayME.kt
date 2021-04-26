package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.payme.sdk.api.AccountApi
import vn.payme.sdk.api.ENV_API
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.api.QueryBuilder
import vn.payme.sdk.enums.*
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.*
import vn.payme.sdk.payment.PaymePayment
import java.nio.charset.StandardCharsets
import java.security.Security
import java.text.DecimalFormat

public class PayME(
    context: Context,
    appToken: String,
    publicKey: String,
    connectToken: String,
    appPrivateKey: String,
    configColor: Array<String>,
    language: LANGUAGES,
    env: Env,
    showLog: Boolean,
) {
    companion object {
        lateinit var appPrivateKey: String
        var appToken: String = ""
        lateinit var publicKey: String
        var connectToken: String = ""
        var action: Action? = null
        var amount: Int = 0
        var balance: Int = 0
        var appID: Int = 0
        var content: String? = null
        var clientId: String = ""
        var handShake: String? = ""
        var accountKycSuccess: Boolean = false
        var accountActive: Boolean = false
        var accessToken: String? = ""
        var showLog: Boolean = false
        var extraData: String? = null
        var infoPayment: InfoPayment? = null
        var limitPayment: MaxminPayment = MaxminPayment(2000, 100000)
        var limitAll: MaxminPayment = MaxminPayment(2000, 100000)
        var clientInfo: ClientInfo = ClientInfo()
        var env: Env? = null
        var configColor: Array<String>? = null
        lateinit var context: Context
        var dataInit: JSONObject? = null
        lateinit var onSuccess: ((JSONObject?) -> Unit)
        lateinit var onError: (JSONObject?, Int?, String) -> Unit
        var colorApp: ColorApp = ColorApp("#08941f", "#0eb92a")
        var methodSelected: Method? = null
        lateinit var fragmentManager: FragmentManager
        var numberAtmCard = ""
        var transaction = ""
        internal var openPayAndKyc: Boolean = true
        private lateinit var listService: ArrayList<Service>
        internal var service: Service? = null
        internal var language = LANGUAGES.VN
        internal var isShowResultUI = true


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
            isShowResultUI: Boolean,
            method: Method?,
            onSuccess: ((JSONObject?) -> Unit)?,
            onError: ((JSONObject?, Int?, String) -> Unit)?,
        ) {
            PayME.isShowResultUI = isShowResultUI
            if (onSuccess != null) {
                Companion.onSuccess = onSuccess
            }
            if (onError != null) {
                Companion.onError = onError
            }
            if (!accountActive) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
            } else if (!accountKycSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
            } else {
                PayME.fragmentManager = fragmentManager
                Companion.infoPayment = infoPayment

                if (amount != null) {
                    Companion.amount = amount
                } else {
                    Companion.amount = 0
                }
                val decimal = DecimalFormat("#,###")
                if (infoPayment.amount!! < limitPayment.min) {
                    onError(
                        null,
                        ERROR_CODE.LITMIT,
                        "Số tiền giao dịch tối thiểu ${decimal.format(limitPayment.min)} VND"
                    )
                } else if (infoPayment.amount!! > limitPayment.max) {
                    onError(
                        null,
                        ERROR_CODE.LITMIT,
                        "Số tiền giao dịch tối đa ${decimal.format(limitPayment.max)} VND"
                    )
                } else {
                    PayME.methodSelected = method
                    val paymePayment: PaymePayment = PaymePayment()
                    paymePayment.show(
                        fragmentManager,
                        "ModalBottomSheet"
                    )
                }
            }
        }

    }

    private fun getAppID(): Int {
        try {
            val listId = PayME.appToken.split(".")
            val appID = Base64.decode(listId[1], Base64.DEFAULT)
            val appID_UTF_8 = String(appID, StandardCharsets.UTF_8)
            val jsonObject = JSONObject(appID_UTF_8)
            return jsonObject.getInt("appId")
        } catch (e: Exception) {
            return 0
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestDownload(familyName: String) {
        val queryBuilder = QueryBuilder(
            familyName,
            width = 0f,
            weight = 0,
            italic = 0f,
            besteffort = false
        )
        val query = queryBuilder.build()
        val mHandler = Handler()


        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            query,
            R.array.com_google_android_gms_fonts_certs
        )

        val callback = object : FontsContractCompat.FontRequestCallback() {
            override fun onTypefaceRetrieved(typeface: Typeface) {

            }

            override fun onTypefaceRequestFailed(reason: Int) {

            }
        }
        FontsContractCompat
            .requestFont(PayME.context, request, callback, mHandler)
    }

    public fun login(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        val paymentApi = PaymentApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")

        val dataRegisterClientInfo = pref.getString("dataRegisterClientInfo", "")
        paymentApi.getSettings(onSuccess = { jsonObject ->
            val Setting = jsonObject.getJSONObject("Setting")
            val configs = Setting.getJSONArray("configs")
            var checkValuePayment = false

            for (i in 0 until configs.length()) {
                val config = configs.optJSONObject(i)
                val key = config.optString("key")
                val valueString = config.optString("value")

                if (key == "limit.param.amount.payment" && valueString != null) {
                    val value = JSONObject(valueString)
                    val max = Integer.parseInt(value.optString("max"))
                    val min = Integer.parseInt(value.optString("min"))
                    checkValuePayment = true
                    limitPayment.min = min
                    limitPayment.max = max
                }
                if (key == "limit.param.amount.all" && valueString != null) {
                    val value = JSONObject(valueString)
                    val max = Integer.parseInt(value.optString("max"))
                    val min = Integer.parseInt(value.optString("min"))
                    limitAll.min = min
                    limitAll.max = max
                }
                if (key == "service.main.visible" && valueString != null) {
                    val value = JSONObject(valueString)
                    PayME.listService = arrayListOf()
                    val listService = value.optJSONArray("listService")
                    for (i in 0 until listService.length()) {
                        val json = listService.getJSONObject(i)
                        val code = json.optString("code")
                        val description = json.optString("description")
                        val disable = json.optBoolean("disable")
                        val enable = json.optBoolean("enable")
                        if (enable == true && disable == false) {
                            PayME.listService.add(Service(code, description))
                        }
                    }
                }
            }
            if (!checkValuePayment) {
                limitPayment.max = limitAll.max
                limitPayment.min = limitAll.min
            }

        },
            onError = { jsonObject, code, message ->

            }
        )
        PayME.accountActive = false
        PayME.accountKycSuccess = false

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
                    loginAccount(onSuccess = { jsonObject ->
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
            loginAccount(onSuccess = { accountInfo ->
                onSuccess(accountInfo)
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
        PayME.showLog = showLog
        Companion.colorApp = ColorApp(configColor[0], configColor[1])
        Companion.clientInfo = ClientInfo(context)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        ENV_API.updateEnv()
        PayME.accountActive = false
        PayME.accountKycSuccess = false
        PayME.appID = getAppID()
        PayME.language = language
    }


    public fun openWallet(
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (connectToken.length > 0) {
            openWalletActivity(Action.OPEN, 0, null, null, null, onSuccess, onError)
        }

    }

    private fun openWalletActivity(
        action: Action,
        amount: Int?,
        content: String?,
        extraData: String?,
        service: Service?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        PayME.service = service
        if (connectToken.length > 0) {
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


    }


    public fun deposit(
        amount: Int,
        content: String?,
        extraData: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        Companion.content = content
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        if (!accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
            this.openWalletActivity(
                Action.DEPOSIT,
                amount,
                content,
                extraData,
                null,
                onSuccess,
                onError
            )
        }
    }

    public fun openService(
        service: Service,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        Companion.content = content
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        if (!accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
            this.openWalletActivity(
                Action.UTILITY,
                amount,
                content,
                extraData,
                service,
                onSuccess,
                onError
            )
        }
    }


    public fun withdraw(
        amount: Int,
        content: String?,
        extraData: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        Companion.content = content
        Companion.extraData = extraData
        if (amount != null) {
            Companion.amount = amount
        } else {
            Companion.amount = 0
        }
        if (!accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
            this.openWalletActivity(
                Action.WITHDRAW,
                amount,
                content,
                extraData,
                null,
                onSuccess,
                onError
            )
        }


    }


    public fun pay(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit,
    ) {
        println("Vao pay")

        if (!accountActive) {
            println("Tài khoản chưa kích hoạt")
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!accountKycSuccess) {
            println("Tài khoản chưa định danh")

            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
            println("con lai")

            if (method != null && method?.type != TYPE_PAYMENT.WALLET && !PayME.openPayAndKyc) {
                PayME.showError("Chức năng chỉ có thể thao tác môi trường production")
            } else {
                PayME.pay(fragmentManager, infoPayment, isShowResultUI, method, onSuccess, onError)

            }
        }

    }

    fun closeOpenWallet() {
        var even: EventBus = EventBus.getDefault()
        var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
        even.post(myEven)
        var closeWebview: MyEven = MyEven(TypeCallBack.onExpired, "")
        even.post(myEven)
        even.post(closeWebview)
    }

    public fun getAccountInfo(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        loginAccount(onSuccess, onError)
    }

    private fun loginAccount(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        accountApi.intAccount(onSuccess = { jsonObject ->
            val OpenEWallet = jsonObject.getJSONObject("OpenEWallet")
            val Init = OpenEWallet.getJSONObject("Init")
            PayME.dataInit = Init
            val kyc = Init.optJSONObject("kyc")
            val appEnv = Init.optString("appEnv")
            val succeeded = Init.optBoolean("succeeded")
            PayME.accountActive = succeeded
            if (appEnv == Env.SANDBOX.toString()) {
                openPayAndKyc = false
            } else {
                openPayAndKyc = true
            }
            if (kyc != null) {
                val state = kyc.optString("state")
                if (state == "APPROVED") {
                    PayME.accountKycSuccess = true
                } else {
                    PayME.accountKycSuccess = false
                }
            } else {
                PayME.accountKycSuccess = false
            }
            val accessToken = Init.optString("accessToken")
            val handShake = Init.optString("handShake")
            if (!accessToken.equals("null")) {
                PayME.accessToken = accessToken
            } else {
                PayME.accessToken = ""
            }
            PayME.handShake = handShake
            if (PayME.accountActive) {
                if (PayME.accountKycSuccess) {
                    onSuccess(AccountStatus.KYC_OK)
                } else {
                    onSuccess(AccountStatus.NOT_KYC)
                }
            } else {
                onSuccess(AccountStatus.NOT_ACTIVED)
            }
        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
        })
    }


    public fun logout() {
        PayME.accessToken = ""
        PayME.connectToken = ""
        PayME.handShake = ""
        this.closeOpenWallet()
    }

    public fun getSupportedServices(): ArrayList<Service> {
        return PayME.listService
    }

    public fun getPaymentMethods(
        onSuccess: (ArrayList<Method>) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (!accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
            val paymentApi = PaymentApi()
            val listMethod: ArrayList<Method> = ArrayList<Method>()
            paymentApi.getTransferMethods(
                onSuccess = { jsonObject ->
                    val Utility = jsonObject.optJSONObject("Utility")
                    val GetPaymentMethod = Utility.optJSONObject("GetPaymentMethod")
                    val message = GetPaymentMethod.optString("message")
                    val succeeded = GetPaymentMethod.optBoolean("succeeded")
                    val methods = GetPaymentMethod.optJSONArray("methods")
                    if (succeeded) {
                        for (i in 0 until methods.length()) {
                            val jsonObject = methods.getJSONObject(i)
                            var data = jsonObject.optJSONObject("data")
                            var dataMethod = DataMethod(null, "")
                            if (data != null) {
                                val linkedId = data.optString("linkedId")
                                val swiftCode = data.optString("swiftCode")
                                dataMethod = DataMethod(linkedId, swiftCode)
                            }
                            var fee = jsonObject.optInt("fee")
                            var label = jsonObject.optString("label")
                            var methodId = jsonObject.optInt("methodId")
                            var minFee = jsonObject.optInt("minFee")
                            var title = jsonObject.optString("title")
                            var type = jsonObject.optString("type")
                            listMethod.add(
                                Method(
                                    dataMethod,
                                    fee,
                                    label,
                                    methodId,
                                    minFee,
                                    title,
                                    type,
                                )
                            )
                        }
                        onSuccess(listMethod)
                    } else {
                        onError(null, null, message)
                    }
                },
                onError
            )
        }

    }

    public fun getWalletInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (!PayME.accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else {
            val paymentApi = PaymentApi()
            paymentApi.getBalance(onSuccess, onError)
        }
    }
}


